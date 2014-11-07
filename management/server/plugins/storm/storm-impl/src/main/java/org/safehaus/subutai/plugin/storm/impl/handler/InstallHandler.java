package org.safehaus.subutai.plugin.storm.impl.handler;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;


public class InstallHandler extends AbstractHandler
{

    private final StormClusterConfiguration config;


    public InstallHandler( StormImpl manager, StormClusterConfiguration config )
    {
        super( manager, config.getClusterName() );
        this.config = config;
        this.trackerOperation = manager.getTracker().createTrackerOperation( StormClusterConfiguration.PRODUCT_NAME,
                "Install cluster " + config.getClusterName() );
    }


    @Override
    public void run()
    {
        Environment env = null;
        EnvironmentBlueprint eb = manager.getDefaultEnvironmentBlueprint( config );
        try
        {
            trackerOperation.addLog( "Building environment..." );
            env = manager.getEnvironmentManager().buildEnvironment( eb );
            trackerOperation.addLog( "Building environment completed" );

            trackerOperation.addLog( "Installing cluster..." );
            ClusterSetupStrategy s = manager.getClusterSetupStrategy( env, config, trackerOperation );
            s.setup();
            trackerOperation.addLog( "Installing cluster completed" );
            trackerOperation.addLogDone( null );
        }
        catch ( EnvironmentBuildException ex )
        {
            String m = "Failed to build environment";
            trackerOperation.addLogFailed( m );
            manager.getLogger().error( m, ex );
        }
        catch ( ClusterSetupException ex )
        {
            String m = "Failed to setup cluster";
            trackerOperation.addLog( ex.getMessage() );
            trackerOperation.addLogFailed( m );
            manager.getLogger().error( m, ex );
        }
        finally
        {
            if ( trackerOperation.getState() != OperationState.SUCCEEDED )
            {
                destroyNodes( env );
            }
        }
    }


    void destroyNodes( Environment env )
    {
        trackerOperation.addLogFailed( "Destroy nodes is not provided by environment manager now. Aborting!" );

        //        if ( env == null || env.getContainers().isEmpty() )
//        {
//            return;
//        }
//
//        Set<Agent> set = new HashSet<>( env.getContainers().size() );
//        for ( EnvironmentContainer n : env.getContainers() )
//        {
//            set.add( n.getAgent() );
//        }
//        trackerOperation.addLog( "Destroying node(s)..." );
//        try
//        {
//            manager.getContainerManager().clonesDestroy( set );
//            trackerOperation.addLog( "Destroying node(s) completed" );
//        }
//        catch ( LxcDestroyException ex )
//        {
//            String m = "Failed to destroy node(s)";
//            trackerOperation.addLog( m );
//            manager.getLogger().error( m, ex );
//        }
    }
}
