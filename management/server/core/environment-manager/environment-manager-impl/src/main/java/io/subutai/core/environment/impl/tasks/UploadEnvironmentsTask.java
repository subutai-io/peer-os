package io.subutai.core.environment.impl.tasks;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.adapter.EnvironmentAdapter;
import io.subutai.core.environment.impl.dao.EnvironmentService;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;


public class UploadEnvironmentsTask implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( UploadEnvironmentsTask.class );

    private final EnvironmentAdapter environmentAdapter;
    private final IdentityManager identityManager;
    private final EnvironmentService environmentService;
    private final EnvironmentManagerImpl environmentManager;


    public UploadEnvironmentsTask( final EnvironmentAdapter environmentAdapter, final IdentityManager identityManager,
                                   final EnvironmentService environmentService,
                                   final EnvironmentManagerImpl environmentManager )
    {
        this.environmentAdapter = environmentAdapter;
        this.identityManager = identityManager;
        this.environmentService = environmentService;
        this.environmentManager = environmentManager;
    }


    @Override
    public void run()
    {
        //0. check if peer is registered with bazaar and bazaar is reachable
        if ( !environmentAdapter.canWorkWithBazaar() )
        {
            return;
        }


        //1. obtain peer owner
        User peerOwner = identityManager.getUserByKeyId( identityManager.getPeerOwnerId() );


        //2. filter out not peer owner's environments or uploaded environments
        Set<LocalEnvironment> envs = new HashSet<>();

        envs.addAll( environmentService.getAll() );

        for ( Iterator<LocalEnvironment> iterator = envs.iterator(); iterator.hasNext(); )
        {
            final LocalEnvironment environment = iterator.next();

            if ( environment.isUploaded() || !Objects.equals( environment.getUserId(), peerOwner.getId() ) )
            {
                iterator.remove();
            }
        }

        if ( envs.isEmpty() )
        {
            return;
        }


        //3. upload them to bazaar
        Set<Environment> environments = Sets.newHashSet();

        environments.addAll( envs );

        environmentManager.setTransientFields( environments );

        for ( LocalEnvironment environment : envs )
        {
            try
            {
                if ( environmentAdapter.uploadPeerOwnerEnvironment( environment ) )
                {
                    environment.markAsUploaded();

                    environmentService.merge( environment );
                }
            }
            catch ( Exception e )
            {
                LOG.error( "Error uploading environment {} to Bazaar: {}", environment.getName(), e.getMessage() );
            }
        }
    }
}
