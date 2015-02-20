package org.safehaus.subutai.core.env.impl.tasks;


import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.environment.Topology;
import org.safehaus.subutai.core.env.api.exception.EnvironmentCreationException;
import org.safehaus.subutai.core.env.impl.EnvironmentManagerImpl;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.env.impl.exception.ResultHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CreateEnvironmentTask implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( CreateEnvironmentTask.class.getName() );

    private final EnvironmentManagerImpl environmentManager;
    private final EnvironmentImpl environment;
    private final Topology topology;
    private final ResultHolder<EnvironmentCreationException> resultHolder;
    private final Semaphore semaphore;


    public CreateEnvironmentTask( final EnvironmentManagerImpl environmentManager, final EnvironmentImpl environment,
                                  final Topology topology,
                                  final ResultHolder<EnvironmentCreationException> resultHolder )
    {
        this.environmentManager = environmentManager;
        this.environment = environment;
        this.topology = topology;
        this.resultHolder = resultHolder;
        this.semaphore = new Semaphore( 0 );
    }


    @Override
    public void run()
    {
        try
        {
            //figure out free VNI
            long vni = environmentManager.findFreeVni( topology.getNodeGroupPlacement().keySet() );

            environment.setVni( vni );

            environmentManager.growEnvironment( environment.getId(), topology, false );
        }
        catch ( Exception e )
        {
            LOG.error( String.format( "Error creating environment %s, topology %s", environment.getId(), topology ),
                    e );

            resultHolder.setResult( new EnvironmentCreationException( e ) );
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
