package io.subutai.core.bazaarmanager.impl.processor;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.util.TaskUtil;
import io.subutai.core.bazaarmanager.api.StateLinkProcessor;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;
import io.subutai.core.bazaarmanager.impl.environment.state.StateHandler;
import io.subutai.core.bazaarmanager.impl.environment.state.StateHandlerFactory;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto;


public class BazaarEnvironmentProcessor implements StateLinkProcessor
{
    private static final Set<String> LINKS_IN_PROGRESS = Sets.newConcurrentHashSet();

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final Context ctx;

    private final StateHandlerFactory handlerFactory;

    private final String linkPattern;


    public BazaarEnvironmentProcessor( Context ctx )
    {
        this.ctx = ctx;

        handlerFactory = new StateHandlerFactory( ctx );

        linkPattern = "/rest/v1/environments/.*/peers/" + ctx.localPeer.getId();
    }


    @Override
    public boolean processStateLinks( Set<String> stateLinks ) throws BazaarManagerException
    {
        boolean fastMode = false;

        TaskUtil<Object> taskUtil = new TaskUtil<>();

        for ( final String link : stateLinks )
        {
            if ( link.matches( linkPattern ) )
            {
                fastMode = true;

                taskUtil.addTask( new TaskUtil.Task<Object>()
                {
                    @Override
                    public Object call() throws Exception
                    {
                        processStateLink( link );

                        return null;
                    }
                } );
            }
        }

        if ( taskUtil.hasTasks() )
        {
            TaskUtil.TaskResults<Object> taskResults = taskUtil.executeParallel();

            for ( TaskUtil.TaskResult<Object> taskResult : taskResults.getResults() )
            {
                if ( !taskResult.hasSucceeded() )
                {
                    log.error( "Error processing state link: {}", taskResult.getFailureReason() );
                }
            }
        }

        return fastMode;
    }


    private void processStateLink( String link ) throws BazaarManagerException
    {
        log.info( "Link process - START: {}", link );

        if ( LINKS_IN_PROGRESS.contains( link ) )
        {
            log.info( "This link is in progress: {}", link );

            return;
        }

        LINKS_IN_PROGRESS.add( link );

        try
        {
            EnvironmentPeerDto peerDto = ctx.restClient.getStrict( link, EnvironmentPeerDto.class );

            StateHandler handler = handlerFactory.getHandler( peerDto.getState() );

            handler.handle( peerDto );
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }
        finally
        {
            log.info( "Link process - END: {}", link );

            LINKS_IN_PROGRESS.remove( link );
        }
    }
}
