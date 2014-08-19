package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ConfigParams;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperStandaloneSetupStrategy;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterConfigurationException;
import org.safehaus.subutai.shared.protocol.Response;

import com.google.common.collect.Sets;


/**
 * Handles destroy node operation
 */
public class DestroyNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
    private final ProductOperation po;
    private final String lxcHostname;


    public DestroyNodeOperationHandler( ZookeeperImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Destroying %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        final ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null ) {
            po.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }
        if ( !config.getNodes().contains( agent ) ) {
            po.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        if ( config.getNodes().size() == 1 ) {
            po.addLogFailed( "This is the last node in the cluster. Please, destroy cluster instead" );
            return;
        }

        config.getNodes().remove( agent );

        try {
            reconfigureZkCluster( config );
        }
        catch ( ClusterConfigurationException e ) {
            po.addLogFailed( String.format( "Error reconfiguring cluster, %s", e.getMessage() ) );
            return;
        }

        if ( config.getSetupType() == SetupType.STANDALONE ) {
            //destroy lxc
            po.addLog( "Destroying lxc container..." );
            Agent physicalAgent = manager.getAgentManager().getAgentByHostname( agent.getParentHostName() );
            if ( physicalAgent == null ) {
                po.addLog( String.format(
                        "Could not determine physical parent of %s. Use LXC module to cleanup, skipping...",
                        agent.getHostname() ) );
            }
            else {

                try {
                    manager.getContainerManager().cloneDestroy( physicalAgent.getHostname(), agent.getHostname() );
                    po.addLog( "Lxc container destroyed successfully" );
                }
                catch ( LxcDestroyException e ) {
                    po.addLog(
                            String.format( "Could not destroy lxc container %s. Use LXC module to cleanup, skipping...",
                                    e.getMessage() ) );
                }
            }
        }
        else {
            //just uninstall Zookeeper
            po.addLog( String.format( "Uninstalling %s", ZookeeperClusterConfig.PRODUCT_NAME ) );

            Command uninstallCommand = Commands.getUninstallCommand( Sets.newHashSet( agent ) );
            manager.getCommandRunner().runCommand( uninstallCommand );

            if ( uninstallCommand.hasCompleted() ) {
                if ( uninstallCommand.hasSucceeded() ) {
                    po.addLog( "Cluster successfully uninstalled" );
                }
                else {
                    po.addLog( String.format( "Uninstallation failed, %s, skipping...",
                            uninstallCommand.getAllErrors() ) );
                }
            }
            else {
                po.addLog( "Uninstallation failed, command timed out, skipping..." );
            }
        }


        //update db
        po.addLog( "Updating cluster information in database..." );

        try {
            manager.getDbManager().saveInfo2( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            po.addLogDone( "Cluster information updated in database" );
        }
        catch ( DBException e ) {
            po.addLogFailed(
                    String.format( "Error while updating cluster information in database, %s", e.getMessage() ) );
        }
    }


    private void reconfigureZkCluster( final ZookeeperClusterConfig config ) throws ClusterConfigurationException {

        //reconfiguring cluster
        po.addLog( "Reconfiguring cluster..." );

        Command configureClusterCommand;
        try {
            configureClusterCommand = Commands.getConfigureClusterCommand( config.getNodes(),
                    ConfigParams.DATA_DIR.getParamValue() + "/" + ConfigParams.MY_ID_FILE.getParamValue(),
                    ZookeeperStandaloneSetupStrategy.prepareConfiguration( config.getNodes() ),
                    ConfigParams.CONFIG_FILE_PATH.getParamValue() );
        }
        catch ( ClusterConfigurationException e ) {
            throw new ClusterConfigurationException(
                    String.format( "Error reconfiguring cluster %s", e.getMessage() ) );
        }

        manager.getCommandRunner().runCommand( configureClusterCommand );

        if ( configureClusterCommand.hasSucceeded() ) {
            po.addLog( "Cluster reconfigured\nRestarting cluster..." );
            //restart all other nodes with new configuration
            Command restartCommand = Commands.getRestartCommand( config.getNodes() );
            final AtomicInteger count = new AtomicInteger();
            manager.getCommandRunner().runCommand( restartCommand, new CommandCallback() {
                @Override
                public void onResponse( Response response, AgentResult agentResult, Command command ) {
                    if ( agentResult.getStdOut().contains( "STARTED" ) ) {
                        if ( count.incrementAndGet() == config.getNodes().size() ) {
                            stop();
                        }
                    }
                }
            } );

            if ( count.get() == config.getNodes().size() ) {
                po.addLog( "Cluster successfully restarted" );
            }
            else {
                po.addLog(
                        String.format( "Failed to restart cluster, %s, skipping...", restartCommand.getAllErrors() ) );
            }
        }
        else {

            throw new ClusterConfigurationException(
                    String.format( "Cluster reconfiguration failed, %s", configureClusterCommand.getAllErrors() ) );
        }
    }
}
