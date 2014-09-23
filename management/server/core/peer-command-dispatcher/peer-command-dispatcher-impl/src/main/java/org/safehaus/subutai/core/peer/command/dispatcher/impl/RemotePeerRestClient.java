package org.safehaus.subutai.core.peer.command.dispatcher.impl;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
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
        LOG.info( "create remote container called" );
        try
        {
            WebClient client = WebClient.create( String.format( baseUrl, ip, port ) );
            String ccmString = GSON.toJson( ccm, CloneContainersMessage.class );

            Response response = client.path( path ).type( MediaType.TEXT_PLAIN ).accept( MediaType.APPLICATION_JSON )
                                      .post( ccmString );

            LOG.info( response.toString() );

            return true;
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }

        return false;
    }
}
