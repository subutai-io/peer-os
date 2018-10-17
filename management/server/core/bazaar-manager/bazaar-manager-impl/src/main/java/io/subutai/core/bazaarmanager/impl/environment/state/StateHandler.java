package io.subutai.core.bazaarmanager.impl.environment.state;


import java.security.PrivilegedAction;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto.PeerState;

import static io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto.PeerState.READY;
import static io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto.PeerState.WAIT;


public abstract class StateHandler
{
    private static final String PATH = "/rest/v1/environments/%s/peers/%s";

    protected final Logger log = LoggerFactory.getLogger( getClass() );

    protected final Context ctx;

    private String description;


    protected StateHandler( Context ctx, String description )
    {
        this.ctx = ctx;
        this.description = description;
    }


    protected abstract Object doHandle( EnvironmentPeerDto peerDto ) throws BazaarManagerException;


    public void handle( final EnvironmentPeerDto peerDto )
    {
        String token = getToken( peerDto );


        Session session = ctx.identityManager.login( IdentityManager.TOKEN_ID, token );

        if ( session != null )
        {
            Subject.doAs( session.getSubject(), new PrivilegedAction<Void>()
            {
                @Override
                public Void run()
                {
                    runAs( peerDto );

                    return null;
                }
            } );
        }
        else
        {
            log.warn( "Probably, environment has been deleted, no user to perform environment operation" );
        }
    }


    /**
     * Most of operations are performed with abazaar user account, i.e. using its token. But for some operations a peer
     * token may be required. For example, removing a user account after an environment destroy.
     */
    protected String getToken( EnvironmentPeerDto peerDto )
    {
        try
        {
            UserToken userToken = ctx.envUserHelper.getUserTokenFromBazaar( peerDto.getEnvironmentInfo().getSsOwnerId() );
            return userToken.getFullToken();
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
        return null;
    }


    private void runAs( EnvironmentPeerDto peerDto )
    {
        if ( canIgnoreState( peerDto ) )
        {
            log.info( "Ignoring state: {}", peerDto.getState() );

            return;
        }

        try
        {
            Object result = doHandle( peerDto );

            post( peerDto, result );
        }
        catch ( Exception e )
        {
            log.error( "Failed to process environment state: ", e );

            handleError( peerDto, e );
        }
    }


    protected boolean canIgnoreState( EnvironmentPeerDto peerDto )
    {
        PeerState state = peerDto.getState();

        return state == WAIT || state == READY;
    }


    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.post( path( PATH, peerDto ), body );
    }


    protected void handleError( EnvironmentPeerDto peerDto, Exception e )
    {
        peerDto.setError( description + ". " + e.getMessage() );

        ctx.restClient.post( path( PATH, peerDto ), peerDto );
    }


    protected String path( String format, EnvironmentPeerDto peerDto )
    {
        return StringUtils.countMatches( format, "%s" ) == 1 ?
               String.format( format, peerDto.getEnvironmentInfo().getId() ) :
               String.format( format, peerDto.getEnvironmentInfo().getId(), peerDto.getPeerId() );
    }


    protected void logStart()
    {
        log.info( "{} - START", description );
    }


    protected void logEnd()
    {
        log.info( "{} - END", description );
    }
}
