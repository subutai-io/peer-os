package io.subutai.core.env.impl.tasks;


import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.settings.Common;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.env.impl.entity.EnvironmentImpl;
import io.subutai.core.env.impl.exception.ResultHolder;
import io.subutai.core.peer.api.PeerManager;


/**
 * Include/excludes container to/from environment domain
 */
public class SetContainerDomainTask implements Awaitable
{
    private static final Logger LOG = LoggerFactory.getLogger( SetContainerDomainTask.class.getName() );
    private final EnvironmentImpl environment;
    private final ContainerHost containerHost;
    private final TrackerOperation op;
    private final ResultHolder<EnvironmentModificationException> resultHolder;
    private final boolean add;
    private final PeerManager peerManager;
    protected Semaphore semaphore;


    public SetContainerDomainTask( final EnvironmentImpl environment, final ContainerHost containerHost,
                                   final TrackerOperation op,
                                   final ResultHolder<EnvironmentModificationException> resultHolder,
                                   final PeerManager peerManager, boolean add )
    {
        this.environment = environment;
        this.containerHost = containerHost;
        this.op = op;
        this.resultHolder = resultHolder;
        this.add = add;
        this.peerManager = peerManager;
        this.semaphore = new Semaphore( 0 );
    }


    @Override
    public void run()
    {
        try
        {
            environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

            if ( add )
            {
                peerManager.getLocalPeer()
                           .addIpToVniDomain( containerHost.getIpByInterfaceName( Common.DEFAULT_CONTAINER_INTERFACE ),
                                   environment.getVni() );
            }
            else
            {
                peerManager.getLocalPeer().removeIpFromVniDomain(
                        containerHost.getIpByInterfaceName( Common.DEFAULT_CONTAINER_INTERFACE ),
                        environment.getVni() );
            }

            environment.setStatus( EnvironmentStatus.HEALTHY );

            op.addLogDone( "Container domain is successfully set" );
        }
        catch ( Exception e )
        {
            LOG.error( String.format( "Error setting domain of container %s", containerHost.getId() ), e );
            environment.setStatus( EnvironmentStatus.UNHEALTHY );
            resultHolder.setResult( new EnvironmentModificationException( e ) );
            op.addLogFailed( String.format( "Error setting domain of container %s: %s", containerHost.getId(),
                    e.getMessage() ) );
        }
        finally
        {
            semaphore.release();
        }
    }


    @Override
    public void waitCompletion() throws InterruptedException
    {
        semaphore.acquire();
    }
}
