package io.subutai.core.env.impl.tasks;


import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.env.impl.entity.EnvironmentImpl;
import io.subutai.core.env.impl.exception.ResultHolder;
import io.subutai.core.peer.api.PeerManager;


/**
 * (Un)sets environment domain
 */
public class SetDomainTask implements Awaitable
{
    private static final Logger LOG = LoggerFactory.getLogger( SetDomainTask.class.getName() );
    private final EnvironmentImpl environment;
    private final TrackerOperation op;
    private final ResultHolder<EnvironmentModificationException> resultHolder;
    private final String domain;
    private final PeerManager peerManager;
    protected Semaphore semaphore;


    public SetDomainTask( final EnvironmentImpl environment, final PeerManager peerManager,
                          final ResultHolder<EnvironmentModificationException> resultHolder, final TrackerOperation op,
                          final String domain )
    {
        this.environment = environment;
        this.peerManager = peerManager;
        this.resultHolder = resultHolder;
        this.op = op;
        this.domain = domain;
        this.semaphore = new Semaphore( 0 );
    }


    @Override
    public void run()
    {
        try
        {
            environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

            if ( Strings.isNullOrEmpty( domain ) )
            {
                peerManager.getLocalPeer().removeVniDomain( environment.getVni() );
            }
            else
            {
                peerManager.getLocalPeer().setVniDomain( environment.getVni(), domain );
            }

            environment.setStatus( EnvironmentStatus.HEALTHY );

            op.addLogDone( "Environment domain is successfully set" );
        }
        catch ( Exception e )
        {
            LOG.error( String.format( "Error setting domain of environment %s", environment.getName() ), e );
            environment.setStatus( EnvironmentStatus.UNHEALTHY );
            resultHolder.setResult( new EnvironmentModificationException( e ) );
            op.addLogFailed( String.format( "Error setting domain of environment: %s", e.getMessage() ) );
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
