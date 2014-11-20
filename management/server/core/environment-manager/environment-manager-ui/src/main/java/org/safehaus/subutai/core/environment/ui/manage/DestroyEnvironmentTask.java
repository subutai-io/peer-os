package org.safehaus.subutai.core.environment.ui.manage;


import java.util.UUID;

import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;


/**
 * Created by bahadyr on 11/7/14.
 */
public class DestroyEnvironmentTask implements Runnable
{

    private EnvironmentManagerPortalModule module;
    private UUID environmentId;
    private final CompleteEvent completeEvent;


    public DestroyEnvironmentTask( final EnvironmentManagerPortalModule managerUI, final UUID environmentId,
                                   final CompleteEvent completeEvent )
    {
        this.module = managerUI;
        this.environmentId = environmentId;
        this.completeEvent = completeEvent;
    }


    @Override
    public void run()
    {

        long start = System.currentTimeMillis();
        while ( !Thread.interrupted() )
        {
            try
            {
                module.getEnvironmentManager().destroyEnvironment( environmentId );
                completeEvent.onComplete("Destroyed");
            }
            catch ( EnvironmentDestroyException e )
            {

            }
            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException ex )
            {
                break;
            }
            if ( System.currentTimeMillis() - start > ( 30 + 3 ) * 1000 )
            {
                break;
            }
        }
    }
}
