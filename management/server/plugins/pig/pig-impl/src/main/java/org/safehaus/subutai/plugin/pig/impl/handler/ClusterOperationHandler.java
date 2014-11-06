package org.safehaus.subutai.plugin.pig.impl.handler;


import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.ClusterOperationHandlerInterface;
import org.safehaus.subutai.plugin.common.api.OperationType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.pig.api.PigConfig;
import org.safehaus.subutai.plugin.pig.api.SetupType;
import org.safehaus.subutai.plugin.pig.impl.Commands;
import org.safehaus.subutai.plugin.pig.impl.PigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Created by ebru on 05.11.2014.
 */
public class ClusterOperationHandler extends AbstractOperationHandler<PigImpl> implements ClusterOperationHandlerInterface
{
    private static final Logger LOG = LoggerFactory.getLogger( ClusterOperationHandler.class.getName() );
    private OperationType operationType;
    private PigConfig config;
    private HadoopClusterConfig hadoopConfig;
    private ExecutorService executor = Executors.newCachedThreadPool();
    public ClusterOperationHandler( final PigImpl manager, final PigConfig config,
                                    final OperationType operationType )
    {
        super( manager, config.getClusterName() );
        this.operationType = operationType;
        this.config = config;
        trackerOperation = manager.getTracker().createTrackerOperation( PigConfig.PRODUCT_KEY,
                String.format( "Creating %s tracker object...", clusterName ) );
    }

    public void setHadoopConfig( HadoopClusterConfig hadoopConfig )
    {
        this.hadoopConfig = hadoopConfig;
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
            case UNINSTALL:
                executor.execute( new Runnable()
                {
                    public void run()
                    {
                        uninstallCluster();
                    }
                } );
                break;
            case DESTROY:
                executor.execute( new Runnable()
                {
                    public void run()
                    {
                        destroyCluster();
                    }
                } );
                break;
        }
    }


    @Override
    public void runOperationOnContainers( final OperationType operationType )
    {

    }


    @Override
    public void setupCluster()
    {
        TrackerOperation po = trackerOperation;
        Environment env = null;

        if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {

            if ( hadoopConfig == null )
            {
                po.addLogFailed( "No Hadoop configuration specified" );
                return;
            }

            po.addLog( "Preparing environment..." );
            hadoopConfig.setTemplateName( PigConfig.TEMPLATE_NAME );
            try
            {
                EnvironmentBlueprint eb = manager.getHadoopManager().getDefaultEnvironmentBlueprint( hadoopConfig );
                env = manager.getEnvironmentManager().buildEnvironment( eb );
            }
            catch ( ClusterSetupException ex )
            {
                po.addLogFailed( "Failed to prepare environment: " + ex.getMessage() );
                return;
            }
            catch ( EnvironmentBuildException ex )
            {
                po.addLogFailed( "Failed to build environment: " + ex.getMessage() );
                return;
            }
            po.addLog( "Environment preparation completed" );
        }

        ClusterSetupStrategy s = manager.getClusterSetupStrategy( env, config, po );

        try
        {
            if ( s == null )
            {
                throw new ClusterSetupException( "No setup strategy" );
            }

            s.setup();
            po.addLogDone( "Done" );
        }
        catch ( ClusterSetupException ex )
        {
            po.addLogFailed( "Failed to setup cluster: " + ex.getMessage() );
        }

    }


    @Override
    public void destroyCluster()
    {
        trackerOperation.addLog( "Destroying node(s)..." );
//                try
//                {
//                    manager.getContainerManager().clonesDestroy( config.getNodes() );
//                    trackerOperation.addLog( "Destroying node(s) completed" );
//                }
//                catch ( LxcDestroyException ex )
//                {
//                    trackerOperation.addLog( "Failed to destroy node(s): " + ex.getMessage() );
//                }

    }

    public boolean uninstallCluster(){
        TrackerOperation po = trackerOperation;
        po.addLog( "Uninstalling Pig..." );

        for ( Iterator<UUID> it = config.getNodes().iterator(); it.hasNext(); )
        {
            UUID containerUUID = it.next();
            ContainerHost containerHost = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() ).getContainerHostByUUID( containerUUID );

            CommandResult result = null;
            try
            {
                result = containerHost.execute( new RequestBuilder( Commands.uninstallCommand ) );
                if ( !result.hasSucceeded() )
                {
                    po.addLog( result.getStdErr() );
                    po.addLogFailed( "Uninstallation failed" );
                    return false;
                }
            }
            catch ( CommandException e )
            {
                LOG.error( e.getMessage(), e );
            }
        }
        return true;
    }
}
