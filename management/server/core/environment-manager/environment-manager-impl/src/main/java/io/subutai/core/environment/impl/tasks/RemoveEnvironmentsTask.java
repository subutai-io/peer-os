package io.subutai.core.environment.impl.tasks;


import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.adapter.EnvironmentAdapter;
import io.subutai.core.environment.impl.adapter.HubEnvironment;
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
        if ( !environmentAdapter.canWorkWithHub() )
        {
            return;
        }

        try
        {
            Set<HubEnvironment> environmentsObtainedFromHub = environmentAdapter.getEnvironments( true );

            Set<RemoteEnvironment> locallyRegisteredHubEnvironments =
                    environmentManager.getLocallyRegisteredHubEnvironments();


            // 1. remove environments on Hub that are missing locally

            Set<Environment> environmentsMissingLocally = Sets.newHashSet();

            for ( Environment hubEnvironment : environmentsObtainedFromHub )
            {
                boolean isMissingLocally = true;

                for ( Environment localEnvironment : locallyRegisteredHubEnvironments )
                {
                    if ( hubEnvironment.getId().equalsIgnoreCase( localEnvironment.getId() ) )
                    {
                        isMissingLocally = false;

                        break;
                    }
                }

                if ( isMissingLocally )
                {
                    environmentsMissingLocally.add( hubEnvironment );
                }
            }

            // remove all missing env-s from Hub

            for ( Environment environment : environmentsMissingLocally )
            {
                environmentAdapter.removeEnvironment( ( LocalEnvironment ) environment );
            }


            // 2. remove local environments that are missing on Hub

            Set<String> deletedEnvironmentsIdsOnHub = environmentAdapter.getDeletedEnvironmentsIds();
            Set<Environment> environmentsMissingOnHub = Sets.newHashSet();

            for ( Environment localEnvironment : locallyRegisteredHubEnvironments )
            {
                boolean isMissingOnHub = false;

                for ( String hubEnvironmentId : deletedEnvironmentsIdsOnHub )
                {
                    if ( localEnvironment.getId().equalsIgnoreCase( hubEnvironmentId ) )
                    {
                        isMissingOnHub = true;

                        break;
                    }
                }

                if ( isMissingOnHub )
                {
                    environmentsMissingOnHub.add( localEnvironment );
                }
            }

            // destroy local env-s missing on Hub

            for ( Environment environment : environmentsMissingOnHub )
            {
                environmentManager.cleanupEnvironment( environment.getEnvironmentId() );

                environmentManager.notifyOnEnvironmentDestroyed( environment.getId() );
            }

            // notify Hub about environment deletion

            for ( String hubEnvironmentId : deletedEnvironmentsIdsOnHub )
            {
                environmentAdapter.removeEnvironment( hubEnvironmentId );
            }

            // 3. Remove deleted local env-s from Hub

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
