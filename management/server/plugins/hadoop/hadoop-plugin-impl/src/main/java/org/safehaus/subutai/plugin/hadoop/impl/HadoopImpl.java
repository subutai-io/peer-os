package org.safehaus.subutai.plugin.hadoop.impl;


import org.safehaus.subutai.core.agentmanager.api.AgentManager;
import org.safehaus.subutai.core.commandrunner.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.*;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.impl.operation.Adding;
import org.safehaus.subutai.plugin.hadoop.impl.operation.Deletion;
import org.safehaus.subutai.plugin.hadoop.impl.operation.Installation;
import org.safehaus.subutai.plugin.hadoop.impl.operation.configuration.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HadoopImpl implements Hadoop
{
    public static final int INITIAL_CAPACITY = 2;

    private static CommandRunner commandRunner;
    private static AgentManager agentManager;
    private static DbManager dbManager;
    private static Tracker tracker;
    private static ContainerManager containerManager;
    private static NetworkManager networkManager;
    private static ExecutorService executor;
    private static EnvironmentManager environmentManager;


    public HadoopImpl( AgentManager agentManager, Tracker tracker, CommandRunner commandRunner, DbManager dbManager,
        NetworkManager networkManager, ContainerManager containerManager,
        EnvironmentManager environmentManager )
    {

        HadoopImpl.agentManager = agentManager;
        HadoopImpl.tracker = tracker;
        HadoopImpl.commandRunner = commandRunner;
        HadoopImpl.dbManager = dbManager;
        HadoopImpl.networkManager = networkManager;
        HadoopImpl.containerManager = containerManager;
        HadoopImpl.environmentManager = environmentManager;
    }


    public static void init()
    {
        executor = Executors.newCachedThreadPool();
    }


    public static void destroy()
    {
        executor.shutdown();
        commandRunner = null;
    }


    public static CommandRunner getCommandRunner()
    {
        return commandRunner;
    }


    public static DbManager getDbManager()
    {
        return dbManager;
    }


    public static Tracker getTracker()
    {
        return tracker;
    }


    public static ContainerManager getContainerManager()
    {
        return containerManager;
    }


    public static NetworkManager getNetworkManager()
    {
        return networkManager;
    }


    public static ExecutorService getExecutor()
    {
        return executor;
    }


    public static AgentManager getAgentManager()
    {
        return agentManager;
    }


    public static EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    @Override
    public UUID installCluster( final HadoopClusterConfig hadoopClusterConfig )
    {
        return new Installation( this, hadoopClusterConfig ).execute();
    }


    @Override
    public UUID uninstallCluster( final String clusterName )
    {
        return new Deletion( this ).execute( clusterName );
    }


    @Override
    public List<HadoopClusterConfig> getClusters()
    {
        return dbManager.getInfo( HadoopClusterConfig.PRODUCT_KEY, HadoopClusterConfig.class );
    }


    @Override
    public HadoopClusterConfig getCluster( String clusterName )
    {
        return dbManager.getInfo( HadoopClusterConfig.PRODUCT_KEY, clusterName, HadoopClusterConfig.class );
    }


    @Override
    public UUID startNameNode( HadoopClusterConfig hadoopClusterConfig )
    {
        return new NameNode( this, hadoopClusterConfig ).start();
    }


    @Override
    public UUID stopNameNode( HadoopClusterConfig hadoopClusterConfig )
    {
        return new NameNode( this, hadoopClusterConfig ).stop();
    }


    @Override
    public UUID restartNameNode( HadoopClusterConfig hadoopClusterConfig )
    {
        return new NameNode( this, hadoopClusterConfig ).restart();
    }


    @Override
    public UUID statusNameNode( HadoopClusterConfig hadoopClusterConfig )
    {
        return new NameNode( this, hadoopClusterConfig ).status();
    }


    @Override
    public UUID statusSecondaryNameNode( HadoopClusterConfig hadoopClusterConfig )
    {
        return new SecondaryNameNode( this, hadoopClusterConfig ).status();
    }


    @Override
    public UUID statusDataNode( Agent agent )
    {
        return new DataNode( this, null ).status( agent );
    }


    @Override
    public UUID startJobTracker( HadoopClusterConfig hadoopClusterConfig )
    {
        return new JobTracker( this, hadoopClusterConfig ).start();
    }


    @Override
    public UUID stopJobTracker( HadoopClusterConfig hadoopClusterConfig )
    {
        return new JobTracker( this, hadoopClusterConfig ).stop();
    }


    @Override
    public UUID restartJobTracker( HadoopClusterConfig hadoopClusterConfig )
    {
        return new JobTracker( this, hadoopClusterConfig ).restart();
    }


    @Override
    public UUID statusJobTracker( HadoopClusterConfig hadoopClusterConfig )
    {
        return new JobTracker( this, hadoopClusterConfig ).status();
    }


    @Override
    public UUID statusTaskTracker( Agent agent )
    {
        return new TaskTracker( this, null ).status( agent );
    }


    @Override
    public UUID addNode( String clusterName )
    {
        return new Adding( this, clusterName ).execute();
    }


    @Override
    public UUID blockDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        return new DataNode( this, hadoopClusterConfig ).block( agent );
    }


    @Override
    public UUID blockTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        return new TaskTracker( this, hadoopClusterConfig ).block( agent );
    }


    @Override
    public UUID unblockDataNode( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        return new DataNode( this, hadoopClusterConfig ).unblock( agent );
    }


    @Override
    public UUID unblockTaskTracker( HadoopClusterConfig hadoopClusterConfig, Agent agent )
    {
        return new TaskTracker( this, hadoopClusterConfig ).unblock( agent );
    }


    @Override
    public ClusterSetupStrategy getClusterSetupStrategy( ProductOperation po,
        HadoopClusterConfig hadoopClusterConfig )
    {
        return new HadoopDbSetupStrategy( po, this, hadoopClusterConfig );
    }


    public ClusterSetupStrategy getClusterSetupStrategy( ProductOperation po,
        HadoopClusterConfig hadoopClusterConfig,
        Environment environment )
    {
        return new HadoopDbSetupStrategy( po, this, hadoopClusterConfig, environment );
    }


    @Override
    public EnvironmentBlueprint getDefaultEnvironmentBlueprint( final HadoopClusterConfig config ) throws
        ClusterSetupException
    {
        EnvironmentBlueprint environmentBlueprint = new EnvironmentBlueprint();
        environmentBlueprint.setName( String.format( "%s-%s", HadoopClusterConfig.PRODUCT_KEY, UUID.randomUUID() ) );
        environmentBlueprint.setLinkHosts( true );
        environmentBlueprint.setExchangeSshKeys( true );
        environmentBlueprint.setDomainName( Common.DEFAULT_DOMAIN_NAME );
        Set<NodeGroup> nodeGroups = new HashSet<>( INITIAL_CAPACITY );

        //hadoop master nodes
        NodeGroup mastersGroup = new NodeGroup();
        mastersGroup.setName( NodeType.MASTER_NODE.name() );
        mastersGroup.setNumberOfNodes( HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY );
        mastersGroup.setTemplateName( config.getTemplateName() );
        mastersGroup.setPlacementStrategy( PlacementStrategy.MORE_RAM );
        mastersGroup.setPhysicalNodes( convertAgent2Hostname() );
        nodeGroups.add( mastersGroup );

        //hadoop slave nodes
        NodeGroup slavesGroup = new NodeGroup();
        slavesGroup.setName( NodeType.SLAVE_NODE.name() );
        slavesGroup.setNumberOfNodes( config.getCountOfSlaveNodes() );
        slavesGroup.setTemplateName( config.getTemplateName() );
        slavesGroup.setPlacementStrategy( PlacementStrategy.MORE_HDD );
        slavesGroup.setPhysicalNodes( convertAgent2Hostname() );
        nodeGroups.add( slavesGroup );

        environmentBlueprint.setNodeGroups( nodeGroups );

        return environmentBlueprint;
    }


    private Set<String> convertAgent2Hostname() throws ClusterSetupException
    {
        Set<Agent> agents = agentManager.getPhysicalAgents();

        if ( agents != null && !agents.isEmpty() )
        {
            Set<String> hostNames = new HashSet<>( agents.size() );

            for ( Agent agent : agents )
            {
                hostNames.add( agent.getHostname() );
            }

            return hostNames;
        }
        else
        {
            throw new ClusterSetupException( "No physical machines available" );
        }
    }
}
