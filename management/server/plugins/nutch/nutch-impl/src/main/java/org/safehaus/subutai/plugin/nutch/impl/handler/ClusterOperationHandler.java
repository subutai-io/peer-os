package org.safehaus.subutai.plugin.nutch.impl.handler;


import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.ClusterOperationHandlerInterface;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.nutch.api.NutchConfig;
import org.safehaus.subutai.plugin.nutch.api.SetupType;
import org.safehaus.subutai.plugin.nutch.impl.Commands;
import org.safehaus.subutai.plugin.nutch.impl.NutchImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Created by ebru on 10.11.2014.
 */
public class ClusterOperationHandler extends AbstractOperationHandler<NutchImpl, NutchConfig>
        implements ClusterOperationHandlerInterface
{
    private static final Logger LOG = LoggerFactory.getLogger( ClusterOperationHandler.class.getName() );
    private ClusterOperationType operationType;
    private NutchConfig config;
    private HadoopClusterConfig hadoopConfig;
    private ExecutorService executor = Executors.newCachedThreadPool();

    public ClusterOperationHandler( final NutchImpl manager, final NutchConfig config,
                                    final ClusterOperationType operationType )
    {
        super( manager, config );
        this.operationType = operationType;
        this.config = config;
        trackerOperation = manager.getTracker().createTrackerOperation( NutchConfig.PRODUCT_KEY,
                String.format( "Creating %s tracker object...", clusterName ) );
    }

    public void setHadoopConfig( HadoopClusterConfig hadoopConfig )
    {
        this.hadoopConfig = hadoopConfig;
    }

    @Override
    public void runOperationOnContainers( final ClusterOperationType clusterOperationType )
    {

    }


    @Override
    public void setupCluster()
    {
        try
        {
            Environment env = null;

            if ( config.getSetupType() == SetupType.WITH_HADOOP )
            {

                if ( hadoopConfig == null )
                {
                    trackerOperation.addLogFailed( "No Hadoop configuration specified" );
                    return;
                }
                hadoopConfig.setTemplateName( NutchConfig.TEMPLATE_NAME );
                try
                {
                    trackerOperation.addLog( "Building environment..." );
                    EnvironmentBlueprint eb = manager.getHadoopManager().getDefaultEnvironmentBlueprint( hadoopConfig );
                    env = manager.getEnvironmentManager().buildEnvironment( eb );
                }
                catch ( ClusterSetupException | EnvironmentBuildException ex )
                {
                    throw new ClusterException( "Failed to build environment: " + ex.getMessage() );
                }

                trackerOperation.addLog( "Environment built successfully" );
            }
            else
            {
                env = manager.getEnvironmentManager().getEnvironmentByUUID( hadoopConfig.getEnvironmentId() );
                if ( env == null )
                {
                    throw new ClusterException( String.format( "Could not find environment of Hadoop cluster by id %s",
                            hadoopConfig.getEnvironmentId() ) );
                }
            }

            ClusterSetupStrategy s = manager.getClusterSetupStrategy( env, config, trackerOperation );
            try
            {
                if ( s == null )
                {
                    throw new ClusterSetupException( "No setup strategy" );
                }
                trackerOperation.addLog( "Installing cluster..." );
                s.setup();
                trackerOperation.addLogDone( "Installing cluster completed" );
            }
            catch ( ClusterSetupException ex )
            {
                throw new ClusterException( "Failed to setup cluster: " + ex.getMessage() );
            }
        }
        catch ( ClusterException e )
        {
            trackerOperation.addLogFailed( String.format( "Could not start all nodes : %s", e.getMessage() ) );
        }


    }


    @Override
    public void destroyCluster()
    {
        NutchConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }

        try
        {
            trackerOperation.addLog( "Destroying environment..." );
            manager.getEnvironmentManager().destroyEnvironment( config.getEnvironmentId() );
            manager.getPluginDao().deleteInfo( NutchConfig.PRODUCT_KEY, config.getClusterName() );
            trackerOperation.addLogDone( "Cluster destroyed" );
        }
        catch ( EnvironmentDestroyException e )
        {
            trackerOperation.addLogFailed( String.format( "Error running command, %s", e.getMessage() ) );
            LOG.error( e.getMessage(), e );
        }

    }

    @Override
    public void run()
    {
        Preconditions.checkNotNull( config, "Configuration is null !!!" );
        switch ( operationType )
        {
            case INSTALL:
                executor.execute( new Runnable()
                {
                    public void run()
                    {
                        setupCluster();
                    }
                } );
                break;
            case DESTROY:
                if ( config.getSetupType() == SetupType.OVER_HADOOP )
                {
                    uninstallCluster();
                }
                else if ( config.getSetupType() == SetupType.WITH_HADOOP )
                {
                    executor.execute( new Runnable()
                    {
                        public void run()
                        {
                            destroyCluster();
                        }
                    } );
                }

                break;
        }

    }


    private void uninstallCluster()
    {
        TrackerOperation po = trackerOperation;
        po.addLog( "Uninstalling Nutch..." );

        for ( UUID uuid : config.getNodes() )
        {
            ContainerHost containerHost = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() ).getContainerHostByUUID( uuid );
            CommandResult result = null;
            try
            {
                result = containerHost.execute( new RequestBuilder( Commands.UNINSTALL ) );
                if ( !result.hasSucceeded() )
                {
                    po.addLog( result.getStdErr() );
                    po.addLogFailed( "Uninstallation failed" );
                    return;
                }
            }
            catch ( CommandException e )
            {
                LOG.error( e.getMessage(), e );
            }
        }
        po.addLog( "Updating db..." );
        manager.getPluginDao().deleteInfo( NutchConfig.PRODUCT_KEY, config.getClusterName() );
        po.addLogDone( "Cluster info deleted from DB\nDone" );
    }
}
