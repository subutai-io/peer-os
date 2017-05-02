package io.subutai.core.hubmanager.impl.processor;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import io.subutai.core.identity.api.model.UserToken;
import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandlerFactory;
import io.subutai.hub.share.dto.UserTokenDto;


public class UserTokenProcessor implements StateLinkProcessor
{
    private static final HashSet<String> LINKS_IN_PROGRESS = new HashSet<>();

    private final Context ctx;

    private final StateHandlerFactory handlerFactory;

    private final Logger log = LoggerFactory.getLogger( getClass() );


    public UserTokenProcessor( Context ctx )
    {
        this.ctx = ctx;

        handlerFactory = new StateHandlerFactory( ctx );
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
            if (userTokenDto != null)
            {
                //This process will get token, updates if expired
                ctx.envUserHelper.getUserTokenFromHub( userTokenDto.getSsUserId() );
            }
        }
        catch ( HubManagerException | PGPException | IOException e )
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
