package org.safehaus.subutai.core.environment.impl.environment;


import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by bahadyr on 11/7/14.
 */
public class EnvironmentDestroyerImpl implements EnvironmentDestroyer, Observer
{

    private static final Logger LOG = LoggerFactory.getLogger( ContainerCreatorThread.class.getName() );
    private ExecutorService executorService;
    private Environment environment;
    final int approximateCloneTime = 30; //Seconds


    public EnvironmentDestroyerImpl()
    {
        this.executorService = Executors.newCachedThreadPool();
    }


    @Override
    public void destroy( Environment environment ) throws DestroyException
    {
        this.environment = environment;
        EnvironmentDestroyerThread thread = new EnvironmentDestroyerThread( environment );
        thread.addObserver( this );
        executorService.execute( thread );

        executorService.shutdown();
        try
        {
            int timeout = environment.getContainerHosts().size() * approximateCloneTime;
            executorService.awaitTermination( timeout, TimeUnit.SECONDS );
        }
        catch ( InterruptedException e )
        {
            LOG.error( e.getMessage(), e );
            throw new DestroyException( e.getMessage() );
        }
    }


    @Override
    public void update( final Observable o, final Object arg )
    {
        if ( arg instanceof ContainerHost )
        {
            ContainerHost containerHost = ( ContainerHost ) arg;
            if ( environment.getContainers().contains( containerHost ) )
            {
                environment.removeContainer( ( ContainerHost ) arg );
                LOG.info( String.format( "Container host %s removed from environment", containerHost.getId() ) );
            }
        }
    }
}
