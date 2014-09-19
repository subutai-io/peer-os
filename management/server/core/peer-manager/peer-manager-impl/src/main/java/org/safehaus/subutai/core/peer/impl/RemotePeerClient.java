package org.safehaus.subutai.core.peer.impl;


import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;


/**
 * Created by bahadyr on 9/18/14.
 */
public class RemotePeerClient {


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
}
