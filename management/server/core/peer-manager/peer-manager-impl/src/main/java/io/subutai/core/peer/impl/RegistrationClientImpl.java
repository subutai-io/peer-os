package io.subutai.core.peer.impl;


import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.RegistrationData;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.common.security.WebClientBuilder;
import io.subutai.common.util.RestUtil;
import io.subutai.core.peer.api.RegistrationClient;


/**
 * REST client implementation of registration process
 */
public class RegistrationClientImpl implements RegistrationClient
{
    private static final Logger LOG = LoggerFactory.getLogger( RegistrationClientImpl.class );

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

        WebClient client = null;
        Response response;

        try
        {
            client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "info" ), provider );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );

            response = client.get();
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Can not connect to '%s'.", destinationHost ) );
        }
        finally
        {
            WebClientBuilder.close( client );
        }

        return WebClientBuilder.checkResponse( response, PeerInfo.class );
    }


    @Override
    public RegistrationData sendInitRequest( final String destinationHost, final RegistrationData registrationData )
            throws PeerException
    {
        WebClient client = null;
        Response response;

        try
        {
            client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "register" ), provider );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );

            response = client.post( registrationData );
        }
        catch ( Exception e )
        {
            throw new PeerException(
                    String.format( "Error requesting remote peer '%s': %s.", destinationHost, e.getMessage() ) );
        }
        finally
        {
            WebClientBuilder.close( client );
        }

        return WebClientBuilder.checkResponse( response, RegistrationData.class );
    }


    @Override
    public void sendCancelRequest( String destinationHost, final RegistrationData registrationData )
            throws PeerException
    {
        WebClient client = null;
        Response response;

        try
        {
            client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "cancel" ), provider );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );

            response = client.post( registrationData );
        }
        catch ( Exception e )
        {
            throw new PeerException(
                    String.format( "Error requesting remote peer '%s': %s.", destinationHost, e.getMessage() ) );
        }
        finally
        {
            WebClientBuilder.close( client );
        }

        WebClientBuilder.checkResponse( response, Response.Status.OK );
    }


    @Override
    public void sendRejectRequest( final String destinationHost, final RegistrationData registrationData )
            throws PeerException
    {
        WebClient client = null;
        Response response;

        try
        {
            client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "reject" ), provider );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );

            response = client.post( registrationData );
        }
        catch ( Exception e )
        {
            throw new PeerException(
                    String.format( "Error requesting remote peer '%s': %s.", destinationHost, e.getMessage() ) );
        }
        finally
        {
            WebClientBuilder.close( client );
        }

        WebClientBuilder.checkResponse( response, Response.Status.OK );
    }


    @Override
    public void sendUnregisterRequest( final String destinationHost, final RegistrationData registrationData )
            throws PeerException
    {
        WebClient client = null;
        Response response;

        try
        {
            client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "unregister" ), provider );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );

            response = client.post( registrationData );
        }
        catch ( Exception e )
        {
            throw new PeerException(
                    String.format( "Error requesting remote peer '%s': %s.", destinationHost, e.getMessage() ) );
        }
        finally
        {
            WebClientBuilder.close( client );
        }

        WebClientBuilder.checkResponse( response, Response.Status.OK );
    }


    @Override
    public void sendApproveRequest( String destinationHost, final RegistrationData registrationData )
            throws PeerException
    {
        WebClient client = null;
        Response response;

        try
        {
            client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "approve" ), provider );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );

            response = client.post( registrationData );
        }
        catch ( Exception e )
        {
            throw new PeerException(
                    String.format( "Error requesting remote peer '%s': %s.", destinationHost, e.getMessage() ) );
        }
        finally
        {
            WebClientBuilder.close( client );
        }

        WebClientBuilder.checkResponse( response, Response.Status.OK );
    }


    @Override
    public RegistrationStatus getStatus( String destinationHost, String peerId )
    {
        WebClient client = null;
        Response response;

        try
        {
            client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "status/" + peerId ), provider );
            client.type( MediaType.APPLICATION_JSON );
            client.accept( MediaType.APPLICATION_JSON );

            response = client.get();

            return WebClientBuilder.checkResponse( response, RegistrationStatus.class );
        }
        catch ( Exception e )
        {
            LOG.warn( e.getMessage() );
        }
        finally
        {
            WebClientBuilder.close( client );
        }

        return RegistrationStatus.OFFLINE;
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
