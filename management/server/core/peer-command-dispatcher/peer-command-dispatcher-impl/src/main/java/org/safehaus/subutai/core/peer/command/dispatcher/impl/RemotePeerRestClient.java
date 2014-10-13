package org.safehaus.subutai.core.peer.command.dispatcher.impl;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.common.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Created by bahadyr on 9/21/14.
 */
public class RemotePeerRestClient
{

    private static final Logger LOG = LoggerFactory.getLogger( RemotePeerRestClient.class.getName() );
    private static final long RECEIVE_TIMEOUT = 1000 * 60 * 5;
    private static final long CONNECTION_TIMEOUT = 1000 * 60 * 5;
    private final long receiveTimeout;
    private final long connectionTimeout;
    public final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String baseUrl = "http://%s:%s/cxf";


    public RemotePeerRestClient()
    {
        this.receiveTimeout = RECEIVE_TIMEOUT;
        this.connectionTimeout = CONNECTION_TIMEOUT;
    }


    public RemotePeerRestClient( long timeout )
    {
        this.connectionTimeout = 1000 * 60; // 15 sec
        this.receiveTimeout = timeout - this.connectionTimeout;
    }


    public String getBaseUrl()
    {
        return baseUrl;
    }


    public void setBaseUrl( final String baseUrl )
    {
        this.baseUrl = baseUrl;
    }


    public String callRemoteRest()
    {
        WebClient client = WebClient.create( baseUrl );
        String response = client.path( "peer/id" ).accept( MediaType.APPLICATION_JSON ).get( String.class );
        return response;
    }


    private boolean createRemoteContainers( String ip, String port, CloneContainersMessage ccm )
    {
        String path = "peer/containers";
        try
        {
            baseUrl = String.format( baseUrl, ip, port );
            LOG.info( baseUrl );
            WebClient client = WebClient.create( baseUrl );
            String ccmString = GSON.toJson( ccm, CloneContainersMessage.class );

            Response response = client.path( path ).type( MediaType.TEXT_PLAIN ).accept( MediaType.APPLICATION_JSON )
                                      .post( ccmString );

            if ( response.getStatus() == Response.Status.OK.getStatusCode() )
            {
                //                JsonObject jsonObject = ( JsonObject ) response.get;

                LOG.info( response.toString() );
                return true;
            }
            return false;
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }

        return false;
    }


    public void invoke( String ip, String port, PeerCommandMessage peerCommandMessage )
    {
        String path = "peer/invoke";
        try
        {
            baseUrl = String.format( baseUrl, ip, port );
            LOG.info( baseUrl );

            WebClient client = WebClient.create( baseUrl );

            Form form = new Form();
            form.set( "commandType", peerCommandMessage.getType().toString() );
            form.set( "command", peerCommandMessage.toJson() );


            HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

            HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
            httpClientPolicy.setConnectionTimeout( connectionTimeout );
            httpClientPolicy.setReceiveTimeout( receiveTimeout );

            httpConduit.setClient( httpClientPolicy );

            Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                      .accept( MediaType.APPLICATION_JSON ).form( form );

            String jsonObject = response.readEntity( String.class );
            PeerCommandMessage result = JsonUtil.fromJson( jsonObject, peerCommandMessage.getClass() );

            if ( response.getStatus() == Response.Status.OK.getStatusCode() )
            {
                peerCommandMessage.setResult( result.getResult() );
//                peerCommandMessage.setSuccess( result.isSuccess() );
                LOG.debug( String.format( "Remote command result: %s", result.toString() ) );
//                return ccm;
            }
            else
            {
//                peerCommandMessage.setSuccess( false );
                peerCommandMessage.setExceptionMessage( result.getExceptionMessage() );
//                return ccm;
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
//            peerCommandMessage.setSuccess( false );
            peerCommandMessage.setExceptionMessage( e.toString() );
//            throw new RuntimeException( "Error while invoking REST Client" );
        }

        //return null;
    }
}
