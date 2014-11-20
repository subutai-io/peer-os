package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;

import com.google.common.base.Strings;


/**
 * Sets up Zookeeper cluster either a standalone ZK cluster or over hadoop cluster nodes or together with hadoop
 */
public class InstallOperationHandler extends AbstractOperationHandler<ZookeeperImpl>
{
    private final ZookeeperClusterConfig config;
    private HadoopClusterConfig hadoopClusterConfig;


    public InstallOperationHandler( final ZookeeperImpl manager, ZookeeperClusterConfig config,
                                    final HadoopClusterConfig hadoopClusterConfig )
    {
        this( manager, config );
        this.hadoopClusterConfig = hadoopClusterConfig;
    }


    public InstallOperationHandler( ZookeeperImpl manager, ZookeeperClusterConfig config )
    {

        super( manager, config.getClusterName() );
        this.config = config;
        trackerOperation = manager.getTracker().createTrackerOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", config.getClusterName() ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return trackerOperation.getId();
    }


    @Override
    public void run()
    {
        if ( Strings.isNullOrEmpty( config.getClusterName() )
                //either number of nodes to create or hadoop cluster nodes must be present
                || ( config.getSetupType() == SetupType.STANDALONE && config.getNumberOfNodes() <= 0 ) || (
                config.getSetupType() == SetupType.OVER_HADOOP && CollectionUtil.isCollectionEmpty( config.getNodes() )
                        && Strings.isNullOrEmpty( config.getHadoopClusterName() ) ) ||
                ( config.getSetupType() == SetupType.WITH_HADOOP && hadoopClusterConfig == null ) )
        {
            trackerOperation.addLogFailed( "Malformed configuration" );
            return;
        }

        if ( manager.getCluster( clusterName ) != null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name '%s' already exists", clusterName ) );
            return;
        }

        if ( config.getSetupType() == SetupType.STANDALONE )
        {
            setupStandalone();
        }
        else if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            setupOverHadoop();
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            setupWithHadoop();
        }
        else
        {
            trackerOperation.addLogFailed( "Wrong setup type !" );
            return;
        }
    }


    /**
     * Sets up ZK cluster over supplied Hadoop cluster nodes
     */
    private void setupOverHadoop()
    {

        try
        {
            //setup ZK cluster
            ClusterSetupStrategy zkClusterSetupStrategy =
                    manager.getClusterSetupStrategy( null, config, trackerOperation );
            zkClusterSetupStrategy.setup();

            trackerOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( ClusterSetupException e )
        {
            trackerOperation.addLogFailed(
                    String.format( "Failed to setup an over-Hadoop ZK cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }


    /**
     * Sets up a standalone Zk cluster
     */
    private void setupStandalone()
    {

        try
        {
            //create environment
            Environment env = manager.getEnvironmentManager()
                                     .buildEnvironment( manager.getDefaultEnvironmentBlueprint( config ) );

            //setup ZK cluster
            ClusterSetupStrategy zkClusterSetupStrategy =
                    manager.getClusterSetupStrategy( env, config, trackerOperation );
            zkClusterSetupStrategy.setup();

            trackerOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e )
        {
            trackerOperation.addLogFailed(
                    String.format( "Failed to setup a standalone ZK cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }


    /**
     * Sets up ZK cluster together with Hadoop cluster
     */
    private void setupWithHadoop()
    {

        try
        {

            final String COMBO_TEMPLATE_NAME = "hadoopnzk";
            hadoopClusterConfig.setTemplateName( COMBO_TEMPLATE_NAME );
            //create environment
            Environment env = manager.getEnvironmentManager().buildEnvironment(
                    manager.getHadoopManager().getDefaultEnvironmentBlueprint( hadoopClusterConfig ) );

            //setup Hadoop cluster
            ClusterSetupStrategy hadoopClusterSetupStrategy =
                    manager.getHadoopManager().getClusterSetupStrategy( trackerOperation, hadoopClusterConfig, env );
            hadoopClusterSetupStrategy.setup();

            //setup ZK cluster
            ClusterSetupStrategy zkClusterSetupStrategy =
                    manager.getClusterSetupStrategy( env, config, trackerOperation );
            zkClusterSetupStrategy.setup();
            config.setEnvironmentId( env.getId() );

            trackerOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e )
        {
            trackerOperation.addLogFailed(
                    String.format( "Failed to setup a standalone ZK cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }


    public HadoopClusterConfig getHadoopClusterConfig()
    {
        return hadoopClusterConfig;
    }


    public void setHadoopClusterConfig( HadoopClusterConfig hadoopClusterConfig )
    {
        this.hadoopClusterConfig = hadoopClusterConfig;
    }
}
