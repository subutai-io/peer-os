package org.safehaus.subutai.core.env.impl.tasks;


import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.environment.EnvironmentNotFoundException;
import org.safehaus.subutai.common.environment.EnvironmentStatus;
import org.safehaus.subutai.common.environment.Topology;
import org.safehaus.subutai.core.env.api.exception.EnvironmentCreationException;
import org.safehaus.subutai.core.env.impl.EnvironmentManagerImpl;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.env.impl.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.env.impl.exception.ResultHolder;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CreateEnvironmentTask implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( CreateEnvironmentTask.class.getName() );

    private final EnvironmentManagerImpl environmentManager;
    private final EnvironmentImpl environment;
    private final Semaphore semaphore;
    private final ResultHolder<EnvironmentCreationException> resultHolder;
    private final Topology topology;


    public CreateEnvironmentTask( final EnvironmentManagerImpl environmentManager, final EnvironmentImpl environment,
                                  final ResultHolder<EnvironmentCreationException> resultHolder,
                                  final Topology topology )
    {
        this.environmentManager = environmentManager;
        this.environment = environment;
        this.resultHolder = resultHolder;
        this.topology = topology;
        this.semaphore = new Semaphore( 0 );
    }


    @Override
    public void run()
    {
        try
        {
            environmentManager.saveEnvironment( environment );

            environmentManager.setEnvironmentTransientFields( environment );

            environment.setStatus( EnvironmentStatus.UNDER_MODIFICATION );

            try
            {
                environmentManager.build( environment, topology );

                environmentManager.configureHosts( environment.getContainerHosts() );

                environmentManager.configureSsh( environment.getContainerHosts() );

                environment.setStatus( EnvironmentStatus.HEALTHY );

                environmentManager.setContainersTransientFields( environment.getContainerHosts() );
            }
            catch ( EnvironmentBuildException | NetworkManagerException e )
            {
                environment.setStatus( EnvironmentStatus.UNHEALTHY );

                throw new EnvironmentCreationException( e );
            }
            finally
            {
                try
                {
                    environmentManager
                            .notifyOnEnvironmentCreated( environmentManager.findEnvironment( environment.getId() ) );
                }
                catch ( EnvironmentNotFoundException e )
                {
                    LOG.warn( "Error notifying on environment creation", e );
                }
            }
        }
        catch ( EnvironmentCreationException e )
        {
            LOG.error( String.format( "Error creating environment %s, topology %s", environment.getName(), topology ),
                    e );
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
