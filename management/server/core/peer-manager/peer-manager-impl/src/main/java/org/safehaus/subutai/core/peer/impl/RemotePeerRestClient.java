package org.safehaus.subutai.core.peer.impl;


import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.HostInfoModel;
import org.safehaus.subutai.core.peer.api.PeerException;
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
    private static final long DEFAULT_RECEIVE_TIMEOUT = 1000 * 60 * 5;
    private static final long CONNECTION_TIMEOUT = 1000 * 60;
    private final long receiveTimeout;
    private final long connectionTimeout;
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
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
            return JsonUtil.fromJson( jsonObject, new TypeToken<Set<ContainerHost>>()
            {}.getType() );
        }

        if ( response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() )
        {
            throw new PeerException( response.getEntity().toString() );
        }
        else
        {
            return Collections.emptySet();
        }
    }


    public void stopContainer( final ContainerHost containerHost ) throws PeerException
    {
        String path = "peer/container/stop";

        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "hostId", containerHost.getId().toString() );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        if ( response.getStatus() != Response.Status.OK.getStatusCode() )
        {
            throw new PeerException( response.getEntity().toString() );
        }
    }


    public void startContainer( final ContainerHost containerHost ) throws PeerException
    {
        String path = "peer/container/start";

        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "hostId", containerHost.getId().toString() );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        if ( response.getStatus() != Response.Status.OK.getStatusCode() )
        {
            throw new PeerException( response.getEntity().toString() );
        }
    }


    public void destroyContainer( final ContainerHost containerHost ) throws PeerException
    {
        String path = "peer/container/destroy";

        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "hostId", containerHost.getId().toString() );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        if ( response.getStatus() != Response.Status.OK.getStatusCode() )
        {
            throw new PeerException( response.getEntity().toString() );
        }
    }


    public boolean isConnected( final Host host )
    {
        String path = "peer/container/isconnected";


        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "hostId", host.getId().toString() );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            return JsonUtil.fromJson( response.readEntity( String.class ), Boolean.class );
        }
        else
        {
            LOG.error( response.getEntity().toString() );
            return false;
        }
    }


    public Template getTemplate( final String templateName ) throws PeerException
    {
        String path = "peer/template/get";

        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "templateName", templateName );
        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            return JsonUtil.fromJson( response.readEntity( String.class ), Template.class );
        }
        else
        {
            throw new PeerException( "Could not retrieve remote template.", response.getEntity().toString() );
        }
    }


    public UUID getId() throws PeerException
    {
        String path = "peer/id";
        try
        {
            WebClient client = createWebClient();

            Response response = client.path( path ).accept( MediaType.TEXT_PLAIN ).get();

            if ( response.getStatus() == Response.Status.OK.getStatusCode() )
            {
                return UUID.fromString( response.readEntity( String.class ) );
            }
            else
            {
                throw new PeerException( "Could not retrieve remote peer ID." );
            }
        }
        catch ( Exception ce )
        {
            throw new PeerException( "Could not retrieve remote peer ID.", ce.toString() );
        }
    }


    public Set<HostInfoModel> scheduleCloneContainers( final UUID creatorPeerId, final List<Template> templates,
                                                       final int quantity, final String strategyId,
                                                       final List<Criteria> criteria ) throws PeerException
    {
        String path = "peer/container/schedule";

        WebClient client = createWebClient();

        Form form = new Form();
        form.set( "creatorPeerId", creatorPeerId );
        form.set( "templates", JsonUtil.toJson( templates ) );
        form.set( "quantity", quantity );
        form.set( "strategyId", strategyId );
        form.set( "criteria", JsonUtil.toJson( criteria ) );

        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
                                  .accept( MediaType.APPLICATION_JSON ).post( form );

        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            return JsonUtil.fromJson( response.readEntity( String.class ), new TypeToken<Set<HostInfoModel>>()
            {}.getType() );
        }
        else
        {
            throw new PeerException( "Could not clone remote containers.", response.getEntity().toString() );
        }
    }


    public void setQuota( ContainerHost host, QuotaInfo quotaInfo ) throws PeerException
    {
        String path = "peer/container/quota";

        WebClient client = createWebClient();

        Form form = new Form();

        form.set( "hostId", host.getId().toString() );
        form.set( "quotaInfo", JsonUtil.toJson( quotaInfo ) );

        Response response = client.path( path ).type( MediaType.APPLICATION_FORM_URLENCODED_TYPE ).post( form );

        if ( response.getStatus() != Response.Status.OK.getStatusCode() )
        {
            throw new PeerException( "Could not set quota", response.getEntity().toString() );
        }
    }


    public PeerQuotaInfo getQuota( ContainerHost host, QuotaType quotaType ) throws PeerException
    {
        String path = "peer/container/quota";

        WebClient client = createWebClient();

        Response response =
                client.path( path ).accept( MediaType.APPLICATION_JSON ).query( "hostId", host.getId().toString() )
                      .query( "quotaType", JsonUtil.toJson( quotaType ) ).get();

        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            return JsonUtil.fromJson( response.readEntity( String.class ), new TypeToken<PeerQuotaInfo>()
            {}.getType() );
        }
        else
        {
            throw new PeerException( "Could not get quota", response.getEntity().toString() );
        }
    }


    public ProcessResourceUsage getProcessResourceUsage( ContainerHost host, int processPid ) throws PeerException
    {
        String path = "peer/container/resource/usage";

        WebClient client = createWebClient();

        Response response =
                client.path( path ).accept( MediaType.APPLICATION_JSON ).query( "hostId", host.getId().toString() )
                      .query( "processPid", processPid ).get();

        if ( response.getStatus() == Response.Status.OK.getStatusCode() )
        {
            return JsonUtil.fromJson( response.readEntity( String.class ), new TypeToken<ProcessResourceUsage>()
            {}.getType() );
        }
        else
        {
            throw new PeerException( "Could not get process resource usage", response.getEntity().toString() );
        }
    }
}
