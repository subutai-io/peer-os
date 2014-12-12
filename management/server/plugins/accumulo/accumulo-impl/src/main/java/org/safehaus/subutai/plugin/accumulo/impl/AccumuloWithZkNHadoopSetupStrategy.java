package org.safehaus.subutai.plugin.accumulo.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Accumulo cluster setup strategy using environment
 */
public class AccumuloWithZkNHadoopSetupStrategy implements ClusterSetupStrategy
{

    private final Environment environment;
    private final AccumuloImpl accumuloManager;
    private final TrackerOperation po;
    private final AccumuloClusterConfig accumuloClusterConfig;


    public AccumuloWithZkNHadoopSetupStrategy( final Environment environment,
                                               final AccumuloClusterConfig accumuloClusterConfig,
                                               final TrackerOperation po, final AccumuloImpl accumuloManager )
    {

        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( accumuloClusterConfig, "Accumulo cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( accumuloManager, "Accumulo manager is null" );

        this.environment = environment;
        this.po = po;
        this.accumuloManager = accumuloManager;
        this.accumuloClusterConfig = accumuloClusterConfig;
    }


    @Override
    public AccumuloClusterConfig setup() throws ClusterSetupException
    {
        if ( Strings.isNullOrEmpty( accumuloClusterConfig.getClusterName() ) ||
                Strings.isNullOrEmpty( accumuloClusterConfig.getInstanceName() ) ||
                Strings.isNullOrEmpty( accumuloClusterConfig.getPassword() ) )
        {
            po.addLogFailed( "Malformed configuration" );
        }

        if ( accumuloManager.getCluster( accumuloClusterConfig.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", accumuloClusterConfig.getClusterName() ) );
        }

        HadoopClusterConfig hadoopClusterConfig =
                accumuloManager.getHadoopManager().getCluster( accumuloClusterConfig.getHadoopClusterName() );
        if ( hadoopClusterConfig == null )
        {
            throw new ClusterSetupException( String.format( "Hadoop cluster with name '%s' not found",
                    accumuloClusterConfig.getHadoopClusterName() ) );
        }

        ZookeeperClusterConfig zookeeperClusterConfig =
                accumuloManager.getZkManager().getCluster( accumuloClusterConfig.getZookeeperClusterName() );
        if ( zookeeperClusterConfig == null )
        {
            throw new ClusterSetupException( String.format( "Zookeeper cluster with name '%s' not found",
                    accumuloClusterConfig.getZookeeperClusterName() ) );
        }

        /*

        //get ZK nodes with Hadoop installed from environment
        Set<Agent> accumuloAgents = new HashSet<>();
        for ( EnvironmentContainer environmentContainer : environment.getContainerHosts() )
        {
            if ( environmentContainer.getTemplate().getProducts()
                                     .contains( Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_NAME )
                    && environmentContainer.getTemplate().getProducts()
                                           .contains( Common.PACKAGE_PREFIX + HadoopClusterConfig
                                                   .PRODUCT_NAME ) )
            {
                accumuloAgents.add( environmentContainer.getAgent() );
            }
        }

        int numberOfNeededAccumuloNodes =
                AccumuloClusterConfig.DEFAULT_ACCUMULO_MASTER_NODES_QUANTITY + accumuloClusterConfig
                        .getNumberOfTracers() + accumuloClusterConfig.getNumberOfSlaves();

        if ( numberOfNeededAccumuloNodes > accumuloAgents.size() )
        {
            throw new ClusterSetupException( String.format(
                    "Number of needed Accumulo nodes (%d) exceeds number of available nodes with Hadoop
                    installed (%d)",
                    numberOfNeededAccumuloNodes, accumuloAgents.size() ) );
        }

        Set<Agent> accumuloTracerNodes = new HashSet<>();
        Set<Agent> accumuloSlaveNodes = new HashSet<>();

        Iterator<Agent> agentIterator = accumuloAgents.iterator();

        accumuloClusterConfig.setMasterNode( agentIterator.next() );
        accumuloClusterConfig.setGcNode( agentIterator.next() );
        accumuloClusterConfig.setMonitor( agentIterator.next() );
        for ( int i = 0; i < accumuloClusterConfig.getNumberOfTracers(); i++ )
        {
            accumuloTracerNodes.add( agentIterator.next() );
        }
        for ( int i = 0; i < accumuloClusterConfig.getNumberOfSlaves(); i++ )
        {
            accumuloSlaveNodes.add( agentIterator.next() );
        }

        accumuloClusterConfig.setTracers( accumuloTracerNodes );
        accumuloClusterConfig.setSlaves( accumuloSlaveNodes );

        try
        {
            new ClusterConfiguration( po, accumuloManager )
                    .configureCluster( accumuloClusterConfig, zookeeperClusterConfig );
        }
        catch ( ClusterConfigurationException e )
        {
            throw new ClusterSetupException( e.getMessage() );
        }

        */

        return accumuloClusterConfig;
    }
}
