package org.safehaus.subutai.plugin.hadoop.impl;


import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;


/**
 * This is a hadoop cluster init strategy.
 */
public class HadoopSetupStrategy implements ClusterSetupStrategy
{

    private Environment environment;
    private HadoopImpl hadoopManager;
    private TrackerOperation trackerOperation;
    private HadoopClusterConfig hadoopClusterConfig;


//    public HadoopSetupStrategy( TrackerOperation po, HadoopImpl hadoopManager, HadoopClusterConfig hadoopClusterConfig )
//    {
//        Preconditions.checkNotNull( hadoopClusterConfig, "Hadoop cluster config is null" );
//        Preconditions.checkNotNull( po, "Product operation tracker is null" );
//        Preconditions.checkNotNull( hadoopManager, "Hadoop manager is null" );
//
//        this.hadoopManager = hadoopManager;
//        this.trackerOperation = po;
//        this.hadoopClusterConfig = hadoopClusterConfig;
//    }


    public HadoopSetupStrategy( Environment environment, HadoopClusterConfig hadoopClusterConfig,
                                TrackerOperation po, HadoopImpl hadoopManager )
    {
        Preconditions.checkNotNull( hadoopClusterConfig, "Hadoop cluster config is null" );
        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( hadoopManager, "Hadoop manager is null" );

        this.hadoopManager = hadoopManager;
        this.trackerOperation = po;
        this.hadoopClusterConfig = hadoopClusterConfig;
        this.environment = environment;
    }


    //    public static PlacementStrategy getNodePlacementStrategyByNodeType( NodeType nodeType )
    //    {
    //        switch ( nodeType )
    //        {
    //            case MASTER_NODE:
    //                return PlacementStrategy.MORE_RAM;
    //            case SLAVE_NODE:
    //                return PlacementStrategy.MORE_HDD;
    //            default:
    //                return PlacementStrategy.ROUND_ROBIN;
    //        }
    //    }


    @Override
    public HadoopClusterConfig setup() throws ClusterSetupException
    {
        try
        {
            trackerOperation.addLog( String.format( "Creating %d servers...", hadoopClusterConfig.getCountOfSlaveNodes()
                    + HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY ) );

            if ( this.environment == null )
            {
                environment = hadoopManager.getEnvironmentManager().buildEnvironment(
                        hadoopManager.getDefaultEnvironmentBlueprint( hadoopClusterConfig ) );
            }

            setMasterNodes();
            setSlaveNodes();
            trackerOperation.addLog( "Lxc containers created successfully" );

            try
            {
                new ClusterConfiguration( trackerOperation, hadoopManager ).configureCluster( hadoopClusterConfig,
                        environment );
            }
            catch ( ClusterConfigurationException e )
            {
                throw new ClusterSetupException( e.getMessage() );
            }
        }
        catch ( EnvironmentBuildException e )
        {
            e.printStackTrace();
        }
        return hadoopClusterConfig;
    }


    protected void setMasterNodes() throws ClusterSetupException
    {
        Set<UUID> masterNodes = new HashSet<>();
        int masterCount = 0;
        for ( ContainerHost containerHost : this.environment.getContainerHosts() )
        {
            if ( masterCount < HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY )
            {
                masterNodes.add( containerHost.getId() );
                masterCount++;
            }
        }

        if ( masterNodes.size() != HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY )
        {
            throw new ClusterSetupException( String.format( "Hadoop master nodes must be %d in count",
                    HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY ) );
        }

        Iterator<UUID> masterIterator = masterNodes.iterator();
        hadoopClusterConfig.setNameNode( masterIterator.next() );
        hadoopClusterConfig.setJobTracker( masterIterator.next() );
        hadoopClusterConfig.setSecondaryNameNode( masterIterator.next() );
    }


    protected void setSlaveNodes() throws ClusterSetupException
    {
        Set<UUID> slaveNodes = new HashSet<>();
        for ( ContainerHost containerHost : environment.getContainerHosts() )
        {
            if ( !hadoopClusterConfig.getAllMasterNodes().contains( containerHost.getId() ) )
            {
                slaveNodes.add( containerHost.getId() );
            }
        }
        if ( slaveNodes.isEmpty() )
        {
            throw new ClusterSetupException( "Hadoop slave nodes are empty" );
        }
        hadoopClusterConfig.setDataNodes( Lists.newArrayList( slaveNodes ) );
        hadoopClusterConfig.setTaskTrackers( Lists.newArrayList( slaveNodes ) );
    }
}
