package io.subutai.core.environment.impl.tasks;


import java.util.concurrent.Semaphore;

import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.exception.ResultHolder;
import io.subutai.core.peer.api.HostNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.exception.ExceptionUtils;


/**
 * Environment Container host destroy task. This destroys, removes {@link io.subutai.core.environment.impl.entity
 * .EnvironmentContainerImpl} from {@link io.subutai.core.environment.impl.entity.EnvironmentImpl} metadata. And
 * consequently destroys container host on existing Peer#ResourceHost
 *
 * @see io.subutai.core.environment.impl.EnvironmentManagerImpl
 * @see io.subutai.core.environment.impl.entity.EnvironmentImpl
 * @see io.subutai.common.peer.ContainerHost
 * @see io.subutai.core.environment.impl.exception.ResultHolder
 * @see java.lang.Runnable
 */
public class DestroyContainerTask implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( DestroyContainerTask.class.getName() );

    private final EnvironmentManagerImpl environmentManager;
    private final EnvironmentImpl environment;
    private final ContainerHost containerHost;
    private final ResultHolder<EnvironmentModificationException> resultHolder;
    private final TrackerOperation op;
    protected boolean forceMetadataRemoval;
    protected Semaphore semaphore;


    public DestroyContainerTask( final EnvironmentManagerImpl environmentManager, final EnvironmentImpl environment,
                                 final ContainerHost containerHost, final boolean forceMetadataRemoval,
                                 final ResultHolder<EnvironmentModificationException> resultHolder,
                                 final TrackerOperation op )
    {
        this.environmentManager = environmentManager;
        this.environment = environment;
        this.containerHost = containerHost;
        this.forceMetadataRemoval = forceMetadataRemoval;
        this.resultHolder = resultHolder;
        this.semaphore = new Semaphore( 0 );
        this.op = op;
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
                    boolean skipError = false;
                    if ( e instanceof HostNotFoundException || ( ExceptionUtils.getRootCauseMessage( e )
                                                                               .contains( "HostNotFoundException" ) ) )
                    {
                        //skip error since host is not found
                        skipError = true;
                    }
                    if ( !skipError )
                    {
                        if ( forceMetadataRemoval )
                        {
                            op.addLog( String.format( "Error destroying container: %s", e.getMessage() ) );
                            resultHolder.setResult( new EnvironmentModificationException( e ) );
                        }
                        else
                        {
                            throw e;
                        }
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
                    environmentManager.removeEnvironment( environment.getId(), false );
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

            op.addLogDone( "Container destroyed successfully" );
        }
        catch ( EnvironmentModificationException e )
        {
            LOG.error( String.format( "Error destroying container %s", containerHost.getHostname() ), e );
            resultHolder.setResult( e );
            op.addLogFailed( String.format( "Error destroying container: %s", e.getMessage() ) );
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
