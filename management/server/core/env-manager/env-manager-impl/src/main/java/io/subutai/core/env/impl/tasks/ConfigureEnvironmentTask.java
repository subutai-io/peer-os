package io.subutai.core.env.impl.tasks;


import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.core.env.impl.EnvironmentManagerImpl;
import io.subutai.core.env.impl.entity.EnvironmentImpl;
import io.subutai.core.env.impl.exception.ResultHolder;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.PeerManager;


/**
 * Created by talas on 9/15/15.
 */
public class ConfigureEnvironmentTask implements Awaitable
{
    private static final Logger LOG = LoggerFactory.getLogger( GrowEnvironmentTask.class.getName() );

    private final EnvironmentManagerImpl environmentManager;
    private final EnvironmentImpl environment;
    private final PeerManager peerManager;
    private final ResultHolder<EnvironmentModificationException> resultHolder;
    private final TrackerOperation op;
    protected Semaphore semaphore;
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();


    public ConfigureEnvironmentTask( final EnvironmentManagerImpl environmentManager, final EnvironmentImpl environment,
                                     final PeerManager peerManager,
                                     final ResultHolder<EnvironmentModificationException> resultHolder,
                                     final TrackerOperation op )
    {
        this.environmentManager = environmentManager;
        this.environment = environment;
        this.peerManager = peerManager;
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
                op.addLog( "Ensuring secure channel..." );

                op.addLog( "Cloning containers..." );

                LocalPeer localPeer = peerManager.getLocalPeer();
                localPeer.processEnvironmentContainers( environment );

                environmentManager.setContainersTransientFields( environment );

                op.addLog( "Configuring /etc/hosts..." );

                environmentManager.configureHosts( environment.getContainerHosts() );

                op.addLog( "Configuring ssh..." );

                environmentManager.configureSsh( environment.getContainerHosts() );

                if ( !Strings.isNullOrEmpty( environment.getSshKey() ) )
                {
                    op.addLog( "Setting environment ssh key.." );

                    environmentManager.setSshKey( environment.getId(), environment.getSshKey(), false, false, op );
                }

                environment.setStatus( EnvironmentStatus.HEALTHY );

                op.addLogDone( "Environment grown successfully" );
            }
            catch ( Exception e )
            {
                environment.setStatus( EnvironmentStatus.UNHEALTHY );

                throw new EnvironmentModificationException( exceptionUtil.getRootCause( e ) );
            }
        }
        catch ( EnvironmentModificationException e )
        {
            LOG.error( String.format( "Error growing environment %s", environment.getId() ), e );

            resultHolder.setResult( e );

            op.addLogFailed( String.format( "Error growing environment: %s", resultHolder.getResult().getMessage() ) );
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