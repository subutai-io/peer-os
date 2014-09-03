package org.safehaus.subutai.impl.hbase;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.api.hbase.HBase;
import org.safehaus.subutai.api.hbase.HBaseConfig;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.common.protocol.Agent;

import com.google.common.collect.Sets;
import org.safehaus.subutai.common.tracker.ProductOperation;


public class HBaseImpl implements HBase {

    private AgentManager agentManager;
    private Hadoop hadoopManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;
    private CommandRunner commandRunner;


    public void init() {
        Commands.init( commandRunner );
        executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
        executor.shutdown();
    }


    public Hadoop getHadoopManager() {
        return hadoopManager;
    }


    public void setHadoopManager( Hadoop hadoopManager ) {
        this.hadoopManager = hadoopManager;
    }


    public void setDbManager( DbManager dbManager ) {
        this.dbManager = dbManager;
    }


    public void setTracker( Tracker tracker ) {
        this.tracker = tracker;
    }


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setCommandRunner( CommandRunner commandRunner ) {
        this.commandRunner = commandRunner;
    }


    public UUID installCluster( final HBaseConfig config ) {
        final ProductOperation po = tracker.createProductOperation( HBaseConfig.PRODUCT_KEY, "Installing HBase" );

        executor.execute( new Runnable() {

            public void run() {
                if ( dbManager.getInfo( HBaseConfig.PRODUCT_KEY, config.getClusterName(), HBaseConfig.class )
                        != null ) {
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

                if ( agentManager.getAgentByHostname( config.getHadoopNameNode() ) == null ) {
                    po.addLogFailed( String.format( "Hadoop NameNode %s not connected", config.getHadoopNameNode() ) );
                    return;
                }

                if ( dbManager.saveInfo( HBaseConfig.PRODUCT_KEY, config.getClusterName(), config ) ) {

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
                                String.format( "Installation failed, %s", installDialogCommand.getAllErrors() ) );
                        return;
                    }*/

                    // Installing HBase
                    po.addLog( "Installing HBase on ..." );
                    for ( Agent agent : allNodes ) {
                        po.addLog( agent.getHostname() );
                    }
                    Command installCommand = Commands.getInstallCommand( allNodes );
                    commandRunner.runCommand( installCommand );

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
                            agentManager.getAgentByHostname( config.getHadoopNameNode() ).getHostname(),
                            agentManager.getAgentByHostname( config.getMaster() ).getHostname() );
                    commandRunner.runCommand( configureMasterCommand );

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
                        Agent agent = agentManager.getAgentByHostname( hostname );
                        sbRegion.append( agent.getHostname() );
                        sbRegion.append( " " );
                    }
                    Command configureRegionCommand =
                            Commands.getConfigRegionCommand( allNodes, sbRegion.toString().trim() );
                    commandRunner.runCommand( configureRegionCommand );

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
                        Agent agent = agentManager.getAgentByHostname( hostname );
                        sbQuorum.append( agent.getHostname() );
                        sbQuorum.append( " " );
                    }
                    Command configureQuorumCommand =
                            Commands.getConfigQuorumCommand( allNodes, sbQuorum.toString().trim() );
                    commandRunner.runCommand( configureQuorumCommand );

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
                            agentManager.getAgentByHostname( config.getBackupMasters() ).getHostname() );
                    commandRunner.runCommand( configureBackupMasterCommand );

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

        return po.getId();
    }


    public UUID uninstallCluster( final String clusterName ) {
        final ProductOperation po = tracker.createProductOperation( HBaseConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );

        executor.execute( new Runnable() {

            public void run() {
                HBaseConfig config = dbManager.getInfo( HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class );
                if ( config == null ) {
                    po.addLogFailed(
                            String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
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

                po.addLog( "Uninstalling..." );

                Command installCommand = Commands.getUninstallCommand( allNodes );
                commandRunner.runCommand( installCommand );

                if ( installCommand.hasSucceeded() ) {
                    po.addLog( "Uninstallation success.." );
                }
                else {
                    po.addLogFailed( String.format( "Uninstallation failed, %s", installCommand.getAllErrors() ) );
                    return;
                }

                po.addLog( "Updating db..." );
                if ( dbManager.deleteInfo( HBaseConfig.PRODUCT_KEY, config.getClusterName() ) ) {
                    po.addLogDone( "Cluster info deleted from DB\nDone" );
                }
                else {
                    po.addLogFailed( "Error while deleting cluster info from DB. Check logs.\nFailed" );
                }
            }
        } );

        return po.getId();
    }


    public List<HBaseConfig> getClusters() {

        return dbManager.getInfo( HBaseConfig.PRODUCT_KEY, HBaseConfig.class );
    }


    @Override
    public List<Config> getHadoopClusters() {
        return hadoopManager.getClusters();
    }


    @Override
    public Config getHadoopCluster( String clusterName ) {
        return hadoopManager.getCluster( clusterName );
    }


    @Override
    public HBaseConfig getCluster( String clusterName ) {
        return dbManager.getInfo( HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class );
    }


    @Override
    public UUID startCluster( final String clusterName ) {
        final ProductOperation po = tracker.createProductOperation( HBaseConfig.PRODUCT_KEY,
                String.format( "Starting cluster %s", clusterName ) );
        executor.execute( new Runnable() {

            public void run() {
                HBaseConfig config = dbManager.getInfo( HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class );
                if ( config == null ) {
                    po.addLogFailed(
                            String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
                    return;
                }

                Agent master = agentManager.getAgentByHostname( config.getMaster() );
                if ( master == null ) {
                    po.addLogFailed( String.format( "Master node %s not connected", config.getMaster() ) );
                    return;
                }

                Command startCommand = Commands.getStartCommand( Sets.newHashSet( master ) );
                commandRunner.runCommand( startCommand );

                if ( startCommand.hasSucceeded() ) {
                    po.addLogDone( "Start success.." );
                }
                else {
                    po.addLogFailed( String.format( "Start failed, %s", startCommand.getAllErrors() ) );
                }
            }
        } );

        return po.getId();
    }


    @Override
    public UUID stopCluster( final String clusterName ) {
        final ProductOperation po = tracker.createProductOperation( HBaseConfig.PRODUCT_KEY,
                String.format( "Stopping cluster %s", clusterName ) );
        executor.execute( new Runnable() {

            public void run() {
                HBaseConfig config = dbManager.getInfo( HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class );
                if ( config == null ) {
                    po.addLogFailed(
                            String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
                    return;
                }

                Agent master = agentManager.getAgentByHostname( config.getMaster() );
                if ( master == null ) {
                    po.addLogFailed( String.format( "Master node %s not connected", config.getMaster() ) );
                    return;
                }


                Command stopCommand = Commands.getStopCommand( Sets.newHashSet( master ) );
                commandRunner.runCommand( stopCommand );

                if ( stopCommand.hasSucceeded() ) {
                    po.addLogDone( "Stop success.." );
                }
                else {
                    po.addLogFailed( String.format( "Stop failed, %s", stopCommand.getAllErrors() ) );
                }
            }
        } );

        return po.getId();
    }


    @Override
    public UUID checkCluster( final String clusterName ) {
        final ProductOperation po = tracker.createProductOperation( HBaseConfig.PRODUCT_KEY,
                String.format( "Checking cluster %s", clusterName ) );
        executor.execute( new Runnable() {

            public void run() {
                HBaseConfig config = dbManager.getInfo( HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class );
                if ( config == null ) {
                    po.addLogFailed(
                            String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
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
                if ( allNodes == null || allNodes.isEmpty() ) {
                    po.addLogFailed( "Nodes not connected" );
                    return;
                }

                Command checkCommand = Commands.getStatusCommand( allNodes );
                commandRunner.runCommand( checkCommand );

                if ( checkCommand.hasSucceeded() ) {
                    StringBuilder status = new StringBuilder();
                    for ( Agent agent : allNodes ) {
                        status.append( agent.getHostname() ).append( ":\n" )
                              .append( checkCommand.getResults().get( agent.getUuid() ).getStdOut() ).append( "\n\n" );
                    }
                    po.addLogDone( status.toString() );
                }
                else {
                    po.addLogFailed( String.format( "Check failed, %s", checkCommand.getAllErrors() ) );
                }
            }
        } );

        return po.getId();
    }


    private Set<Agent> getAllNodes( HBaseConfig config ) throws Exception {
        final Set<Agent> allNodes = new HashSet<>();

        if ( agentManager.getAgentByHostname( config.getMaster() ) == null ) {
            throw new Exception( String.format( "Master node %s not connected", config.getMaster() ) );
        }
        allNodes.add( agentManager.getAgentByHostname( config.getMaster() ) );
        if ( agentManager.getAgentByHostname( config.getBackupMasters() ) == null ) {
            throw new Exception( String.format( "Backup master node %s not connected", config.getBackupMasters() ) );
        }
        allNodes.add( agentManager.getAgentByHostname( config.getBackupMasters() ) );

        for ( String hostname : config.getRegion() ) {
            if ( agentManager.getAgentByHostname( hostname ) == null ) {
                throw new Exception( String.format( "Region server node %s not connected", hostname ) );
            }
            allNodes.add( agentManager.getAgentByHostname( hostname ) );
        }

        for ( String hostname : config.getQuorum() ) {
            if ( agentManager.getAgentByHostname( hostname ) == null ) {
                throw new Exception( String.format( "Quorum node %s not connected", hostname ) );
            }
            allNodes.add( agentManager.getAgentByHostname( hostname ) );
        }

        return allNodes;
    }
}
