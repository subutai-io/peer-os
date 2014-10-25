package org.safehaus.subutai.core.peer.impl;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.strategy.api.Criteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Remote Peer REST client
 */
public class RemotePeerRestClient
{

    private static final Logger LOG = LoggerFactory.getLogger( RemotePeerRestClient.class.getName() );
    private static final long RECEIVE_TIMEOUT = 1000 * 60 * 5;
    private static final long CONNECTION_TIMEOUT = 1000 * 60 * 1;
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
        this.connectionTimeout = CONNECTION_TIMEOUT;
        this.receiveTimeout = timeout;
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


    public Set<ContainerHost> createRemoteContainers( String ip, String port, UUID ownerPeerId, UUID environemntId,
                                                      List<Template> templates, int quantity, String strategyId,
                                                      List<Criteria> criteria ) throws ContainerCreateException
    {
        String path = "peer/container/create";

        baseUrl = String.format( baseUrl, ip, port );
        LOG.info( baseUrl );

        WebClient client = WebClient.create( baseUrl );

        Template template = templates.get( 0 );
        Form form = new Form();
        form.set( "ownerPeerId", ownerPeerId.toString() );
        form.set( "environmentId", environemntId.toString() );
        form.set( "templates", JsonUtil.toJson( template ) );
        form.set( "quantity", quantity );
        form.set( "strategyId", strategyId );
        // TODO: implement criteria transfer
        form.set( "criteria", "" );


        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout( connectionTimeout );
        httpClientPolicy.setReceiveTimeout( receiveTimeout );

        httpConduit.setClient( httpClientPolicy );

        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).form( form );

        String jsonObject = response.readEntity( String.class );
        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            Set<ContainerHost> result = JsonUtil.fromJson( jsonObject, new TypeToken<Set<ContainerHostImpl>>()
            {
            }.getType() );
            return result;
        }

        if ( response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() )
        {
            throw new ContainerCreateException( response.getEntity().toString() );
        }
        else
        {
            return null;
        }
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
