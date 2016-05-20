package io.subutai.core.hubmanager.impl.environment.state;


import java.security.PrivilegedAction;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import io.subutai.core.identity.api.model.Session;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public abstract class StateHandler
{
    private static final String PATH = "/rest/v1/environments/%s/peers/%s";

    protected final Logger log = LoggerFactory.getLogger( getClass() );

    protected final Context ctx;


    protected StateHandler( Context ctx )
    {
        this.ctx = ctx;
    }


    protected abstract Object doHandle( EnvironmentPeerDto peerDto ) throws Exception;


    public void handle( final EnvironmentPeerDto peerDto )
    {
        log.info( "envPeerDto.state: {}", peerDto.getState() );

        String token = StringUtils.defaultIfEmpty( peerDto.getEnvOwnerToken(), peerDto.getPeerToken() );

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


    private void runAs( EnvironmentPeerDto peerDto )
    {
        try
        {
            Object result = doHandle( peerDto );

            post( peerDto, result );
        }
        catch ( Exception e )
        {
            log.error( "Error to handle environment peer data: ", e );

            handleError( peerDto, e );
        }
    }


    protected void post( EnvironmentPeerDto peerDto, Object body )
    {
        ctx.restClient.post( path( PATH, peerDto ), body );
    }


    protected void handleError( EnvironmentPeerDto peerDto, Exception e )
    {
        peerDto.setError( e.getMessage() );

        ctx.restClient.post( path( PATH, peerDto ), peerDto );
    }

    protected String path( String format, EnvironmentPeerDto peerDto )
    {
        return StringUtils.countMatches( format, "%s" ) == 1
            ? String.format( format, peerDto.getEnvironmentInfo().getId() )
            : String.format( format, peerDto.getEnvironmentInfo().getId(), peerDto.getPeerId() );
    }
}
