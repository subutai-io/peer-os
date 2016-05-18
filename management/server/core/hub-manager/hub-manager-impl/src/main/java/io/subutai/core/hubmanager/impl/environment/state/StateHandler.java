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


    private void runAs( EnvironmentPeerDto envPeerDto )
    {
        try
        {
            Object result = doHandle( envPeerDto );

            post( envPeerDto, result );
        }
        catch ( Exception e )
        {
            log.error( "Error to handle environment peer data: ", e );
        }
    }


    protected void post( EnvironmentPeerDto envPeerDto, Object body )
    {
        String path = String.format( "/rest/v1/environments/%s/peers/%s", envPeerDto.getEnvironmentInfo().getId(), envPeerDto.getPeerId() );

        ctx.restClient.post( path, body );
    }
}
