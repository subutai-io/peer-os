package org.safehaus.subutai.core.env.impl.tasks;


import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.environment.EnvironmentModificationException;
import org.safehaus.subutai.common.environment.EnvironmentNotFoundException;
import org.safehaus.subutai.common.environment.EnvironmentStatus;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.core.env.impl.EnvironmentManagerImpl;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentContainerImpl;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.env.impl.exception.ResultHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DestroyContainerTask implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( DestroyContainerTask.class.getName() );

    private final EnvironmentManagerImpl environmentManager;
    private final EnvironmentImpl environment;
    private final ContainerHost containerHost;
    private final boolean forceMetadataRemoval;
    private final ResultHolder<EnvironmentModificationException> resultHolder;
    private final Semaphore semaphore;


    public DestroyContainerTask( final EnvironmentManagerImpl environmentManager, final EnvironmentImpl environment,
                                 final ContainerHost containerHost, final boolean forceMetadataRemoval,
                                 final ResultHolder<EnvironmentModificationException> resultHolder )
    {
        this.environmentManager = environmentManager;
        this.environment = environment;
        this.containerHost = containerHost;
        this.forceMetadataRemoval = forceMetadataRemoval;
        this.resultHolder = resultHolder;
        this.semaphore = new Semaphore( 0 );
    }


    @Override
    public void run()
    {
        try
        {
            environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

            try
            {
                try
                {
                    ( ( EnvironmentContainerImpl ) containerHost ).destroy();
                }
                catch ( PeerException e )
                {
                    if ( forceMetadataRemoval )
                    {
                        resultHolder.setResult( new EnvironmentModificationException( e ) );
                    }
                    else
                    {
                        throw e;
                    }
                }

                environment.removeContainer( containerHost.getId() );

                environmentManager.notifyOnContainerDestroyed( environment, containerHost.getId() );
            }
            catch ( PeerException e )
            {

                environment.setStatus( EnvironmentStatus.UNHEALTHY );

                throw new EnvironmentModificationException( e );
            }

            if ( environment.getContainerHosts().isEmpty() )
            {
                try
                {
                    environmentManager.removeEnvironment( environment.getId() );
                }
                catch ( EnvironmentNotFoundException e )
                {
                    LOG.error( "Error removing environment", e );
                }
            }
            else
            {
                environment.setStatus( EnvironmentStatus.HEALTHY );
            }
        }
        catch ( EnvironmentModificationException e )
        {
            LOG.error( String.format( "Error destroying container %s", containerHost.getHostname() ), e );
            resultHolder.setResult( e );
        }
        finally
        {
            semaphore.release();
        }
    }


    public void waitCompletion() throws InterruptedException
    {
        semaphore.acquire();
    }
}
