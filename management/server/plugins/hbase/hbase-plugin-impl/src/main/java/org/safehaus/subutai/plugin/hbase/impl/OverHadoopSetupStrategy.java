package org.safehaus.subutai.plugin.hbase.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;


public class OverHadoopSetupStrategy extends SetupBase implements ClusterSetupStrategy
{

    public OverHadoopSetupStrategy( HBaseImpl manager, ProductOperation po, HBaseClusterConfig config )
    {
        super( po, manager, config );
    }

    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        check();
        configure();
        return config;
    }


//    @Override
//    public ConfigBase setup() throws ClusterSetupException
//    {
//        if ( manager.getCluster( config.getClusterName() ) != null )
//        {
//            throw new ClusterSetupException( "Cluster already exists: " + config.getClusterName() );
//        }
//
//        Set<Agent> allNodes;
//        allNodes = getAllNodes( config );
//
//        if ( manager.getAgentManager().getAgentByHostname( config.getHadoopNameNode() ) == null )
//        {
//            po.addLogFailed( String.format( "Hadoop NameNode %s not connected", config.getHadoopNameNode() ) );
//            throw new ClusterSetupException( "Hadoop Name Node is not connected: " + config.getHadoopNameNode() );
//        }
//
//        // Installing HBase
//        po.addLog( "Installing HBase on ..." );
//
//
//        Command installCommand = Commands.getInstallCommand( allNodes );
//        manager.getCommandRunner().runCommand( installCommand );
//
//        if ( installCommand.hasSucceeded() )
//        {
//            po.addLog( "Installation HBase successful.." );
//        }
//        else
//        {
//            po.addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
//            throw new ClusterSetupException( "Could not install HBase on nodes !");
//        }
//
//        po.addLog( "Installation succeeded\nConfiguring master..." );
//
//        // Configuring master
//        Command configureMasterCommand = Commands.getConfigMasterCommand( allNodes,
//                manager.getAgentManager().getAgentByHostname( config.getHadoopNameNode() ).getHostname(),
//                config.getHbaseMaster().getHostname() );
//        manager.getCommandRunner().runCommand( configureMasterCommand );
//
//        if ( configureMasterCommand.hasSucceeded() )
//        {
//            po.addLog( "Configure master successful..." );
//        }
//        else
//        {
//            po.addLogFailed( String.format( "Configuration failed, %s", configureMasterCommand ) );
//            throw new ClusterSetupException( "Configuration of master failed !");
//
//        }
//        po.addLog( "Configuring master succeeded\nConfiguring region..." );
//
//        // Configuring region
//        StringBuilder sbRegion = new StringBuilder();
//        for ( Agent agent : config.getRegionServers() )
//        {
//            sbRegion.append( agent.getHostname() );
//            sbRegion.append( " " );
//        }
//        Command configureRegionCommand =
//                Commands.getConfigRegionCommand( allNodes, sbRegion.toString().trim() );
//        manager.getCommandRunner().runCommand( configureRegionCommand );
//
//        if ( configureRegionCommand.hasSucceeded() )
//        {
//            po.addLog( "Configuring region success..." );
//        }
//        else
//        {
//            po.addLogFailed(
//                    String.format( "Configuring failed, %s", configureRegionCommand.getAllErrors() ) );
//            throw new ClusterSetupException( "Configuration of region servers failed !");
//        }
//        po.addLog( "Configuring region succeeded\nSetting quorum..." );
//
//        // Configuring quorum
//        StringBuilder sbQuorum = new StringBuilder();
//        for ( Agent agent : config.getQuorumPeers() )
//        {
//            sbQuorum.append( agent.getHostname() );
//            sbQuorum.append( " " );
//        }
//        Command configureQuorumCommand =
//                Commands.getConfigQuorumCommand( allNodes, sbQuorum.toString().trim() );
//        manager.getCommandRunner().runCommand( configureQuorumCommand );
//
//        if ( configureQuorumCommand.hasSucceeded() )
//        {
//            po.addLog( "Configuring quorum success..." );
//        }
//        else
//        {
//            po.addLogFailed(
//                    String.format( "Installation failed, %s", configureQuorumCommand.getAllErrors() ) );
//            throw new ClusterSetupException( "Configuration of quorum peers failed !");
//        }
//        po.addLog( "Setting quorum succeeded\nSetting backup masters..." );
//
//        // Configuring backup master
//        StringBuilder sbBackUpMasters = new StringBuilder();
//        for ( Agent agent : config.getBackupMasters() )
//        {
//            sbBackUpMasters.append( agent.getHostname() );
//            sbBackUpMasters.append( " " );
//        }
//        Command configureBackupMasterCommand =
//                Commands.getConfigBackupMastersCommand( allNodes, sbBackUpMasters.toString().trim() );
//        manager.getCommandRunner().runCommand( configureBackupMasterCommand );
//
//        if ( configureBackupMasterCommand.hasSucceeded() )
//        {
//            po.addLog( "Configuring backup masters success..." );
//        }
//        else
//        {
//            po.addLogFailed( String.format( "Installation failed, %s",
//                    configureBackupMasterCommand.getAllErrors() ) );
//            throw new ClusterSetupException( "Configuration of backup master failed !");
//        }
//        po.addLog( "Cluster installation succeeded\n" );
//
//        manager.getPluginDAO().saveInfo( HBaseClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
//        po.addLog( "Cluster info saved to DB\nInstalling HBase..." );
//
//
//        return config;
//    }


    private void check() throws ClusterSetupException
    {

        String m = "Malformed configuration: ";
        if ( config.getClusterName() == null || config.getClusterName().isEmpty() )
        {
            throw new ClusterSetupException( m + "cluster name not specified" );
        }
        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    m + String.format( "cluster %s already exists", config.getClusterName() ) );
        }

        if ( config.getHbaseMaster() == null )
        {
            throw new ClusterSetupException( m + "master node not specified" );
        }

        if ( config.getRegionServers().isEmpty() )
        {
            throw new ClusterSetupException( m + "no region server nodes" );
        }

        if ( config.getQuorumPeers().isEmpty() )
        {
            throw new ClusterSetupException( m + "no quorum nodes" );
        }

        if ( config.getBackupMasters().isEmpty() )
        {
            throw new ClusterSetupException( m + "no backup master nodes" );
        }

        // check if nodes are connected
        if ( manager.agentManager.getAgentByHostname( config.getHbaseMaster().getHostname() ) == null )
        {
            throw new ClusterSetupException( "Master node is not connected" );
        }

        for ( Agent a : config.getRegionServers() )
        {
            if ( manager.agentManager.getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException( "Not all region server are connected" );
            }
        }

        for ( Agent a : config.getQuorumPeers() )
        {
            if ( manager.agentManager.getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException( "Not all quorum peer nodes are connected" );
            }
        }

        for ( Agent a : config.getBackupMasters() )
        {
            if ( manager.agentManager.getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException( "Not all backup master nodes are connected" );
            }
        }

        // check Hadoop cluster
        HadoopClusterConfig hc = manager.hadoopManager.getCluster( config.getHadoopClusterName() );
        if ( hc == null )
        {
            throw new ClusterSetupException( "Could not find Hadoop cluster " + config.getHadoopClusterName() );
        }
        if ( !hc.getAllNodes().containsAll( config.getAllNodes() ) )
        {
            throw new ClusterSetupException(
                    "Not all nodes belong to Hadoop cluster " + config.getHadoopClusterName() );
        }
        config.setHadoopNodes( new HashSet<>( hc.getAllNodes() ) );

        po.addLog( "Checking prerequisites..." );

        //check installed ksks packages
        Set<Agent> allNodes = config.getAllNodes();
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( allNodes );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() )
        {
            throw new ClusterSetupException(
                    "Failed to check presence of installed ksks packages\nInstallation aborted" );
        }
        for ( Iterator<Agent> it = allNodes.iterator(); it.hasNext(); )
        {
            Agent node = it.next();

            AgentResult result = checkInstalledCommand.getResults().get( node.getUuid() );
            if ( result.getStdOut().contains( Commands.PACKAGE_NAME ) )
            {
                po.addLog( String.format( "Node %s already has HBase installed. Omitting this node from installation",
                        node.getHostname() ) );
                config.getAllNodes().remove( node );
                it.remove();
            }
            else if ( !result.getStdOut().contains( "ksks-hadoop" ) )
            {
                po.addLog( String.format( "Node %s has no Hadoop installation. Omitting this node from installation",
                        node.getHostname() ) );
                config.getAllNodes().remove( node );
                it.remove();
            }
        }

        if ( config.getRegionServers().isEmpty() || config.getQuorumPeers().isEmpty() || config.getBackupMasters().isEmpty() )
        {
            throw new ClusterSetupException( "No nodes eligible for installation\nInstallation aborted" );
        }
        if ( !allNodes.contains( config.getHbaseMaster() ) )
        {
            throw new ClusterSetupException( "Master node was omitted\nInstallation aborted" );
        }
    }

    private void configure() throws ClusterSetupException
    {
        po.addLog( "Updating db..." );
        //save to db
        manager.getPluginDAO().saveInfo( HBaseClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
        po.addLog( "Cluster info saved to DB\nInstalling HBase..." );
        //install hbase
        Command installCommand = Commands.getInstallCommand( config.getAllNodes() );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasSucceeded() )
        {
            po.addLog( "Installation succeeded" );

            SetupHelper helper = new SetupHelper( manager, config, po );
            helper.configureHMaster();
            helper.configureRegionServers();
            helper.configureQuorumPeers();
            helper.configureBackUpMasters();
        }
        else
        {
            throw new ClusterSetupException( "Installation failed: " + installCommand.getAllErrors() );
        }
    }
}
