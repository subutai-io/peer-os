package org.safehaus.subutai.core.peer.command.dispatcher.impl;


import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.core.peer.api.helpers.CloneContainersMessage;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Created by bahadyr on 9/21/14.
 */
public class RemotePeerRestClient
{

//    executeRemoteCommand(C)

    private static final Logger LOG = Logger.getLogger( RemotePeerRestClient.class.getName() );
        public final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        private String baseUrl;


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


        public String createRemoteContainers( CloneContainersMessage ccm )
        {
            try
            {
                WebClient client = WebClient.create( baseUrl );
                String ccmString = GSON.toJson( ccm, CloneContainersMessage.class );

                Response response =
                        client.path( "peer/containers" ).type( MediaType.TEXT_PLAIN ).accept( MediaType.APPLICATION_JSON )
                              .post( ccmString );

                return response.toString();
            }
            catch ( Exception e )
            {
                LOG.log( Level.SEVERE, e.getMessage() );
            }

            return null;
        }

}
