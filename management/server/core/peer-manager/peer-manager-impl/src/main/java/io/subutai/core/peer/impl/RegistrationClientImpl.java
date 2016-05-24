package io.subutai.core.peer.impl;


import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.RegistrationData;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.common.util.IPUtil;
import io.subutai.core.peer.api.RegistrationClient;


/**
 * REST client implementation of registration process
 */
public class RegistrationClientImpl implements RegistrationClient
{
    protected RestUtil restUtil = new RestUtil();
    private static final String urlTemplate = "%s/rest/v1/handshake/%s";
    private Object provider;


    public RegistrationClientImpl( final Object provider )
    {

        Preconditions.checkNotNull( provider );

        this.provider = provider;
    }


    @Override
    public PeerInfo getPeerInfo( final String destinationHost ) throws PeerException
    {

        WebClient client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "/info" ), provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            Response response = client.get();

            String s = response.readEntity( String.class );
            if ( response.getStatus() != Response.Status.OK.getStatusCode() )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return JsonUtil.fromJson( s, PeerInfo.class );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Can not connect to '%s'.", destinationHost ) );
        }
    }


    @Override
    public RegistrationData sendInitRequest( final String destinationHost, final RegistrationData registrationData )
            throws PeerException
    {
        WebClient client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "/register" ), provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            Response response = client.post( registrationData );
            if ( response.getStatus() != Response.Status.OK.getStatusCode() )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
            else
            {
                return response.readEntity( RegistrationData.class );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException(
                    String.format( "Error requesting remote peer '%s': %s.", destinationHost, e.getMessage() ) );
        }
    }


    @Override
    public void sendCancelRequest( String destinationHost, final RegistrationData registrationData )
            throws PeerException
    {
        WebClient client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "/cancel" ), provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            Response response = client.post( registrationData );
            if ( response.getStatus() != Response.Status.OK.getStatusCode() )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException(
                    String.format( "Error requesting remote peer '%s': %s.", destinationHost, e.getMessage() ) );
        }
    }


    @Override
    public void sendRejectRequest( final String destinationHost, final RegistrationData registrationData )
            throws PeerException
    {
        WebClient client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "/reject" ), provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            Response response = client.post( registrationData );
            if ( response.getStatus() != Response.Status.OK.getStatusCode() )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException(
                    String.format( "Error requesting remote peer '%s': %s.", destinationHost, e.getMessage() ) );
        }
    }


    @Override
    public void sendUnregisterRequest( final String destinationHost, final RegistrationData registrationData )
            throws PeerException
    {
        WebClient client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "/unregister" ), provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            Response response = client.post( registrationData );
            if ( response.getStatus() != Response.Status.OK.getStatusCode() )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException(
                    String.format( "Error requesting remote peer '%s': %s.", destinationHost, e.getMessage() ) );
        }
    }


    @Override
    public void sendApproveRequest( String destinationHost, final RegistrationData registrationData )
            throws PeerException
    {
        WebClient client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "/approve" ), provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        try
        {
            Response response = client.post( registrationData );
            if ( response.getStatus() != Response.Status.OK.getStatusCode() )
            {
                throw new PeerException( response.readEntity( String.class ) );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException(
                    String.format( "Error requesting remote peer '%s': %s.", destinationHost, e.getMessage() ) );
        }
    }


    private String buildUrl( String destination, String action ) throws PeerException
    {
        try
        {
            URL url = new URL( destination );
            return String.format( urlTemplate, url, action );
        }
        catch ( MalformedURLException e )
        {
            throw new PeerException( "Invalid URL." );
        }
    }

}
