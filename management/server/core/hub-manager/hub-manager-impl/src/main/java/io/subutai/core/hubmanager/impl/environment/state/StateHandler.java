package io.subutai.core.hubmanager.impl.environment.state;


import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.core.identity.api.model.Session;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState;

import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.READY;
import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.WAIT;


public abstract class StateHandler
{
    private static final String PATH = "/rest/v1/environments/%s/peers/%s";

    /**
     * Map of <envId, state>. Used to prevent duplicated handling of states.
     */
    private static final Map<String, PeerState> envLastStates = Collections.synchronizedMap( new HashMap<String, PeerState>() );

    protected final Logger log = LoggerFactory.getLogger( getClass() );

    protected final Context ctx;

    private String description;


    protected StateHandler( Context ctx, String description )
    {
        this.ctx = ctx;
        this.description = description;
    }


    protected abstract Object doHandle( EnvironmentPeerDto peerDto ) throws Exception;


    public void handle( final EnvironmentPeerDto peerDto )
    {
        String token = getToken( peerDto );

        Session session = ctx.identityManager.login( "token", token );

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


    /**
     * Most of operations are performed with a Hub user account, i.e. using its token. But for some operations a peer token may be required.
     * For example, removing a user account after an environment destroy.
     */
    protected String getToken( EnvironmentPeerDto peerDto )
    {
        return StringUtils.defaultIfEmpty( peerDto.getEnvOwnerToken(), peerDto.getPeerToken() );
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

            RestResult<Object> restResult = post( peerDto, result );

            if ( restResult.isSuccess() )
            {
                onSuccess( peerDto );
            }
        }
        catch ( Exception e )
        {
            log.error( "Failed to process environment state: ", e );

            handleError( peerDto, e );
        }
    }


    private boolean canIgnoreState( EnvironmentPeerDto peerDto )
    {
        PeerState state = peerDto.getState();

        return state == WAIT || state == READY || envLastStates.get( peerDto.getEnvironmentInfo().getId() ) == state;
    }


    protected void onSuccess( EnvironmentPeerDto peerDto )
    {
        envLastStates.put( peerDto.getEnvironmentInfo().getId(), peerDto.getState() );
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
        return StringUtils.countMatches( format, "%s" ) == 1
            ? String.format( format, peerDto.getEnvironmentInfo().getId() )
            : String.format( format, peerDto.getEnvironmentInfo().getId(), peerDto.getPeerId() );
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
