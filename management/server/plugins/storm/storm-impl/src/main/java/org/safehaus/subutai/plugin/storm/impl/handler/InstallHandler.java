package org.safehaus.subutai.plugin.storm.impl.handler;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.Node;
import org.safehaus.subutai.plugin.storm.api.StormConfig;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;


public class InstallHandler extends AbstractHandler
{

    private final StormConfig config;


    public InstallHandler( StormImpl manager, StormConfig config )
    {
        super( manager, config.getClusterName() );
        this.config = config;
        this.productOperation = manager.getTracker().createProductOperation( StormConfig.PRODUCT_NAME,
                "Install cluster " + config.getClusterName() );
    }


    @Override
    public void run()
    {
        Environment env = null;
        EnvironmentBuildTask eb = manager.getDefaultEnvironmentBlueprint( config );
        try
        {
            productOperation.addLog( "Building environment..." );
            env = manager.getEnvironmentManager().buildEnvironmentAndReturn( eb );
            productOperation.addLog( "Building environment completed" );

            productOperation.addLog( "Installing cluster..." );
            ClusterSetupStrategy s = manager.getClusterSetupStrategy( env, config, productOperation );
            s.setup();
            productOperation.addLog( "Installing cluster completed" );
            productOperation.addLogDone( null );
        }
        catch ( EnvironmentBuildException ex )
        {
            String m = "Failed to build environment";
            productOperation.addLogFailed( m );
            manager.getLogger().error( m, ex );
        }
        catch ( ClusterSetupException ex )
        {
            String m = "Failed to setup cluster";
            productOperation.addLog( ex.getMessage() );
            productOperation.addLogFailed( m );
            manager.getLogger().error( m, ex );
        }
        finally
        {
            if ( productOperation.getState() != ProductOperationState.SUCCEEDED )
            {
                destroyNodes( env );
            }
        }
    }


    void destroyNodes( Environment env )
    {

        if ( env == null || env.getNodes().isEmpty() )
        {
            return;
        }

        Set<Agent> set = new HashSet<>( env.getNodes().size() );
        for ( Node n : env.getNodes() )
        {
            set.add( n.getAgent() );
        }
        productOperation.addLog( "Destroying node(s)..." );
        try
        {
            manager.getContainerManager().clonesDestroy( set );
            productOperation.addLog( "Destroying node(s) completed" );
        }
        catch ( LxcDestroyException ex )
        {
            String m = "Failed to destroy node(s)";
            productOperation.addLog( m );
            manager.getLogger().error( m, ex );
        }
    }
}
