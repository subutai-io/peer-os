package io.subutai.core.environment.impl.tasks;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.dao.EnvironmentService;
import io.subutai.core.environment.impl.entity.LocalEnvironment;


public class EnvironmentManagerInitTask implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentManagerInitTask.class );

    private final LocalPeer localPeer;
    private final EnvironmentManagerImpl environmentManager;
    private final EnvironmentService environmentService;


    public EnvironmentManagerInitTask( final LocalPeer localPeer, final EnvironmentManagerImpl environmentManager,
                                       final EnvironmentService environmentService )
    {
        this.localPeer = localPeer;
        this.environmentManager = environmentManager;
        this.environmentService = environmentService;
    }


    @Override
    public void run()
    {
        //due to karaf bundle loading scheme, this bundle can be loaded several times
        //skip the loads when local peer is not ready yet
        if ( localPeer.getState() != LocalPeer.State.READY )
        {
            return;
        }

        boolean dbReady = false;
        do
        {
            TaskUtil.sleep( 1000 );

            try
            {
                LOG.debug( "Waiting for db initialization" );
                environmentService.getAll();
                dbReady = true;
            }
            catch ( Exception e )
            {
                //ignore
            }
        }
        while ( !dbReady );

        //update all local environments that are UNDER_MODIFICATION to be UNHEALTHY
        for ( LocalEnvironment environment : environmentService.getAll() )
        {
            if ( environment.getStatus().equals( EnvironmentStatus.UNDER_MODIFICATION ) )
            {
                environment.setStatus( EnvironmentStatus.UNHEALTHY );

                environmentManager.update( environment );
            }
        }
    }
}
