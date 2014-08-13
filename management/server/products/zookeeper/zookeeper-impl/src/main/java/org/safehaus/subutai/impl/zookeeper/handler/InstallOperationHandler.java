package org.safehaus.subutai.impl.zookeeper.handler;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.api.zookeeper.Config;
import org.safehaus.subutai.impl.zookeeper.Commands;
import org.safehaus.subutai.impl.zookeeper.ZookeeperImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.Util;

import com.google.common.base.Strings;


/**
 * Installs Zookeeper cluster either on newly created lxcs or over hadoop cluster nodes
 */
public class InstallOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
    private final ProductOperation po;
    private final Config config;


    public InstallOperationHandler( ZookeeperImpl manager, Config config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker()
                    .createProductOperation( Config.PRODUCT_KEY, String.format( "Installing %s", Config.PRODUCT_KEY ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        if ( Strings.isNullOrEmpty( config.getClusterName() )
                //either number of nodes to create or hadoop cluster nodes must be present
                || ( config.isStandalone() && config.getNumberOfNodes() <= 0 ) || ( !config.isStandalone() && Util
                .isCollectionEmpty( config.getNodes() ) ) ) {
            po.addLogFailed( "Malformed configuration\nInstallation aborted" );
            return;
        }

        if ( manager.getCluster( config.getClusterName() ) != null ) {
            po.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                    config.getClusterName() ) );
            return;
        }

        if ( config.isStandalone() ) {
            installStandalone();
        }
        else {
            installOverHadoop();
        }
    }


    /**
     * installs ZK cluster over supplied Hadoop cluster nodes
     */
    private void installOverHadoop() {

        po.addLog( "Installing over hadoop cluster nodes" );

        //check if node agent is connected
        for ( Iterator<Agent> it = config.getNodes().iterator(); it.hasNext(); ) {
            Agent node = it.next();
            if ( manager.getAgentManager().getAgentByHostname( node.getHostname() ) == null ) {
                po.addLog( String.format( "Node %s is not connected. Omitting this node from installation",
                        node.getHostname() ) );
                it.remove();
            }
        }

        if ( config.getNodes().isEmpty() ) {
            po.addLogFailed( "No nodes eligible for installation. Operation aborted" );
            return;
        }

        po.addLog( "Checking prerequisites..." );

        //check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            po.addLogFailed( "Failed to check presence of installed ksks packages\nInstallation aborted" );
            return;
        }

        for ( Iterator<Agent> it = config.getNodes().iterator(); it.hasNext(); ) {
            Agent node = it.next();

            AgentResult result = checkInstalledCommand.getResults().get( node.getUuid() );

            if ( result.getStdOut().contains( "ksks-zookeeper" ) ) {
                po.addLog(
                        String.format( "Node %s already has Zookeeper installed. Omitting this node from installation",
                                node.getHostname() ) );
                it.remove();
            }
            else if ( !result.getStdOut().contains( "ksks-hadoop" ) ) {
                po.addLog( String.format( "Node %s has no Hadoop installation. Omitting this node from installation",
                        node.getHostname() ) );
                it.remove();
            }
        }


        if ( config.getNodes().isEmpty() ) {
            po.addLogFailed( "No nodes eligible for installation. Operation aborted" );
            return;
        }

        configureCluster( config );
    }


    /**
     * Installs Zk cluster on a newly created set of lxcs
     */
    private void installStandalone() {

        try {
            po.addLog( String.format( "Creating %d lxc containers...", config.getNumberOfNodes() ) );
            Map<Agent, Set<Agent>> lxcAgentsMap = manager.getLxcManager().createLxcs( config.getNumberOfNodes() );
            config.setNodes( new HashSet<Agent>() );

            for ( Map.Entry<Agent, Set<Agent>> entry : lxcAgentsMap.entrySet() ) {
                config.getNodes().addAll( entry.getValue() );
            }
            po.addLog( "Lxc containers created successfully" );


            configureCluster( config );

            if ( po.getState() != ProductOperationState.SUCCEEDED ) {
                try {
                    manager.getLxcManager().destroyLxcs( lxcAgentsMap );
                }
                catch ( LxcDestroyException ignore ) {
                }
            }
        }
        catch ( LxcCreateException ex ) {
            po.addLogFailed( ex.getMessage() );
        }
    }


    private void configureCluster( final Config config ) {

        po.addLog( "Installing Zookeeper..." );

        //install
        Command installCommand = Commands.getInstallCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasCompleted() ) {
            po.addLog( "Installation succeeded\nReconfiguring cluster..." );

            //update settings
            Command updateSettingsCommand = Commands.getUpdateSettingsCommand( config.getNodes() );
            manager.getCommandRunner().runCommand( updateSettingsCommand );

            if ( updateSettingsCommand.hasSucceeded() ) {
                po.addLog( "Cluster reconfigured\nRestarting cluster..." );
                //restart all nodes
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
                    po.addLog( "Cluster restarted successfully" );
                }
                else {
                    po.addLog( String.format( "Failed to restart cluster, %s, skipping...",
                            restartCommand.getAllErrors() ) );
                }

                po.addLog( "Updating information in database..." );

                try {
                    manager.getDbManager().saveInfo2( Config.PRODUCT_KEY, clusterName, config );

                    po.addLogDone( "Information in database updated successfully" );
                }
                catch ( DBException e ) {
                    po.addLogFailed( String.format( "Failed to update information in database, %s", e.getMessage() ) );
                }
            }
            else {
                po.addLogFailed(
                        String.format( "Failed to reconfigure cluster, %s", updateSettingsCommand.getAllErrors() ) );
            }
        }
        else {
            po.addLogFailed( String.format( "Installation failed, %s\nUse Terminal/LXC Module to cleanup",
                    installCommand.getAllErrors() ) );
        }
    }
}
