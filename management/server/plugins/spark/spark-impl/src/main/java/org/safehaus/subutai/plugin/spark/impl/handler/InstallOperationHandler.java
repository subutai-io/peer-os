package org.safehaus.subutai.plugin.spark.impl.handler;


import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.spark.api.SetupType;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;


public class InstallOperationHandler extends AbstractOperationHandler<SparkImpl>
{

    private final SparkClusterConfig config;
    private HadoopClusterConfig hadoopConfig;


    public InstallOperationHandler( SparkImpl manager, SparkClusterConfig config )
    {
        super( manager, config.getClusterName() );
        this.config = config;
        trackerOperation = manager.getTracker().createTrackerOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Installing %s", SparkClusterConfig.PRODUCT_KEY ) );
    }


    public void setHadoopConfig( HadoopClusterConfig hadoopConfig )
    {
        this.hadoopConfig = hadoopConfig;
    }


    @Override
    public void run()
    {
        try
        {
            Environment env;
            if ( config.getSetupType() == SetupType.WITH_HADOOP )
            {

                if ( hadoopConfig == null )
                {
                    throw new ClusterException( "No Hadoop configuration specified" );
                }

                hadoopConfig.setTemplateName( SparkClusterConfig.TEMPLATE_NAME );
                try
                {
                    trackerOperation.addLog( "Building environment..." );
                    EnvironmentBlueprint eb = manager.getHadoopManager().getDefaultEnvironmentBlueprint( hadoopConfig );
                    env = manager.getEnvironmentManager().buildEnvironment( eb );

                    ClusterSetupStrategy s =
                            manager.getHadoopManager().getClusterSetupStrategy( env, hadoopConfig, trackerOperation );
                    s.setup();
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
                    throw new ClusterException( String.format( "Could not find environment of Hadoo cluster by id %s",
                            hadoopConfig.getEnvironmentId() ) );
                }
            }

            ClusterSetupStrategy s = manager.getClusterSetupStrategy( trackerOperation, config, env );
            try
            {
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
}

