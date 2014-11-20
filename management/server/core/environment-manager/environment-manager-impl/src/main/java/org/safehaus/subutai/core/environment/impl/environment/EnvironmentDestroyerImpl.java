package org.safehaus.subutai.core.environment.impl.environment;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by bahadyr on 11/7/14.
 */
public class EnvironmentDestroyerImpl implements EnvironmentDestroyer
{

    private static final Logger LOG = LoggerFactory.getLogger( ContainerCreatorThread.class.getName() );
    private ExecutorService executorService;
    final int approximateDestroyTime = 30; //Seconds


    @Override
    public void destroy( Environment environment ) throws DestroyException
    {
        EnvironmentDestroyerThread thread = new EnvironmentDestroyerThread( environment );

        executorService = Executors.newCachedThreadPool();
        executorService.execute( thread );
        executorService.shutdown();

        try
        {
            int timeout = environment.getContainers().size() * approximateDestroyTime;
            executorService.awaitTermination( timeout, TimeUnit.SECONDS );
        }
        catch ( InterruptedException e )
        {
            LOG.error( e.getMessage(), e );
            throw new DestroyException( e.getMessage() );
        }
    }
}
