package org.safehaus.subutai.impl.hbase.handler;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.hbase.HBaseConfig;
import org.safehaus.subutai.impl.hbase.Commands;
import org.safehaus.subutai.impl.hbase.HBaseImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by bahadyr on 8/25/14.
 */
public class InstallOperationHandler extends AbstractOperationHandler<HBaseImpl> {

    private ProductOperation po;
    private HBaseConfig config;


    public InstallOperationHandler( final HBaseImpl manager, final HBaseConfig config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createProductOperation( HBaseConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", config.getClusterName() ) );
    }


    @Override
    public void run() {
        final ProductOperation po =
                manager.getTracker().createProductOperation( HBaseConfig.PRODUCT_KEY, "Installing HBase" );

        manager.getExecutor().execute( new Runnable() {

            public void run() {
                if ( manager.getDbManager()
                            .getInfo( HBaseConfig.PRODUCT_KEY, config.getClusterName(), HBaseConfig.class ) != null ) {
                    po.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                            config.getClusterName() ) );
                    return;
                }

                Set<Agent> allNodes;
                try {
                    allNodes = getAllNodes( config );
                }
                catch ( Exception e ) {
                    po.addLogFailed( e.getMessage() );
                    return;
                }

                if ( manager.getAgentManager().getAgentByHostname( config.getHadoopNameNode() ) == null ) {
                    po.addLogFailed( String.format( "Hadoop NameNode %s not connected", config.getHadoopNameNode() ) );
                    return;
                }

                if ( manager.getDbManager().saveInfo( HBaseConfig.PRODUCT_KEY, config.getClusterName(), config ) ) {

                    po.addLog( "Cluster info saved to DB\nInstalling HBase..." );

                    // Installing Dialog
                            /*po.addLog( "Installing Dialog..." );
                            Command installDialogCommand = Commands.getInstallDialogCommand( allNodes );
                            commandRunner.runCommand( installDialogCommand );

                            if ( installDialogCommand.hasSucceeded() ) {
                                po.addLog( "Installation dialog successful.." );
                            }
                            else {
                                po.addLogFailed(
                                        String.format( "Installation failed, %s", installDialogCommand.getAllErrors()
                                         ) );
                                return;
                            }*/

                    // Installing HBase
                    po.addLog( "Installing HBase on ..." );
                    for ( Agent agent : allNodes ) {
                        po.addLog( agent.getHostname() );
                    }
                    Command installCommand = Commands.getInstallCommand( allNodes );
                    manager.getCommandRunner().runCommand( installCommand );

                    if ( installCommand.hasSucceeded() ) {
                        po.addLog( "Installation HBase successful.." );
                    }
                    else {
                        po.addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
                        return;
                    }

                    po.addLog( "Installation succeeded\nConfiguring master..." );

                    // Configuring master
                    Command configureMasterCommand = Commands.getConfigMasterTask( allNodes,
                            manager.getAgentManager().getAgentByHostname( config.getHadoopNameNode() ).getHostname(),
                            manager.getAgentManager().getAgentByHostname( config.getMaster() ).getHostname() );
                    manager.getCommandRunner().runCommand( configureMasterCommand );

                    if ( configureMasterCommand.hasSucceeded() ) {
                        po.addLog( "Configure master successful..." );
                    }
                    else {
                        po.addLogFailed( String.format( "Configuration failed, %s", configureMasterCommand ) );
                        return;
                    }
                    po.addLog( "Configuring master succeeded\nConfiguring region..." );

                    // Configuring region
                    StringBuilder sbRegion = new StringBuilder();
                    for ( String hostname : config.getRegion() ) {
                        Agent agent = manager.getAgentManager().getAgentByHostname( hostname );
                        sbRegion.append( agent.getHostname() );
                        sbRegion.append( " " );
                    }
                    Command configureRegionCommand =
                            Commands.getConfigRegionCommand( allNodes, sbRegion.toString().trim() );
                    manager.getCommandRunner().runCommand( configureRegionCommand );

                    if ( configureRegionCommand.hasSucceeded() ) {
                        po.addLog( "Configuring region success..." );
                    }
                    else {
                        po.addLogFailed(
                                String.format( "Configuring failed, %s", configureRegionCommand.getAllErrors() ) );
                        return;
                    }
                    po.addLog( "Configuring region succeeded\nSetting quorum..." );

                    // Configuring quorum
                    StringBuilder sbQuorum = new StringBuilder();
                    for ( String hostname : config.getQuorum() ) {
                        Agent agent = manager.getAgentManager().getAgentByHostname( hostname );
                        sbQuorum.append( agent.getHostname() );
                        sbQuorum.append( " " );
                    }
                    Command configureQuorumCommand =
                            Commands.getConfigQuorumCommand( allNodes, sbQuorum.toString().trim() );
                    manager.getCommandRunner().runCommand( configureQuorumCommand );

                    if ( configureQuorumCommand.hasSucceeded() ) {
                        po.addLog( "Configuring quorum success..." );
                    }
                    else {
                        po.addLogFailed(
                                String.format( "Installation failed, %s", configureQuorumCommand.getAllErrors() ) );
                        return;
                    }
                    po.addLog( "Setting quorum succeeded\nSetting backup masters..." );

                    // Configuring backup master
                    Command configureBackupMasterCommand = Commands.getConfigBackupMastersCommand( allNodes,
                            manager.getAgentManager().getAgentByHostname( config.getBackupMasters() ).getHostname() );
                    manager.getCommandRunner().runCommand( configureBackupMasterCommand );

                    if ( configureBackupMasterCommand.hasSucceeded() ) {
                        po.addLogDone( "Configuring backup master success..." );
                    }
                    else {
                        po.addLogFailed( String.format( "Installation failed, %s",
                                configureBackupMasterCommand.getAllErrors() ) );
                        return;
                    }
                    po.addLogDone( "Cluster installation succeeded\n" );
                }
                else {
                    po.addLogFailed( "Could not save cluster info to DB! Please see logs\nInstallation aborted" );
                }
            }
        } );
    }


    private Set<Agent> getAllNodes( HBaseConfig config ) throws Exception {
        final Set<Agent> allNodes = new HashSet<>();

        if ( manager.getAgentManager().getAgentByHostname( config.getMaster() ) == null ) {
            throw new Exception( String.format( "Master node %s not connected", config.getMaster() ) );
        }
        allNodes.add( manager.getAgentManager().getAgentByHostname( config.getMaster() ) );
        if ( manager.getAgentManager().getAgentByHostname( config.getBackupMasters() ) == null ) {
            throw new Exception( String.format( "Backup master node %s not connected", config.getBackupMasters() ) );
        }
        allNodes.add( manager.getAgentManager().getAgentByHostname( config.getBackupMasters() ) );

        for ( String hostname : config.getRegion() ) {
            if ( manager.getAgentManager().getAgentByHostname( hostname ) == null ) {
                throw new Exception( String.format( "Region server node %s not connected", hostname ) );
            }
            allNodes.add( manager.getAgentManager().getAgentByHostname( hostname ) );
        }

        for ( String hostname : config.getQuorum() ) {
            if ( manager.getAgentManager().getAgentByHostname( hostname ) == null ) {
                throw new Exception( String.format( "Quorum node %s not connected", hostname ) );
            }
            allNodes.add( manager.getAgentManager().getAgentByHostname( hostname ) );
        }

        return allNodes;
    }
}
