package io.subutai.core.hubmanager.impl.processor;


import java.util.HashSet;
import java.util.Set;

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

    private final String linkPattern;

    private final Logger log = LoggerFactory.getLogger( getClass() );


    public UserTokenProcessor( Context ctx )
    {
        this.ctx = ctx;

        handlerFactory = new StateHandlerFactory( ctx );

        linkPattern = "/rest/v1/environments/.*/peers/" + ctx.localPeer.getId();
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws HubManagerException
    {
        boolean fastMode = false;

        for ( String link : stateLinks )
        {
            if ( link.matches( linkPattern ) )
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
            if (userTokenDto.getState().equals( UserTokenDto.State.UPDATE ))
            {
                //TODO update token
            }
            else if (userTokenDto.getState().equals( UserTokenDto.State.DELETE ))
            {
                //TODO delete token
            }
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }
        finally
        {
            log.info( "Link process - END: {}", link );

            LINKS_IN_PROGRESS.remove( link );
        }

    }

    public void updateToken()
    {



    }

    public void deleteToken()
    {

    }
}
