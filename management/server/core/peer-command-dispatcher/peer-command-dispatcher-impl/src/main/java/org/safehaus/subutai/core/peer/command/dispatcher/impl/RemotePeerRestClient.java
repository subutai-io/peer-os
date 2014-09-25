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
    public final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String baseUrl = "http://%s:%s/cxf";


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


    public boolean createRemoteContainers( String ip, String port, CloneContainersMessage ccm )
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


    public boolean invoke( String ip, String port, PeerCommandMessage ccm )
    {
        String path = "peer/invoke";
        try
        {
            baseUrl = String.format( baseUrl, ip, port );
            LOG.info( baseUrl );

            WebClient client = WebClient.create( baseUrl );

            Form form = new Form();
            form.set( "commandType", ccm.getType().toString() );
            form.set( "command", ccm.toJson() );


            HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

            HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
            httpClientPolicy.setConnectionTimeout( 3000000 );
            httpClientPolicy.setReceiveTimeout( 3000000 );

            httpConduit.setClient( httpClientPolicy );

            Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                      .accept( MediaType.APPLICATION_JSON ).form( form );

            String jsonObject = response.readEntity( String.class );

            if ( response.getStatus() == Response.Status.OK.getStatusCode() )
            {
                //                LOG.info( response.toString() );
                LOG.info( response.getEntity().toString() );
                LOG.info( jsonObject );
                PeerCommandMessage result = JsonUtil.fromJson( jsonObject, ccm.getClass() );
                ccm.setResult( result.getResult() );
                LOG.info( String.format( "RESULT: %s", result.toString() ) );

                return true;
            }
            else
            {
                ccm.setResult( jsonObject );
                ccm.setSuccess( false );
                return false;
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }

        return false;
    }
}
