package io.subutai.core.peer.impl;


import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.client.ClientException;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandRequest;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandResultImpl;
import io.subutai.common.command.CommandStatus;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.ContainersDestructionResultImpl;
import io.subutai.common.environment.CreateEnvironmentContainerGroupRequest;
import io.subutai.common.environment.CreateEnvironmentContainerGroupResponse;
import io.subutai.common.environment.DestroyEnvironmentContainerGroupRequest;
import io.subutai.common.environment.DestroyEnvironmentContainerGroupResponse;
import io.subutai.common.exception.HTTPException;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInfoModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerGateway;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.ContainersDestructionResult;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.MessageRequest;
import io.subutai.common.peer.MessageResponse;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.RecipientType;
import io.subutai.common.peer.RemotePeer;
import io.subutai.common.peer.Timeouts;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.protocol.Template;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.SecuritySettings;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.messenger.api.Message;
import io.subutai.core.messenger.api.MessageException;
import io.subutai.core.messenger.api.Messenger;
import io.subutai.core.peer.impl.command.BlockingCommandCallback;
import io.subutai.core.peer.impl.command.CommandResponseListener;
import io.subutai.core.peer.impl.request.MessageResponseListener;
import io.subutai.core.security.api.SecurityManager;


/**
 * Remote Peer implementation
 */
@PermitAll
public class RemotePeerImpl implements RemotePeer
{
    private static final Logger LOG = LoggerFactory.getLogger( RemotePeerImpl.class );

    private final LocalPeer localPeer;
    private SecurityManager securityManager;
    protected final PeerInfo peerInfo;
    protected final Messenger messenger;
    private final CommandResponseListener commandResponseListener;
    private final MessageResponseListener messageResponseListener;
    protected RestUtil restUtil = new RestUtil();
    protected JsonUtil jsonUtil = new JsonUtil();
    private String baseUrl;
    Object provider;


    public RemotePeerImpl( LocalPeer localPeer, SecurityManager securityManager, final PeerInfo peerInfo,
                           final Messenger messenger, CommandResponseListener commandResponseListener,
                           MessageResponseListener messageResponseListener, Object provider )
    {
        this.localPeer = localPeer;
        this.securityManager = securityManager;
        this.peerInfo = peerInfo;
        this.messenger = messenger;
        this.commandResponseListener = commandResponseListener;
        this.messageResponseListener = messageResponseListener;
        String url = "";

        String port = String.valueOf( peerInfo.getPort() );

        //switch case for formatting request url
        switch ( port )
        {
            case ChannelSettings.OPEN_PORT:
            case ChannelSettings.SPECIAL_PORT_X1:
                url = String.format( "http://%s:%s/rest/v1/peer", peerInfo.getIp(), peerInfo.getPort() );
                break;
            case ChannelSettings.SECURE_PORT_X1:
            case ChannelSettings.SECURE_PORT_X2:
            case ChannelSettings.SECURE_PORT_X3:
                url = String.format( "https://%s:%s/rest/v1/peer", peerInfo.getIp(), peerInfo.getPort() );
                break;
        }
        this.baseUrl = url;
        this.provider = provider;
    }


    protected String request( RestUtil.RequestType requestType, String path, String alias, Map<String, String> params,
                              Map<String, String> headers ) throws HTTPException
    {
        return restUtil.request( requestType,
                String.format( "%s/%s", baseUrl, path.startsWith( "/" ) ? path.substring( 1 ) : path ), alias, params,
                headers, provider );
    }


    protected String get( String path, String alias, Map<String, String> params, Map<String, String> headers )
            throws HTTPException
    {
        return request( RestUtil.RequestType.GET, path, alias, params, headers );
    }


    protected String post( String path, String alias, Map<String, String> params, Map<String, String> headers )
            throws HTTPException
    {

        return request( RestUtil.RequestType.POST, path, alias, params, headers );
    }


    protected String delete( String path, String alias, Map<String, String> params, Map<String, String> headers )
            throws HTTPException
    {

        return request( RestUtil.RequestType.DELETE, path, alias, params, headers );
    }


    @Override
    public String getId()
    {
        return peerInfo.getId();
    }


    @Override
    public PeerInfo check() throws PeerException
    {
        PeerInfo response = new PeerWebClient( peerInfo.getIp(), provider ).getInfo();
        if ( !peerInfo.getId().equals( response.getId() ) )
        {
            throw new PeerException( String.format(
                    "Remote peer check failed. Id of the remote peer %s changed. Please verify the remote peer.",
                    peerInfo.getId() ) );
        }

        return response;
    }


    @Override
    public boolean isOnline()
    {
        try
        {
            check();
            return true;
        }
        catch ( PeerException | ClientException e )
        {
            LOG.error( e.getMessage(), e );
            return false;
        }
    }


    @Override
    public boolean isLocal()
    {
        return false;
    }


    @Override
    public String getName()
    {
        return peerInfo.getName();
    }


    @Override
    public String getOwnerId()
    {
        return peerInfo.getOwnerId();
    }


    @Override
    public PeerInfo getPeerInfo()
    {
        return peerInfo;
    }


    @Override
    public Template getTemplate( final String templateName ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );

        String path = "/template/get";

        Map<String, String> params = Maps.newHashMap();

        params.put( "templateName", templateName );


        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();
        //*************************************************************


        try
        {
            String response = get( path, SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS, params, headers );

            return jsonUtil.from( response, Template.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining template", e );
        }
    }


    //********** ENVIRONMENT SPECIFIC REST *************************************


    @RolesAllowed( "Environment-Management|A|Update" )
    @Override
    public void startContainer( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Container id is null" );
        Preconditions.checkArgument( containerId.getPeerId().getId().equals( peerInfo.getId() ) );

        if ( containerId.getEnvironmentId() == null )
        {
            new PeerWebClient( peerInfo.getIp(), provider ).startContainer( containerId );
        }
        else
        {
            new EnvironmentWebClient( provider ).startContainer( peerInfo.getIp(), containerId );
        }
    }


    @RolesAllowed( "Environment-Management|A|Update" )
    @Override
    public void stopContainer( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Container id is null" );
        Preconditions.checkArgument( containerId.getPeerId().getId().equals( peerInfo.getId() ) );

        if ( containerId.getEnvironmentId() == null )
        {
            new PeerWebClient( peerInfo.getIp(), provider ).stopContainer( containerId );
        }
        else
        {
            new EnvironmentWebClient( provider ).stopContainer( peerInfo.getIp(), containerId );
        }
    }


    @RolesAllowed( "Environment-Management|A|Delete" )
    @Override
    public void destroyContainer( final ContainerId containerId ) throws PeerException
    {

        if ( containerId.getEnvironmentId() == null )
        {
            new PeerWebClient( peerInfo.getIp(), provider ).destroyContainer( containerId );
        }
        else
        {
            new EnvironmentWebClient( provider ).destroyContainer( peerInfo.getIp(), containerId );
        }
    }


    @Override
    public void setDefaultGateway( final ContainerGateway containerGateway ) throws PeerException
    {
        Preconditions.checkNotNull( containerGateway, "Container host is null" );

        String path = "peer/container/gateway";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", containerGateway.getContainerId().getId() );
        params.put( "gatewayIp", containerGateway.getGateway() );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            post( path, alias, params, headers );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error setting container gateway ip", e );
        }
    }


    @Override
    public void removePeerEnvironmentKeyPair( final EnvironmentId environmentId ) throws PeerException
    {
        new PeerWebClient( peerInfo.getIp(), provider ).removePeerEnvironmentKeyPair( environmentId );
    }


    @RolesAllowed( "Environment-Management|A|Delete" )
    @Override
    public void cleanupEnvironmentNetworkSettings( final EnvironmentId environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        new PeerWebClient( peerInfo.getIp(), provider ).cleanupEnvironmentNetworkSettings( environmentId );
    }


    @RolesAllowed( "Environment-Management|A|Delete" )
    @Override
    public boolean isConnected( final HostId hostId )
    {
        Preconditions.checkNotNull( hostId, "Host id is null" );

        if ( hostId instanceof ContainerId )
        {
            return ContainerHostState.RUNNING.equals( getContainerState( ( ContainerId ) hostId ) );
        }
        else
        {
            return false;
        }
    }


    @PermitAll
    @Override
    public ProcessResourceUsage getProcessResourceUsage( final ContainerId containerId, int pid ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Container id is null" );
        Preconditions.checkArgument( pid > 0, "Process pid must be greater than 0" );

        if ( containerId.getEnvironmentId() == null )
        {
            return new PeerWebClient( peerInfo.getIp(), provider ).getProcessResourceUsage( containerId, pid );
        }
        else
        {
            return new EnvironmentWebClient( provider ).getProcessResourceUsage( peerInfo.getIp(), containerId, pid );
        }
    }


    @Override
    public ContainerHostState getContainerState( final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId, "Container id is null" );
        Preconditions.checkArgument( containerId.getPeerId().getId().equals( peerInfo.getId() ) );

        if ( containerId.getEnvironmentId() == null )
        {
            return new PeerWebClient( peerInfo.getIp(), provider ).getState( containerId );
        }
        else
        {
            return new EnvironmentWebClient( provider ).getState( peerInfo.getIp(), containerId );
        }
    }


    @Override
    public Set<Integer> getCpuSet( final ContainerHost containerHost ) throws PeerException
    {
        Preconditions.checkNotNull( containerHost, "Container host is null" );
        Preconditions.checkArgument( containerHost instanceof EnvironmentContainerHost );

        return new EnvironmentWebClient( provider ).getCpuSet( peerInfo.getIp(), containerHost.getContainerId() );
    }


    @RolesAllowed( "Environment-Management|A|Update" )
    @Override
    public void setCpuSet( final ContainerHost containerHost, final Set<Integer> cpuSet ) throws PeerException
    {
        Preconditions.checkNotNull( containerHost, "Container host is null" );
        Preconditions.checkArgument( containerHost instanceof EnvironmentContainerHost );

        EnvironmentContainerHost host = ( EnvironmentContainerHost ) containerHost;
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( cpuSet ), "Empty cpu set" );

        new EnvironmentWebClient( provider ).setCpuSet( peerInfo.getIp(), containerHost.getContainerId(), cpuSet );
    }


    @Override
    public ResourceValue getQuota( final ContainerHost containerHost, final ResourceType resourceType )
            throws PeerException
    {
        Preconditions.checkNotNull( containerHost, "Container host is null" );
        Preconditions.checkNotNull( resourceType, "Resource type is null" );

        return new EnvironmentWebClient( provider )
                .getQuota( peerInfo.getIp(), containerHost.getContainerId(), resourceType );
    }


    @Override
    public void setQuota( final ContainerHost containerHost, final ResourceType resourceType,
                          final ResourceValue resourceValue ) throws PeerException
    {
        Preconditions.checkNotNull( containerHost, "Container host is null" );
        Preconditions.checkNotNull( resourceType, "Resource type is null" );

        new EnvironmentWebClient( provider )
                .setQuota( peerInfo.getIp(), containerHost.getContainerId(), resourceType, resourceValue );
    }


    @Override
    public ResourceValue getAvailableQuota( final ContainerHost containerHost, final ResourceType resourceType )
            throws PeerException
    {
        Preconditions.checkNotNull( containerHost, "Container host is null" );
        Preconditions.checkNotNull( resourceType, "Resource type is null" );

        return new EnvironmentWebClient( provider )
                .getAvailableQuota( peerInfo.getIp(), containerHost.getContainerId(), resourceType );
    }


    @Override
    public ResourceValue getQuota( final ContainerId containerId, final ResourceType resourceType ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Container id is null" );
        Preconditions.checkNotNull( resourceType, "Resource type is null" );

        return new EnvironmentWebClient( provider ).getQuota( peerInfo.getIp(), containerId, resourceType );
    }


    @Override
    public void setQuota( final ContainerId containerId, final ResourceType resourceType,
                          final ResourceValue resourceValue ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Container id is null" );
        Preconditions.checkNotNull( resourceType, "Resource type is null" );
        Preconditions.checkNotNull( resourceValue, "Resource value is null" );

        new EnvironmentWebClient( provider ).setQuota( peerInfo.getIp(), containerId, resourceType, resourceValue );
    }


    @Override
    public ResourceValue getAvailableQuota( final ContainerId containerId, final ResourceType resourceType )
            throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Container id is null" );
        Preconditions.checkNotNull( resourceType, "Resource type is null" );

        return new EnvironmentWebClient( provider ).getAvailableQuota( peerInfo.getIp(), containerId, resourceType );
    }


    @PermitAll
    @Override
    public HostInfo getContainerHostInfoById( final String containerHostId ) throws PeerException
    {
        String path = String.format( "/container/info" );
        try
        {
            //*********construct Secure Header ****************************
            Map<String, String> headers = Maps.newHashMap();
            //*************************************************************

            Map<String, String> params = Maps.newHashMap();
            params.put( "containerId", jsonUtil.to( containerHostId ) );
            String response = get( path, SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS, params, headers );
            return jsonUtil.from( response, HostInfoModel.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Error getting hostInfo from peer %s", getName() ), e );
        }
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host host ) throws CommandException
    {
        return execute( requestBuilder, host, null );
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback )
            throws CommandException
    {
        Preconditions.checkNotNull( requestBuilder, "Invalid request" );
        Preconditions.checkNotNull( host, "Invalid host" );

        BlockingCommandCallback blockingCommandCallback = getBlockingCommandCallback( callback );

        executeAsync( requestBuilder, host, blockingCommandCallback, blockingCommandCallback.getCompletionSemaphore() );

        CommandResult commandResult = blockingCommandCallback.getCommandResult();

        if ( commandResult == null )
        {
            commandResult = new CommandResultImpl( null, null, null, CommandStatus.TIMEOUT );
        }

        return commandResult;
    }


    protected BlockingCommandCallback getBlockingCommandCallback( CommandCallback callback )
    {
        return new BlockingCommandCallback( callback );
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback )
            throws CommandException
    {
        executeAsync( requestBuilder, host, callback, null );
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final Host host ) throws CommandException
    {
        executeAsync( requestBuilder, host, null );
    }


    protected void executeAsync( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback,
                                 Semaphore semaphore ) throws CommandException
    {
        Preconditions.checkNotNull( requestBuilder, "Invalid request" );
        Preconditions.checkNotNull( host, "Invalid host" );

        if ( !host.isConnected() )
        {
            throw new CommandException( "Host disconnected." );
        }

        if ( !( host instanceof ContainerHost ) )
        {
            throw new CommandException( "Operation not allowed" );
        }

        String environmentId = ( ( EnvironmentContainerHost ) host ).getEnvironmentId();
        CommandRequest request = new CommandRequest( requestBuilder, host.getId(), environmentId );
        //cache callback
        commandResponseListener.addCallback( request.getRequestId(), callback, requestBuilder.getTimeout(), semaphore );

        //send command request to remote peer counterpart
        try
        {
            //*********construct Secure Header ****************************
            Map<String, String> headers = Maps.newHashMap();
            //************************************************************************


            sendRequest( request, RecipientType.COMMAND_REQUEST.name(), Timeouts.COMMAND_REQUEST_MESSAGE_TIMEOUT,
                    headers );
        }
        catch ( PeerException e )
        {
            throw new CommandException( e );
        }
    }


    @Override
    public <T, V> V sendRequest( final T request, final String recipient, final int requestTimeout,
                                 Class<V> responseType, int responseTimeout, final Map<String, String> headers )
            throws PeerException
    {
        Preconditions.checkArgument( responseTimeout > 0, "Invalid response timeout" );
        Preconditions.checkNotNull( responseType, "Invalid response type" );

        //send request
        MessageRequest messageRequest = sendRequestInternal( request, recipient, requestTimeout, headers );

        //wait for response here
        MessageResponse messageResponse =
                messageResponseListener.waitResponse( messageRequest, requestTimeout, responseTimeout );

        LOG.debug( String.format( "%s", messageResponse ) );
        if ( messageResponse != null )
        {
            if ( messageResponse.getException() != null )
            {
                throw new PeerException( messageResponse.getException() );
            }
            else if ( messageResponse.getPayload() != null )
            {
                LOG.debug( String.format( "Trying get response object: %s", responseType ) );
                final V message = messageResponse.getPayload().getMessage( responseType );
                LOG.debug( String.format( "Response object: %s", message ) );
                return message;
            }
        }

        return null;
    }


    @Override
    public <T> void sendRequest( final T request, final String recipient, final int requestTimeout,
                                 final Map<String, String> headers ) throws PeerException
    {

        sendRequestInternal( request, recipient, requestTimeout, headers );
    }


    protected <T> MessageRequest sendRequestInternal( final T request, final String recipient, final int requestTimeout,
                                                      final Map<String, String> headers ) throws PeerException
    {
        Preconditions.checkNotNull( request, "Invalid request" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipient ), "Invalid recipient" );
        Preconditions.checkArgument( requestTimeout > 0, "Invalid request timeout" );

        MessageRequest messageRequest =
                new MessageRequest( new Payload( request, localPeer.getId() ), recipient, headers );
        Message message = messenger.createMessage( messageRequest );

        messageRequest.setMessageId( message.getId() );

        try
        {
            messenger.sendMessage( this, message, RecipientType.PEER_REQUEST_LISTENER.name(), requestTimeout, headers );
        }
        catch ( MessageException e )
        {
            throw new PeerException( e );
        }

        return messageRequest;
    }


    @RolesAllowed( "Environment-Management|A|Write" )
    @Override
    public Set<HostInfoModel> createEnvironmentContainerGroup( final CreateEnvironmentContainerGroupRequest request )
            throws PeerException
    {
        Preconditions.checkNotNull( request, "Invalid request" );


        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();
        //************************************************************************

        CreateEnvironmentContainerGroupResponse response =
                sendRequest( request, RecipientType.CREATE_ENVIRONMENT_CONTAINER_GROUP_REQUEST.name(),
                        Timeouts.CREATE_CONTAINER_REQUEST_TIMEOUT, CreateEnvironmentContainerGroupResponse.class,
                        Timeouts.CREATE_CONTAINER_RESPONSE_TIMEOUT, headers );

        if ( response != null )
        {
            return response.getHosts();
        }
        else
        {
            throw new PeerException( "Command timed out" );
        }
    }


    @RolesAllowed( "Environment-Management|A|Delete" )
    @Override
    public ContainersDestructionResult destroyContainersByEnvironment( final String environmentId ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );


        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();
        //**************************************************************************


        DestroyEnvironmentContainerGroupResponse response =
                sendRequest( new DestroyEnvironmentContainerGroupRequest( environmentId ),
                        RecipientType.DESTROY_ENVIRONMENT_CONTAINER_GROUP_REQUEST.name(),
                        Timeouts.DESTROY_CONTAINER_REQUEST_TIMEOUT, DestroyEnvironmentContainerGroupResponse.class,
                        Timeouts.DESTROY_CONTAINER_RESPONSE_TIMEOUT, headers );

        if ( response != null )
        {
            return new ContainersDestructionResultImpl( getId(), response.getDestroyedContainersIds(),
                    response.getException() );
        }
        else
        {
            throw new PeerException( "Command timed out" );
        }
    }


    //networking
    @RolesAllowed( "Environment-Management|A|Write" )
    @Override
    public int setupTunnels( final Map<String, String> peerIps, final String environmentId ) throws PeerException
    {

        Preconditions.checkNotNull( peerIps, "Invalid peer ips set" );
        Preconditions.checkArgument( !peerIps.isEmpty(), "Invalid peer ips set" );
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        String path = "/tunnels";

        try
        {
            //*********construct Secure Header ****************************
            Map<String, String> headers = Maps.newHashMap();
            //*************************************************************
            Map<String, String> params = Maps.newHashMap();
            params.put( "peerIps", jsonUtil.to( peerIps ) );
            params.put( "environmentId", environmentId );

            String response = post( path, SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS, params, headers );

            return Integer.parseInt( response );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Error setting up tunnels on peer %s", getName() ), e );
        }
    }


    @RolesAllowed( "Environment-Management|A|Write" )
    @Override
    public Vni reserveVni( final Vni vni ) throws PeerException
    {
        Preconditions.checkNotNull( vni, "Invalid vni" );

        return new PeerWebClient( peerInfo.getIp(), provider ).reserveVni( vni );
    }

    //************ END ENVIRONMENT SPECIFIC REST


    @RolesAllowed( "Environment-Management|A|Read" )
    @Override
    public Set<Gateway> getGateways() throws PeerException
    {
        try
        {
            return new PeerWebClient( peerInfo.getIp(), provider ).getGateways();
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Error obtaining gateways from peer %s", getName() ), e );
        }
    }


    @Override
    public Set<Vni> getReservedVnis() throws PeerException
    {
        return new PeerWebClient( peerInfo.getIp(), provider ).getReservedVnis();
    }


    @Override
    public PublicKeyContainer createPeerEnvironmentKeyPair( EnvironmentId environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environmentId" );

        return new PeerWebClient( peerInfo.getIp(), provider ).createEnvironmentKeyPair( environmentId );
    }


    @Override
    public void updatePeerEnvironmentPubKey( final EnvironmentId environmentId, final PGPPublicKeyRing publicKeyRing )
            throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environmentId" );
        Preconditions.checkNotNull( publicKeyRing, "Public key ring is null" );


        try
        {
            String exportedPubKeyRing =
                    securityManager.getEncryptionTool().armorByteArrayToString( publicKeyRing.getEncoded() );
            final PublicKeyContainer publicKeyContainer =
                    new PublicKeyContainer( environmentId.getId(), publicKeyRing.getPublicKey().getFingerprint(),
                            exportedPubKeyRing );
            new PeerWebClient( peerInfo.getIp(), provider ).updateEnvironmentPubKey( publicKeyContainer );
        }
        catch ( IOException | PGPException e )
        {


        }
    }


    @Override
    public HostInterfaces getInterfaces()
    {
        return new PeerWebClient( peerInfo.getIp(), provider ).getInterfaces();
    }


    @Override
    public void setupN2NConnection( final N2NConfig config )
    {
        Preconditions.checkNotNull( config, "Invalid n2n config" );

        new PeerWebClient( peerInfo.getIp(), provider ).setupN2NConnection( config );
    }


    @Override
    public void removeN2NConnection( final EnvironmentId environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment ID" );
        new PeerWebClient( peerInfo.getIp(), provider ).removeN2NConnection( environmentId );
    }


    @Override
    public void createGateway( final Gateway gateway ) throws PeerException
    {
        Preconditions.checkNotNull( gateway );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( gateway.getIp() ) );
        Preconditions.checkArgument( gateway.getVlan() > 0 );


        new PeerWebClient( peerInfo.getIp(), provider ).createGateway( gateway );
    }


    @Override
    public ResourceHostMetrics getResourceHostMetrics()
    {
        return new PeerWebClient( peerInfo.getIp(), provider ).getResourceHostMetrics();
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof RemotePeerImpl ) )
        {
            return false;
        }

        final RemotePeerImpl that = ( RemotePeerImpl ) o;

        return getId().equals( that.getId() );
    }


    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }
}
