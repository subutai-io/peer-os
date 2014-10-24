package org.safehaus.subutai.plugin.shark.impl.handler;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.shark.api.SetupType;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;


public class InstallOperationHandler extends AbstractOperationHandler<SharkImpl>
{
    private final SharkClusterConfig config;
    private HadoopClusterConfig hadoopConfig;


    public InstallOperationHandler( SharkImpl manager, SharkClusterConfig config )
    {
        this( manager, config, null );
    }


    public InstallOperationHandler( SharkImpl manager, SharkClusterConfig config, HadoopClusterConfig hadoopConfig )
    {
        super( manager, config.getClusterName() );
        this.config = config;
        this.hadoopConfig = hadoopConfig;
        this.trackerOperation = manager.getTracker().createTrackerOperation( SharkClusterConfig.PRODUCT_KEY,
                String.format( "Installing %s", SharkClusterConfig.PRODUCT_KEY ) );
    }


    @Override
    public void run()
    {
        TrackerOperation po = trackerOperation;
        Environment env = null;
        ClusterSetupStrategy css;
        if ( config.getSetupType() == SetupType.WITH_HADOOP_SPARK )
        {
            if ( hadoopConfig == null )
            {
                po.addLogFailed( "No Hadoop configuration specified" );
                return;
            }

            po.addLog( "Preparing environment..." );
            hadoopConfig.setTemplateName( SharkClusterConfig.TEMPLATE_NAME );
            try
            {
                EnvironmentBuildTask eb = manager.getHadoopManager().getDefaultEnvironmentBlueprint( hadoopConfig );
                env = manager.getEnvironmentManager().buildEnvironment( eb );

                css = manager.getHadoopManager().getClusterSetupStrategy( po, hadoopConfig, env );
                css.setup();

                SparkClusterConfig sparkConfig = new SparkClusterConfig();
                sparkConfig.setClusterName( config.getSparkClusterName() );
                sparkConfig.setSetupType( org.safehaus.subutai.plugin.spark.api.SetupType.WITH_HADOOP );
                css = manager.getSparkManager().getClusterSetupStrategy( po, sparkConfig, env );
                css.setup();
            }
            catch ( ClusterSetupException ex )
            {
                po.addLogFailed( "Failed to prepare environment: " + ex.getMessage() );
                destroyNodes( env );
                return;
            }
            catch ( EnvironmentBuildException ex )
            {
                po.addLogFailed( "Failed to build environment: " + ex.getMessage() );
                destroyNodes( env );
                return;
            }
            po.addLog( "Environment preparation completed" );
        }

        css = manager.getClusterSetupStrategy( po, config, env );
        try
        {
            if ( css == null )
            {
                throw new ClusterSetupException( "No setup strategy" );
            }
            po.addLog( "Installing cluster..." );
            css.setup();
            po.addLogDone( "Installing cluster completed" );
        }
        catch ( ClusterSetupException ex )
        {
            po.addLogFailed( "Failed to setup cluster: " + ex.getMessage() );
            destroyNodes( env );
        }
    }


    void destroyNodes( Environment env )
    {
        if ( env != null )
        {
            try
            {
                manager.getEnvironmentManager().destroyEnvironment( env.getId().toString() );
                trackerOperation.addLog( "Environment destroyed" );
            }
            catch ( EnvironmentDestroyException ex )
            {
                trackerOperation.addLog( "Failed to destroy environment: " + ex.getMessage() );
            }
        }
    }
}

