package io.subutai.core.env.impl.tasks;


import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.core.env.impl.entity.EnvironmentImpl;
import io.subutai.core.env.impl.exception.ResultHolder;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;


/**
 * Generate and configure environment ssh keys.
 *
 * @see io.subutai.core.env.impl.entity.EnvironmentImpl
 * @see io.subutai.core.env.impl.exception.ResultHolder
 * @see io.subutai.core.network.api.NetworkManager
 * @see java.lang.Runnable
 */
public class SetSshKeyTask implements Awaitable
{
    private static final Logger LOG = LoggerFactory.getLogger( SetSshKeyTask.class.getName() );

    private final EnvironmentImpl environment;
    private final NetworkManager networkManager;
    private final ResultHolder<EnvironmentModificationException> resultHolder;
    private final String sshKey;
    private final TrackerOperation op;
    protected Semaphore semaphore;
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();


    public SetSshKeyTask( final EnvironmentImpl environment, final NetworkManager networkManager,
                          final ResultHolder<EnvironmentModificationException> resultHolder, final String sshKey,
                          final TrackerOperation op )
    {
        this.environment = environment;
        this.networkManager = networkManager;
        this.resultHolder = resultHolder;
        this.sshKey = Strings.isNullOrEmpty( sshKey ) ? null : sshKey.trim();
        this.semaphore = new Semaphore( 0 );
        this.op = op;
    }


    @Override
    public void run()
    {
        environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

        String oldSshKey = environment.getSshKey();

        environment.saveSshKey( sshKey );

        try
        {
            if ( Strings.isNullOrEmpty( sshKey ) && !Strings.isNullOrEmpty( oldSshKey ) )
            {
                //remove old key from containers
                networkManager.removeSshKeyFromAuthorizedKeys( environment.getContainerHosts(), oldSshKey );
            }
            else if ( !Strings.isNullOrEmpty( sshKey ) && Strings.isNullOrEmpty( oldSshKey ) )
            {
                //insert new key to containers
                networkManager.addSshKeyToAuthorizedKeys( environment.getContainerHosts(), sshKey );
            }
            else if ( !Strings.isNullOrEmpty( sshKey ) && !Strings.isNullOrEmpty( oldSshKey ) )
            {
                //replace old ssh key with new one
                networkManager.replaceSshKeyInAuthorizedKeys( environment.getContainerHosts(), oldSshKey, sshKey );
            }

            environment.setStatus( EnvironmentStatus.HEALTHY );

            op.addLogDone( "Environment ssh key is successfully set" );
        }
        catch ( NetworkManagerException e )
        {
            LOG.error( String.format( "Error setting ssh key to environment %s", environment.getName() ), e );
            environment.setStatus( EnvironmentStatus.UNHEALTHY );
            resultHolder.setResult( new EnvironmentModificationException( exceptionUtil.getRootCause( e ) ) );
            op.addLogFailed(
                    String.format( "Error setting environment ssh key: %s", exceptionUtil.getRootCauseMessage( e ) ) );
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
