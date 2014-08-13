package org.safehaus.subutai.impl.hbase;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.api.hadoop.Hadoop;
import org.safehaus.subutai.api.hbase.HBase;
import org.safehaus.subutai.api.hbase.HBaseConfig;
import org.safehaus.subutai.api.tracker.Tracker;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;


public class HBaseImpl implements HBase
{

    private AgentManager agentManager;
    private Hadoop hadoopManager;
    private DbManager dbManager;
    private Tracker tracker;
    private ExecutorService executor;
    private CommandRunner commandRunner;


    public void init()
    {
        Commands.init( commandRunner );
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }


    public void setHadoopManager( Hadoop hadoopManager )
    {
        this.hadoopManager = hadoopManager;
    }


    public void setDbManager( DbManager dbManager )
    {
        this.dbManager = dbManager;
    }


    public void setTracker( Tracker tracker )
    {
        this.tracker = tracker;
    }


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    public void setCommandRunner( CommandRunner commandRunner )
    {
        this.commandRunner = commandRunner;
    }


    public UUID installCluster( final HBaseConfig config )
    {
        final ProductOperation po = tracker.createProductOperation( HBaseConfig.PRODUCT_KEY, "Installing HBase" );

        final Set<Agent> allNodes = getAllNodes( config );
//                new HashSet<Agent>();
//
//        allNodes.add( agentManager.getAgentByUUID( config.getMaster() ) );
//        allNodes.add( agentManager.getAgentByUUID( config.getBackupMasters() ) );
//
//        for ( UUID uuid : config.getRegion() )
//        {
//            allNodes.add( agentManager.getAgentByUUID( uuid ) );
//        }
//
//        for ( UUID uuid : config.getQuorum() )
//        {
//            allNodes.add( agentManager.getAgentByUUID( uuid ) );
//        }

        //        allNodes.addAll( config.getQuorum() );

        executor.execute( new Runnable()
        {

            public void run()
            {
                if ( dbManager.getInfo( HBaseConfig.PRODUCT_KEY, config.getClusterName(), HBaseConfig.class ) != null )
                {
                    po.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                        config.getClusterName() ) );
                    return;
                }

                if ( dbManager.saveInfo( HBaseConfig.PRODUCT_KEY, config.getClusterName(), config ) )
                {

                    po.addLog( "Cluster info saved to DB\nInstalling HBase..." );

                    // Installing HBase
                    po.addLog( "Installing..." );
                    Command installCommand = Commands.getInstallCommand( allNodes );
                    commandRunner.runCommand( installCommand );

                    if ( installCommand.hasSucceeded() )
                    {
                        po.addLog( "Installation success.." );
                    }
                    else
                    {
                        po.addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
                        return;
                    }

                    po.addLog( "Installation succeeded\nConfiguring master..." );

                    // Configuring master
                    Command configureMasterCommand = Commands
                        .getConfigMasterTask( allNodes,
                            agentManager.getAgentByHostname( config.getHadoopNameNode() ).getHostname(),
                            agentManager.getAgentByHostname( config.getMaster() ).getHostname() );
                    commandRunner.runCommand( configureMasterCommand );

                    if ( configureMasterCommand.hasSucceeded() )
                    {
                        po.addLog( "Configure master success..." );
                    }
                    else
                    {
                        po.addLogFailed( String.format( "Configuration failed, %s", configureMasterCommand ) );
                        return;
                    }
                    po.addLog( "Configuring master succeeded\nConfiguring region..." );

                    // Configuring region
                    StringBuilder sbRegion = new StringBuilder();
                    for ( String hostname : config.getRegion() )
                    {
                        Agent agent = agentManager.getAgentByHostname( hostname );
                        sbRegion.append( agent.getHostname() );
                        sbRegion.append( " " );
                    }
                    Command configureRegionCommand = Commands
                        .getConfigRegionCommand( allNodes, sbRegion.toString().trim() );
                    commandRunner.runCommand( configureRegionCommand );

                    if ( configureRegionCommand.hasSucceeded() )
                    {
                        po.addLog( "Configuring region success..." );
                    }
                    else
                    {
                        po.addLogFailed(
                            String.format( "Configuring failed, %s", configureRegionCommand.getAllErrors() ) );
                        return;
                    }
                    po.addLog( "Configuring region succeeded\nSetting quorum..." );

                    // Configuring quorum
                    StringBuilder sbQuorum = new StringBuilder();
                    for ( String hostname : config.getQuorum() )
                    {
                        Agent agent = agentManager.getAgentByHostname( hostname );
                        sbQuorum.append( agent.getHostname() );
                        sbQuorum.append( " " );
                    }
                    Command configureQuorumCommand = Commands
                        .getConfigQuorumCommand( allNodes, sbQuorum.toString().trim() );
                    commandRunner.runCommand( configureQuorumCommand );

                    if ( configureQuorumCommand.hasSucceeded() )
                    {
                        po.addLog( "Configuring quorum success..." );
                    }
                    else
                    {
                        po.addLogFailed(
                            String.format( "Installation failed, %s", configureQuorumCommand.getAllErrors() ) );
                        return;
                    }
                    po.addLog( "Setting quorum succeeded\nSetting backup masters..." );

                    // Configuring backup master
                    Command configureBackupMasterCommand = Commands
                        .getConfigBackupMastersCommand( allNodes,
                            agentManager.getAgentByHostname( config.getBackupMasters() ).getHostname() );
                    commandRunner.runCommand( configureBackupMasterCommand );

                    if ( configureBackupMasterCommand.hasSucceeded() )
                    {
                        po.addLogDone( "Configuring backup master success..." );
                    }
                    else
                    {
                        po.addLogFailed(
                            String.format( "Installation failed, %s", configureBackupMasterCommand.getAllErrors() ) );
                        return;
                    }
                    po.addLogDone( "Cluster installation succeeded\n" );

                }
                else
                {
                    po.addLogFailed( "Could not save cluster info to DB! Please see logs\nInstallation aborted" );
                }

            }
        } );

        return po.getId();
    }


    public UUID uninstallCluster( final String clusterName )
    {
        final ProductOperation po
            = tracker.createProductOperation( HBaseConfig.PRODUCT_KEY,
            String.format( "Destroying cluster %s", clusterName ) );

        executor.execute( new Runnable()
        {

            public void run()
            {
                HBaseConfig config =
                    dbManager.getInfo( HBaseConfig.PRODUCT_KEY, clusterName,
                        HBaseConfig.class );
                if ( config == null )
                {
                    po.addLogFailed(
                        String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
                    return;
                }

                final Set<Agent> allNodes = getAllNodes( config );

                po.addLog( "Uninstalling..." );

                Command installCommand = Commands.getUninstallCommand( allNodes );
                commandRunner.runCommand( installCommand );

                if ( installCommand.hasSucceeded() )
                {
                    po.addLog( "Uninstallation success.." );
                }
                else
                {
                    po.addLogFailed( String.format( "Uninstallation failed, %s", installCommand.getAllErrors() ) );
                    return;
                }

                po.addLog( "Updating db..." );
                if ( dbManager.deleteInfo( HBaseConfig.PRODUCT_KEY, config.getClusterName() ) )
                {
                    po.addLogDone( "Cluster info deleted from DB\nDone" );
                }
                else
                {
                    po.addLogFailed( "Error while deleting cluster info from DB. Check logs.\nFailed" );
                }

            }
        } );

        return po.getId();
    }


    public List<HBaseConfig> getClusters()
    {

        return dbManager.getInfo( HBaseConfig.PRODUCT_KEY, HBaseConfig.class );

    }


    @Override
    public List<Config> getHadoopClusters()
    {
        return hadoopManager.getClusters();
    }


    @Override
    public Config getHadoopCluster( String clusterName )
    {
        return hadoopManager.getCluster( clusterName );
    }


    @Override
    public HBaseConfig getCluster( String clusterName )
    {
        return dbManager.getInfo( HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class );
    }


    @Override
    public UUID startCluster( final String clusterName )
    {
        final ProductOperation po
            = tracker.createProductOperation( HBaseConfig.PRODUCT_KEY,
            String.format( "Starting cluster %s", clusterName ) );
        executor.execute( new Runnable()
        {

            public void run()
            {
                HBaseConfig config = dbManager.getInfo( HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class );
                if ( config == null )
                {
                    po.addLogFailed( String
                        .format( "Cluster with name %s does not exist\nOperation aborted", config.getClusterName() ) );
                    return;
                }

                final Set<Agent> allNodes = getAllNodes( config );

                Command startCommand = Commands.getStartCommand( allNodes );
                commandRunner.runCommand( startCommand );

                if ( startCommand.hasSucceeded() )
                {
                    po.addLogDone( "Start success.." );
                }
                else
                {
                    po.addLogFailed( String.format( "Start failed, %s", startCommand.getAllErrors() ) );
                    return;
                }

            }
        } );

        return po.getId();
    }


    @Override
    public UUID stopCluster( final String clusterName )
    {
        final ProductOperation po
            = tracker.createProductOperation( HBaseConfig.PRODUCT_KEY,
            String.format( "Stopping cluster %s", clusterName ) );
        executor.execute( new Runnable()
        {

            public void run()
            {
                HBaseConfig config = dbManager.getInfo( HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class );
                if ( config == null )
                {
                    po.addLogFailed( String
                        .format( "Cluster with name %s does not exist\nOperation aborted", config.getClusterName() ) );
                    return;
                }

                final Set<Agent> allNodes = getAllNodes( config );

                Command stopCommand = Commands.getStopCommand( allNodes );
                commandRunner.runCommand( stopCommand );

                if ( stopCommand.hasSucceeded() )
                {
                    po.addLogDone( "Start success.." );
                }
                else
                {
                    po.addLogFailed( String.format( "Start failed, %s", stopCommand.getAllErrors() ) );
                    return;
                }

            }
        } );

        return po.getId();
    }


    @Override
    public UUID checkCluster( final String clusterName )
    {
        final ProductOperation po
            = tracker.createProductOperation( HBaseConfig.PRODUCT_KEY,
            String.format( "Checking cluster %s", clusterName ) );
        executor.execute( new Runnable()
        {

            public void run()
            {
                HBaseConfig config = dbManager.getInfo( HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class );
                if ( config == null )
                {
                    po.addLogFailed( String
                        .format( "Cluster with name %s does not exist\nOperation aborted", config.getClusterName() ) );
                    return;
                }

                final Set<Agent> allNodes = getAllNodes( config );

                Command checkCommand = Commands.getStatusCommand( allNodes );
                commandRunner.runCommand( checkCommand );

                if ( checkCommand.hasSucceeded() )
                {
                    po.addLogDone( "All nodes are running.." );
                }
                else
                {
                    po.addLogFailed( String.format( "Start failed, %s", checkCommand.getAllErrors() ) );
                    return;
                }

            }
        } );

        return po.getId();
    }


    private Set<Agent> getAllNodes( HBaseConfig config )
    {
        final Set<Agent> allNodes = new HashSet<Agent>();

        allNodes.add( agentManager.getAgentByHostname( config.getMaster() ) );
        allNodes.add( agentManager.getAgentByHostname( config.getBackupMasters() ) );

        for ( String hostname : config.getRegion() )
        {
            allNodes.add( agentManager.getAgentByHostname( hostname ) );
        }

        for ( String hostname : config.getQuorum() )
        {
            allNodes.add( agentManager.getAgentByHostname( hostname ) );
        }

        return allNodes;
    }

}
