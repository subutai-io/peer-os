package io.subutai.core.hubmanager.impl.processor;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.hub.share.dto.UserTokenDto;


public class UserTokenProcessor implements StateLinkProcessor
{
    private static final Set<String> LINKS_IN_PROGRESS = Sets.newConcurrentHashSet();

    private final Context ctx;

    private final Logger log = LoggerFactory.getLogger( getClass() );


    public UserTokenProcessor( Context ctx )
    {
        this.ctx = ctx;
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws HubManagerException
    {
        boolean fastMode = false;

        for ( String link : stateLinks )
        {
            if ( link.contains( "token" ) )
            {
                fastMode = true;

                processStateLink( link );
            }
        }

        return fastMode;
    }


    private void processStateLink( String link ) throws HubManagerException
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
            UserTokenDto userTokenDto = ctx.restClient.getStrict( link, UserTokenDto.class );
            if ( userTokenDto != null )
            {
                //This process will get token, updates if expired
                ctx.envUserHelper.getUserTokenFromHub( userTokenDto.getSsUserId() );
            }
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
        finally
        {
            log.info( "Link process - END: {}", link );

            LINKS_IN_PROGRESS.remove( link );
        }
    }
}
