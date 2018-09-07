package io.subutai.core.bazaarmanager.impl.processor;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.core.bazaarmanager.api.StateLinkProcessor;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;
import io.subutai.bazaar.share.dto.UserTokenDto;


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
    public boolean processStateLinks( final Set<String> stateLinks ) throws BazaarManagerException
    {
        for ( String link : stateLinks )
        {
            if ( link.contains( "token" ) )
            {
                processStateLink( link );
            }
        }

        return false;
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
            UserTokenDto userTokenDto = ctx.restClient.getStrict( link, UserTokenDto.class );
            if ( userTokenDto != null )
            {
                //This process will get token, updates if expired
                ctx.envUserHelper.getUserTokenFromBazaar( userTokenDto.getSsUserId() );
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
