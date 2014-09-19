package org.safehaus.subutai.core.peer.impl;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.core.peer.api.helpers.CreateContainersMessage;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Created by bahadyr on 9/18/14.
 */
public class RemotePeerClient {


    public final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String baseUrl;


    public String getBaseUrl() {
        return baseUrl;
    }


    public void setBaseUrl( final String baseUrl ) {
        this.baseUrl = baseUrl;
    }


    public String callRemoteRest() {
        WebClient client = WebClient.create( baseUrl );
        String response = client.path( "peer/id" ).accept( MediaType.APPLICATION_JSON ).get( String.class );
        return response;
    }


    public String createRemoteContainers( CreateContainersMessage ccm ) {
        WebClient client = WebClient.create( baseUrl );
        String ccmString = GSON.toJson( ccm, CreateContainersMessage.class );

        Response response = client.path( "peer/container" ).accept( MediaType.APPLICATION_JSON ).post( ccmString );

        return response.toString();
    }
}
