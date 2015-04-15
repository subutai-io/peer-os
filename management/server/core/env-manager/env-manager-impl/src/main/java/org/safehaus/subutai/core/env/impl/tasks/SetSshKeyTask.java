package org.safehaus.subutai.core.env.impl.tasks;


import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.environment.EnvironmentModificationException;
import org.safehaus.subutai.common.environment.EnvironmentStatus;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.env.impl.exception.ResultHolder;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;


/**
 * Generate and configure environment ssh keys.
 *
 * @see org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl
 * @see org.safehaus.subutai.core.env.impl.exception.ResultHolder
 * @see org.safehaus.subutai.core.network.api.NetworkManager
 * @see java.lang.Runnable
 */
public class SetSshKeyTask implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( SetSshKeyTask.class.getName() );

    private final EnvironmentImpl environment;
    private final NetworkManager networkManager;
    private final ResultHolder<EnvironmentModificationException> resultHolder;
    private final String sshKey;
    private final TrackerOperation op;
    protected Semaphore semaphore;


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
            resultHolder.setResult( new EnvironmentModificationException( e ) );
            op.addLogFailed( String.format( "Error setting environment ssh key: %s", e.getMessage() ) );
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
