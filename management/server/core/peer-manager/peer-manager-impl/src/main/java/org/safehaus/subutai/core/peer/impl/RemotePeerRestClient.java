package org.safehaus.subutai.core.peer.impl;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.CommandCallback;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.PeerCommandMessage;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerInfo;
import org.safehaus.subutai.core.peer.api.RemotePeer;
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
public class RemotePeerRestClient implements RemotePeer
{

    private static final Logger LOG = LoggerFactory.getLogger( RemotePeerRestClient.class.getName() );
    private static final long DEFAULT_RECEIVE_TIMEOUT = 1000 * 60 * 5;
    private static final long CONNECTION_TIMEOUT = 1000 * 60 * 1;
    private final long receiveTimeout;
    private final long connectionTimeout;
    public final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String baseUrl = "http://%s:%s/cxf";


    public RemotePeerRestClient( String ip, String port )
    {
        this.receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;
        this.connectionTimeout = CONNECTION_TIMEOUT;

        baseUrl = String.format( baseUrl, ip, port );
        LOG.info( baseUrl );
    }


    public RemotePeerRestClient( long timeout, String ip, String port )
    {
        this.connectionTimeout = CONNECTION_TIMEOUT;
        this.receiveTimeout = timeout;

        baseUrl = String.format( baseUrl, ip, port );
        LOG.info( baseUrl );
    }


    public String getBaseUrl()
    {
        return baseUrl;
    }


    public void setBaseUrl( final String baseUrl )
    {
        this.baseUrl = baseUrl;
    }


    protected WebClient createWebClient()
    {
        WebClient client = WebClient.create( baseUrl );
        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout( connectionTimeout );
        httpClientPolicy.setReceiveTimeout( receiveTimeout );

        httpConduit.setClient( httpClientPolicy );
        return client;
    }


    public String callRemoteRest()
    {
        WebClient client = WebClient.create( baseUrl );
        String response = client.path( "peer/id" ).accept( MediaType.APPLICATION_JSON ).get( String.class );
        return response;
    }


    @Override
    public boolean isOnline() throws PeerException
    {
        return false;
    }


    @Override
    public UUID getId()
    {
        return null;
    }


    @Override
    public String getName()
    {
        return null;
    }


    @Override
    public UUID getOwnerId()
    {
        return null;
    }


    @Override
    public PeerInfo getPeerInfo()
    {
        return null;
    }


    @Override
    public Set<ContainerHost> getContainerHostsByEnvironmentId( final UUID environmentId ) throws PeerException
    {

        String path = "peer/environment/containers";

        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "environmentId", environmentId.toString() );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        String jsonObject = response.readEntity( String.class );
        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            Set<ContainerHost> result = JsonUtil.fromJson( jsonObject, new TypeToken<Set<ContainerHost>>()
            {}.getType() );
            return result;
        }

        if ( response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() )
        {
            throw new PeerException( response.getEntity().toString() );
        }
        else
        {
            return null;
        }
    }


    @Override
    public boolean startContainer( final ContainerHost containerHost ) throws PeerException
    {
        String path = "peer/container/start";

        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "host", JsonUtil.toJson( containerHost ) );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            return JsonUtil.fromJson( response.readEntity( String.class ), Boolean.class );
        }
        else
        {
            throw new PeerException( response.getEntity().toString() );
        }
    }


    @Override
    public boolean stopContainer( final ContainerHost containerHost ) throws PeerException
    {
        String path = "peer/container/stop";

        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "host", JsonUtil.toJson( containerHost ) );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            return JsonUtil.fromJson( response.readEntity( String.class ), Boolean.class );
        }
        else
        {
            throw new PeerException( response.getEntity().toString() );
        }
    }


    @Override
    public void destroyContainer( final ContainerHost containerHost ) throws PeerException
    {
        String path = "peer/container/destroy";

        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "host", JsonUtil.toJson( containerHost ) );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            return;
        }
        else
        {
            throw new PeerException( response.getEntity().toString() );
        }
    }


    @Override
    public boolean isConnected( final Host host ) throws PeerException
    {

        if ( !( host instanceof ContainerHost ) )
        {
            throw new PeerException( "Operation not allowed." );
        }
        String path = "peer/container/isconnected";


        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "host", JsonUtil.toJson( host ) );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            return JsonUtil.fromJson( response.readEntity( String.class ), Boolean.class );
        }
        else
        {
            throw new PeerException( response.getEntity().toString() );
        }
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host host ) throws CommandException
    {
        if ( !( host instanceof ContainerHost ) )
        {
            throw new CommandException( "Operation not allowed." );
        }

        String path = "peer/execute";

        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "requestBuilder", JsonUtil.toJson( requestBuilder ) );
        form.set( "host", JsonUtil.toJson( host ) );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        String jsonObject = response.readEntity( String.class );
        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            CommandResult result = JsonUtil.fromJson( jsonObject, CommandResult.class );
            return result;
        }

        if ( response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() )
        {
            throw new CommandException( response.getEntity().toString() );
        }
        else
        {
            throw new CommandException( "Unknown response: " + response.getEntity().toString() );
        }
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback )
            throws CommandException
    {
        return null;
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback )
            throws CommandException
    {

    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final Host host ) throws CommandException
    {

    }


    @Override
    public boolean isLocal()
    {
        return false;
    }


    @Override
    public Set<ContainerHost> createContainers( final UUID creatorPeerId, final UUID environmentId,
                                                final List<Template> templates, final int quantity,
                                                final String strategyId, final List<Criteria> criteria )
            throws ContainerCreateException
    {
        String path = "peer/container/create";

        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "ownerPeerId", creatorPeerId.toString() );
        form.set( "environmentId", environmentId.toString() );
        form.set( "templates", JsonUtil.toJson( templates ) );
        form.set( "quantity", quantity );
        form.set( "strategyId", strategyId );
        // TODO: implement criteria transfer
        form.set( "criteria", "" );

        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).form( form );

        String jsonObject = response.readEntity( String.class );
        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            Set<ContainerHost> result = JsonUtil.fromJson( jsonObject, new TypeToken<Set<ContainerHost>>()
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
