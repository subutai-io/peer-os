package io.subutai.core.environment.impl.tasks;


import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.adapter.EnvironmentAdapter;
import io.subutai.core.environment.impl.adapter.BazaarEnvironment;
import io.subutai.core.environment.impl.dao.EnvironmentService;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.environment.impl.xpeer.RemoteEnvironment;


public class RemoveEnvironmentsTask implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( RemoveEnvironmentsTask.class );

    private final EnvironmentAdapter environmentAdapter;
    private final EnvironmentManagerImpl environmentManager;
    private final EnvironmentService environmentService;


    public RemoveEnvironmentsTask( final EnvironmentAdapter environmentAdapter,
                                   final EnvironmentManagerImpl environmentManager,
                                   final EnvironmentService environmentService )
    {
        this.environmentAdapter = environmentAdapter;
        this.environmentManager = environmentManager;
        this.environmentService = environmentService;
    }


    @Override
    public void run()
    {
        if ( !environmentAdapter.canWorkWithBazaar() )
        {
            return;
        }

        try
        {
            Set<BazaarEnvironment> environmentsObtainedFromBazaar = environmentAdapter.getEnvironments( true );

            Set<RemoteEnvironment> locallyRegisteredBazaarEnvironments =
                    environmentManager.getLocallyRegisteredBazaarEnvironments();


            // 1. remove environments on bazaar that are missing locally

            Set<Environment> environmentsMissingLocally = Sets.newHashSet();

            for ( Environment bzrEnvironment : environmentsObtainedFromBazaar )
            {
                boolean isMissingLocally = true;

                for ( Environment localEnvironment : locallyRegisteredBazaarEnvironments )
                {
                    if ( bzrEnvironment.getId().equalsIgnoreCase( localEnvironment.getId() ) )
                    {
                        isMissingLocally = false;

                        break;
                    }
                }

                if ( isMissingLocally )
                {
                    environmentsMissingLocally.add( bzrEnvironment );
                }
            }

            // remove all missing env-s from bazaar

            for ( Environment environment : environmentsMissingLocally )
            {
                environmentAdapter.removeEnvironment( ( LocalEnvironment ) environment );
            }


            // 2. remove local environments that are missing on bazaar

            Set<String> deletedEnvironmentsIdsOnBazaar = environmentAdapter.getDeletedEnvironmentsIds();
            Set<Environment> environmentsMissingOnBazaar = Sets.newHashSet();

            for ( Environment localEnvironment : locallyRegisteredBazaarEnvironments )
            {
                boolean isMissingOnBazaar = false;

                for ( String bzrEnvironmentId : deletedEnvironmentsIdsOnBazaar )
                {
                    if ( localEnvironment.getId().equalsIgnoreCase( bzrEnvironmentId ) )
                    {
                        isMissingOnBazaar = true;

                        break;
                    }
                }

                if ( isMissingOnBazaar )
                {
                    environmentsMissingOnBazaar.add( localEnvironment );
                }
            }

            // destroy local env-s missing on bazaar

            for ( Environment environment : environmentsMissingOnBazaar )
            {
                environmentManager.cleanupEnvironment( environment.getEnvironmentId() );

                environmentManager.notifyOnEnvironmentDestroyed( environment.getId() );
            }

            // notify bazaar about environment deletion

            for ( String bzrEnvironmentId : deletedEnvironmentsIdsOnBazaar )
            {
                environmentAdapter.removeEnvironment( bzrEnvironmentId );
            }

            // 3. Remove deleted local env-s from bazaar

            Collection<LocalEnvironment> deletedEnvs = environmentService.getDeleted();

            for ( LocalEnvironment deletedEnvironment : deletedEnvs )
            {
                if ( environmentAdapter.removeEnvironment( deletedEnvironment ) )
                {
                    environmentService.remove( deletedEnvironment.getId() );
                }
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
    }
}
