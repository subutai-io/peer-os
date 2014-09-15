package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import com.google.common.collect.Sets;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.ClusterConfiguration;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;

import java.util.UUID;


/**
 * Handles destroy node operation
 */
public class DestroyNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
    private final String lxcHostname;


    public DestroyNodeOperationHandler( ZookeeperImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Destroying %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return productOperation.getId();
    }


    @Override
    public void run() {
        final ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null ) {
            productOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }
        if ( !config.getNodes().contains( agent ) ) {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        if ( config.getNodes().size() == 1 ) {
            productOperation.addLogFailed( "This is the last node in the cluster. Please, destroy cluster instead" );
            return;
        }

        config.getNodes().remove( agent );
        config.setNumberOfNodes( config.getNumberOfNodes() - 1 );

        try {
            new ClusterConfiguration( manager, productOperation ).configureCluster( config );
        }
        catch ( ClusterConfigurationException e ) {
            productOperation.addLogFailed( String.format( "Error reconfiguring cluster, %s", e.getMessage() ) );
            return;
        }

        if ( config.getSetupType() == SetupType.STANDALONE ) {
            //destroy lxc
            productOperation.addLog( "Destroying lxc container..." );
            Agent physicalAgent = manager.getAgentManager().getAgentByHostname( agent.getParentHostName() );
            if ( physicalAgent == null ) {
                productOperation.addLog( String.format(
                        "Could not determine physical parent of %s. Use LXC module to cleanup, skipping...",
                        agent.getHostname() ) );
            }
            else {

                try {
                    manager.getContainerManager().cloneDestroy( physicalAgent.getHostname(), agent.getHostname() );
                    productOperation.addLog( "Lxc container destroyed successfully" );
                }
                catch ( LxcDestroyException e ) {
                    productOperation.addLog(
                            String.format( "Could not destroy lxc container %s. Use LXC module to cleanup, skipping...",
                                    e.getMessage() ) );
                }
            }
        }
        else {
            //just uninstall Zookeeper
            productOperation.addLog( String.format( "Uninstalling %s", ZookeeperClusterConfig.PRODUCT_NAME ) );

            Command uninstallCommand = Commands.getUninstallCommand( Sets.newHashSet( agent ) );
            manager.getCommandRunner().runCommand( uninstallCommand );

            if ( uninstallCommand.hasCompleted() ) {
                if ( uninstallCommand.hasSucceeded() ) {
                    productOperation.addLog( "Cluster successfully uninstalled" );
                }
                else {
                    productOperation.addLog( String.format( "Uninstallation failed, %s, skipping...",
                            uninstallCommand.getAllErrors() ) );
                }
            }
            else {
                productOperation.addLog( "Uninstallation failed, command timed out, skipping..." );
            }
        }


        //update db
        productOperation.addLog( "Updating cluster information in database..." );

        try {
            manager.getPluginDAO().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            productOperation.addLogDone( "Cluster information updated in database" );
        }
        catch ( DBException e ) {
            productOperation.addLogFailed(
                    String.format( "Error while updating cluster information in database, %s", e.getMessage() ) );
        }
    }
}
