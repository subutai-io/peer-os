package org.safehaus.subutai.core.peer.command.dispatcher.impl;


import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

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
        String path = "invoke";
        try
        {
            baseUrl = String.format( baseUrl, ip, port );
            LOG.info( baseUrl );
            WebClient client = WebClient.create( baseUrl );

            Form form = new Form();
            form.param( "commandType", ccm.getType().toString() );
            form.param( "command", ccm.toJson() );
            Response response =
                    client.path( path ).type( MediaType.TEXT_PLAIN ).accept( MediaType.APPLICATION_JSON ).post( form );

            if ( response.getStatus() == Response.Status.OK.getStatusCode() )
            {
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
}
