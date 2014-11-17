package org.safehaus.subutai.plugin.accumulo.impl;


import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * This is an accumulo cluster setup strategy over existing Hadoop & ZK clusters
 */
public class AccumuloOverZkNHadoopSetupStrategy implements ClusterSetupStrategy
{


    private final AccumuloImpl accumuloManager;
    private final TrackerOperation trackerOperation;
    private final AccumuloClusterConfig accumuloClusterConfig;
    private final HadoopClusterConfig hadoopClusterConfig;
    private final Environment environment;


    public AccumuloOverZkNHadoopSetupStrategy( final Environment environment,
                                               final AccumuloClusterConfig accumuloClusterConfig,
                                               final HadoopClusterConfig hadoopClusterConfig,
                                               final TrackerOperation trackerOperation,
                                               final AccumuloImpl accumuloManager )
    {
        Preconditions.checkNotNull( accumuloClusterConfig, "Accumulo cluster config is null" );
        Preconditions.checkNotNull( trackerOperation, "Product operation tracker is null" );
        Preconditions.checkNotNull( accumuloManager, "Accumulo manager is null" );

        this.trackerOperation = trackerOperation;
        this.accumuloClusterConfig = accumuloClusterConfig;
        this.hadoopClusterConfig = hadoopClusterConfig;
        this.accumuloManager = accumuloManager;
        this.environment = environment;
    }


    @Override
    public AccumuloClusterConfig setup() throws ClusterSetupException
    {
        if ( accumuloClusterConfig.getMasterNode() == null || accumuloClusterConfig.getGcNode() == null
                || accumuloClusterConfig.getMonitor() == null ||
                Strings.isNullOrEmpty( accumuloClusterConfig.getClusterName() ) ||
                CollectionUtil.isCollectionEmpty( accumuloClusterConfig.getTracers() ) ||
                CollectionUtil.isCollectionEmpty( accumuloClusterConfig.getSlaves() ) ||
                Strings.isNullOrEmpty( accumuloClusterConfig.getInstanceName() ) ||
                Strings.isNullOrEmpty( accumuloClusterConfig.getPassword() ) )
        {
            throw new ClusterSetupException( "Malformed configuration" );
        }

        if ( accumuloManager.getCluster( accumuloClusterConfig.getClusterName() ) != null )
        {
            trackerOperation.addLogFailed( "There is already a cluster with that name" );
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
            throw new ClusterSetupException( String.format( "ZK cluster with name '%s' not found",
                    accumuloClusterConfig.getZookeeperClusterName() ) );
        }


        if ( !hadoopClusterConfig.getAllNodes().containsAll( accumuloClusterConfig.getAllNodes() ) )
        {
            throw new ClusterSetupException( String.format( "Not all supplied nodes belong to Hadoop cluster %s",
                    hadoopClusterConfig.getClusterName() ) );
        }


         /** start hadoop and zk clusters */
        accumuloManager.getHadoopManager().startNameNode( hadoopClusterConfig );

         /** start Zookeeper cluster */
        for ( UUID node : zookeeperClusterConfig.getNodes() )
        {
            accumuloManager.getZkManager().startNode( zookeeperClusterConfig.getClusterName(),
                    environment.getContainerHostByUUID( node ).getHostname() );
        }

        trackerOperation.addLog( "Installing Accumulo..." );
        for ( UUID uuid : accumuloClusterConfig.getAllNodes() )
        {
            CommandResult result;
            ContainerHost host = environment.getContainerHostByUUID( uuid );
            if ( checkIfProductIsInstalled( host, HadoopClusterConfig.PRODUCT_NAME ) )
            {
                if ( !checkIfProductIsInstalled( host, AccumuloClusterConfig.PRODUCT_NAME ) )
                {
                    try
                    {
                        result = host.execute( new RequestBuilder(
                                Commands.installCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY
                                        .toLowerCase() ).withTimeout( 1800 ) );
                        if ( result.hasSucceeded() )
                        {
                            trackerOperation.addLog(
                                    AccumuloClusterConfig.PRODUCT_KEY + " is installed on node " + host.getHostname() );
                        }
                        else
                        {
                            trackerOperation.addLogFailed(
                                    AccumuloClusterConfig.PRODUCT_KEY + " is not installed on node " + host
                                            .getTemplateName() );
                        }
                    }
                    catch ( CommandException e )
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    trackerOperation
                            .addLog( String.format( "Node %s already has Accumulo installed", host.getHostname() ) );
                }
            }
            else
            {
                throw new ClusterSetupException(
                        String.format( "Node %s has no Hadoop installation", host.getHostname() ) );
            }
        }

        try
        {
            new ClusterConfiguration( accumuloManager, trackerOperation )
                    .configureCluster( environment, accumuloClusterConfig, zookeeperClusterConfig );
        }
        catch ( ClusterConfigurationException e )
        {
            throw new ClusterSetupException( e.getMessage() );
        }
        return accumuloClusterConfig;
    }


    private boolean checkIfProductIsInstalled( ContainerHost containerHost, String productName )
    {
        boolean isInstalled = false;
        try
        {
            CommandResult result = containerHost.execute( new RequestBuilder( Commands.checkIfInstalled ) );
            if ( result.getStdOut().toLowerCase().contains( productName.toLowerCase() ) )
            {
                isInstalled = true;
            }
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
        return isInstalled;
    }
}
