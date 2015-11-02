package io.subutai.core.peer.impl;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.RegistrationData;
import io.subutai.common.util.RestUtil;
import io.subutai.core.peer.api.RegistrationClient;


/**
 * REST client implementation of registration process
 */
public class RegistrationClientImpl implements RegistrationClient
{
    protected RestUtil restUtil = new RestUtil();
    private static final String urlTemplate = "https://%s:8443/rest/v1/registration/%s";
    private Object provider;


    public RegistrationClientImpl( final Object provider )
    {
        this.provider = provider;
    }


    @Override
    public RegistrationData sendInitRequest( final String destinationHost,
                                                final RegistrationData registrationData ) throws PeerException
    {
        WebClient client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "/register" ), provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        return client.post( registrationData, RegistrationData.class );
    }


    @Override
    public void sendCancelRequest( String destinationHost, final RegistrationData registrationData )
            throws PeerException
    {
        WebClient client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "/cancel" ), provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        Response response = client.post( registrationData );
        if ( response.getStatus() != Response.Status.NO_CONTENT.getStatusCode() )
        {
            throw new PeerException(
                    "Error on sending cancel registration request: " + response.getEntity().toString() );
        }
    }


    @Override
    public void sendRejectRequest( final String destinationHost, final RegistrationData registrationData )
            throws PeerException
    {
        WebClient client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "/reject" ), provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        Response response = client.post( registrationData );
        if ( response.getStatus() != Response.Status.NO_CONTENT.getStatusCode() )
        {
            throw new PeerException(
                    "Error on sending reject registration request: " + response.getEntity().toString() );
        }
    }


    @Override
    public void sendUnregisterRequest( final String destinationHost, final RegistrationData registrationData )
            throws PeerException
    {
        WebClient client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "/unregister" ), provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        Response response = client.post( registrationData );
        if ( response.getStatus() != Response.Status.NO_CONTENT.getStatusCode() )
        {
            throw new PeerException( "Error on sending un-register request: " + response.getEntity().toString() );
        }
    }


    @Override
    public RegistrationData sendApproveRequest( String destinationHost,
                                                   final RegistrationData registrationData ) throws PeerException
    {
        WebClient client = restUtil.getTrustedWebClient( buildUrl( destinationHost, "/approve" ), provider );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        return client.post( registrationData, RegistrationData.class );
    }


    private String buildUrl( String destination, String action )
    {
        return String.format( urlTemplate, destination, action );
    }
}
