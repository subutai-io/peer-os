package org.safehaus.subutai.plugin.oozie.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.SetupType;
import org.safehaus.subutai.plugin.oozie.impl.OozieImpl;


public class InstallHandler extends AbstractOperationHandler<OozieImpl, OozieClusterConfig>
{

    private final TrackerOperation trackerOperation;
    private OozieClusterConfig config;
    private HadoopClusterConfig hadoopConfig;


    public InstallHandler( final OozieImpl manager, final OozieClusterConfig config )
    {
        super( manager, config.getClusterName() );
        this.config = config;
        trackerOperation = manager.getTracker().createTrackerOperation( OozieClusterConfig.PRODUCT_KEY,
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
        /*Environment env = null;

        if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {

            if ( hadoopConfig == null )
            {
                trackerOperation.addLogFailed( "No Hadoop configuration specified" );
                return;
            }

            trackerOperation.addLog( "Preparing environment..." );
            hadoopConfig.setTemplateName( OozieClusterConfig.PRODUCT_NAME_SERVER );
            try
            {
                EnvironmentBuildTask eb = manager.getHadoopManager().getDefaultEnvironmentBlueprint( hadoopConfig );
                env = manager.getEnvironmentManager().buildEnvironment( eb );
            }
            catch ( ClusterSetupException ex )
            {
                trackerOperation.addLogFailed( "Failed to prepare environment: " + ex.getMessage() );
                return;
            }
            catch ( EnvironmentBuildException ex )
            {
                trackerOperation.addLogFailed( "Failed to build environment: " + ex.getMessage() );
                return;
            }
            trackerOperation.addLog( "Environment preparation completed" );
        }

        ClusterSetupStrategy s = manager.getClusterSetupStrategy( env, config, trackerOperation );
        try
        {
            if ( s == null )
            {
                throw new ClusterSetupException( "No setup strategy" );
            }

            s.setup();
            trackerOperation.addLogDone( "Done" );
        }
        catch ( ClusterSetupException ex )
        {
            trackerOperation.addLogFailed( "Failed to setup cluster: " + ex.getMessage() );
        }*/
    }
}
