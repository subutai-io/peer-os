package org.safehaus.subutai.core.peer.impl;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandStatus;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.HTTPException;
import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.ContainersDestructionResult;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.RestUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageException;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.RemotePeer;
import org.safehaus.subutai.core.peer.impl.command.BlockingCommandCallback;
import org.safehaus.subutai.core.peer.impl.command.CommandRequest;
import org.safehaus.subutai.core.peer.impl.command.CommandResponseListener;
import org.safehaus.subutai.core.peer.impl.command.CommandResultImpl;
import org.safehaus.subutai.core.peer.impl.container.ContainersDestructionResultImpl;
import org.safehaus.subutai.core.peer.impl.container.CreateContainersRequest;
import org.safehaus.subutai.core.peer.impl.container.CreateContainersResponse;
import org.safehaus.subutai.core.peer.impl.container.DestroyEnvironmentContainersRequest;
import org.safehaus.subutai.core.peer.impl.container.DestroyEnvironmentContainersResponse;
import org.safehaus.subutai.core.peer.impl.request.MessageRequest;
import org.safehaus.subutai.core.peer.impl.request.MessageResponse;
import org.safehaus.subutai.core.peer.impl.request.MessageResponseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;


/**
 * Remote Peer implementation
 */
public class RemotePeerImpl implements RemotePeer
{
    private static final Logger LOG = LoggerFactory.getLogger( RemotePeerImpl.class.getName() );

    private final LocalPeer localPeer;
    protected final PeerInfo peerInfo;
    protected final Messenger messenger;
    private final CommandResponseListener commandResponseListener;
    private final MessageResponseListener messageResponseListener;
    private final RestUtil restUtil = new RestUtil();
    private String baseUrl;


    public RemotePeerImpl( LocalPeer localPeer, final PeerInfo peerInfo, final Messenger messenger,
                           CommandResponseListener commandResponseListener,
                           MessageResponseListener messageResponseListener )
    {
        this.localPeer = localPeer;
        this.peerInfo = peerInfo;
        this.messenger = messenger;
        this.commandResponseListener = commandResponseListener;
        this.messageResponseListener = messageResponseListener;
        this.baseUrl = String.format( "http://%s:%s/cxf", peerInfo.getIp(), peerInfo.getPort() );
    }


    protected String request( RestUtil.RequestType requestType, String path, Map<String, String> params,
                              Map<String, String> headers ) throws HTTPException
    {
        return restUtil.request( requestType,
                String.format( "%s/%s", baseUrl, path.startsWith( "/" ) ? path.substring( 1 ) : path ), params,
                headers );
    }


    protected String get( String path, Map<String, String> params, Map<String, String> headers ) throws HTTPException
    {
        return request( RestUtil.RequestType.GET, path, params, headers );
    }


    protected String post( String path, Map<String, String> params, Map<String, String> headers ) throws HTTPException
    {

        return request( RestUtil.RequestType.POST, path, params, headers );
    }


    @Override
    public UUID getId()
    {
        return peerInfo.getId();
    }


    @Override
    public boolean isOnline() throws PeerException
    {
        if ( peerInfo.getId().equals( getRemoteId() ) )
        {
            return true;
        }
        else
        {
            throw new PeerException( "Invalid peer ID." );
        }
    }


    @Override
    public String getName()
    {
        return peerInfo.getName();
    }


    @Override
    public UUID getOwnerId()
    {
        return peerInfo.getOwnerId();
    }


    @Override
    public PeerInfo getPeerInfo()
    {
        return peerInfo;
    }


    @Override
    public void startContainer( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Container host is null" );

        String path = "peer/container/start";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", host.getId().toString() );

        try
        {
            post( path, params, null );
        }
        catch ( HTTPException e )
        {
            throw new PeerException( "Error starting container", e );
        }
    }


    @Override
    public void stopContainer( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Container host is null" );

        String path = "peer/container/stop";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", host.getId().toString() );

        try
        {
            post( path, params, null );
        }
        catch ( HTTPException e )
        {
            throw new PeerException( "Error stopping container", e );
        }
    }


    @Override
    public void destroyContainer( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Container host is null" );

        String path = "peer/container/destroy";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", host.getId().toString() );

        try
        {
            post( path, params, null );
        }
        catch ( HTTPException e )
        {
            throw new PeerException( "Error destroying container", e );
        }
    }


    @Override
    public boolean isConnected( final Host host )
    {
        Preconditions.checkNotNull( host, "Container host is null" );

        String path = "peer/container/isconnected";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", host.getId().toString() );

        try
        {
            return JsonUtil.fromJson( post( path, params, null ), Boolean.class );
        }
        catch ( Exception e )
        {
            LOG.error( "Error checking container connection", e );
        }
        return false;
    }


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final UUID containerId, final int processPid )
            throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );
        Preconditions.checkArgument( processPid > 0, "Process pid must be greater than 0" );

        String path = "peer/container/resource/usage";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", containerId.toString() );
        params.put( "processPid", String.valueOf( processPid ) );

        try
        {
            String response = get( path, params, null );

            return JsonUtil.fromJson( response, new TypeToken<ProcessResourceUsage>() {}.getType() );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining resource usage", e );
        }
    }


    @Override
    public boolean isLocal()
    {
        return false;
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

        BlockingCommandCallback blockingCommandCallback = new BlockingCommandCallback( callback );

        executeAsync( requestBuilder, host, blockingCommandCallback, blockingCommandCallback.getCompletionSemaphore() );

        CommandResult commandResult = blockingCommandCallback.getCommandResult();

        if ( commandResult == null )
        {
            commandResult = new CommandResultImpl( null, null, null, CommandStatus.TIMEOUT );
        }

        return commandResult;
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


    private void executeAsync( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback,
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

        if ( !UUIDUtil.isStringAUuid( ( ( ContainerHost ) host ).getEnvironmentId() ) )
        {
            throw new CommandException( "Invalid container environment id" );
        }

        UUID environmentId = UUID.fromString( ( ( ContainerHost ) host ).getEnvironmentId() );

        CommandRequest request = new CommandRequest( requestBuilder, host.getId(), environmentId );
        //cache callback
        commandResponseListener.addCallback( request.getRequestId(), callback, requestBuilder.getTimeout(), semaphore );

        //send command request to remote peer counterpart
        try
        {
            sendRequest( request, RecipientType.COMMAND_REQUEST.name(), Timeouts.COMMAND_REQUEST_MESSAGE_TIMEOUT,
                    environmentId );
        }
        catch ( PeerException e )
        {
            throw new CommandException( e );
        }
    }


    @Override
    public <T, V> V sendRequest( final T request, final String recipient, final int requestTimeout,
                                 Class<V> responseType, int responseTimeout, final UUID environmentId )
            throws PeerException
    {
        Preconditions.checkArgument( responseTimeout > 0, "Invalid response timeout" );
        Preconditions.checkNotNull( responseType, "Invalid response type" );

        //send request
        MessageRequest messageRequest = sendRequestInternal( request, recipient, requestTimeout, environmentId );

        //wait for response here
        MessageResponse messageResponse =
                messageResponseListener.waitResponse( messageRequest.getId(), requestTimeout, responseTimeout );

        if ( messageResponse != null )
        {
            if ( messageResponse.getException() != null )
            {
                throw new PeerException( messageResponse.getException() );
            }
            else if ( messageResponse.getPayload() != null )
            {
                return messageResponse.getPayload().getMessage( responseType );
            }
        }

        return null;
    }


    @Override
    public <T> void sendRequest( final T request, final String recipient, final int requestTimeout, UUID environmentId )
            throws PeerException
    {

        sendRequestInternal( request, recipient, requestTimeout, environmentId );
    }


    private <T> MessageRequest sendRequestInternal( final T request, final String recipient, final int requestTimeout,
                                                    final UUID environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( request, "Invalid request" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipient ), "Invalid recipient" );
        Preconditions.checkArgument( requestTimeout > 0, "Invalid request timeout" );
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        MessageRequest messageRequest = new MessageRequest( new Payload( request, localPeer.getId() ), recipient );
        Message message = messenger.createMessage( messageRequest );
        message.setEnvironmentId( environmentId );

        try
        {
            messenger.sendMessage( this, message, RecipientType.PEER_REQUEST_LISTENER.name(), requestTimeout );
        }
        catch ( MessageException e )
        {
            throw new PeerException( e );
        }

        return messageRequest;
    }


    @Override
    public Template getTemplate( final String templateName ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );

        String path = "peer/template/get";

        Map<String, String> params = Maps.newHashMap();

        params.put( "templateName", templateName );

        try
        {
            String response = get( path, params, null );

            return JsonUtil.fromJson( response, Template.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining template", e );
        }
    }


    @Override
    public ContainerHostState getContainerHostState( final String containerId ) throws PeerException
    {
        Preconditions.checkArgument( UUIDUtil.isStringAUuid( containerId ), "Invalid container id" );

        String path = "peer/container/state";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", containerId );

        try
        {
            String response = get( path, params, null );

            return JsonUtil.fromJson( response, ContainerHostState.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container state", e );
        }
    }


    // ********** Quota functions *****************


    @Override
    public int getRamQuota( final UUID containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );

        String path = "peer/container/quota/ram";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", containerId.toString() );

        try
        {
            String response = get( path, params, null );

            return JsonUtil.fromJson( response, Integer.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container ram quota", e );
        }
    }


    @Override
    public void setRamQuota( final UUID containerId, final int ramInMb ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );
        Preconditions.checkArgument( ramInMb > 0, "Ram quota value must be greater than 0" );

        String path = "peer/container/quota/ram";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", containerId.toString() );
        params.put( "ram", String.valueOf( ramInMb ) );

        try
        {
            post( path, params, null );
        }
        catch ( HTTPException e )
        {
            throw new PeerException( "Error setting container ram quota", e );
        }
    }


    @Override
    public int getCpuQuota( final UUID containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );

        String path = "peer/container/quota/cpu";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", containerId.toString() );

        try
        {
            String response = get( path, params, null );

            return JsonUtil.fromJson( response, Integer.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container cpu quota", e );
        }
    }


    @Override
    public void setCpuQuota( final UUID containerId, final int cpuPercent ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );
        Preconditions.checkArgument( cpuPercent > 0, "Cpu quota value must be greater than 0" );

        String path = "peer/container/quota/cpu";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", containerId.toString() );
        params.put( "cpu", String.valueOf( cpuPercent ) );

        try
        {
            post( path, params, null );
        }
        catch ( HTTPException e )
        {
            throw new PeerException( "Error setting container cpu quota", e );
        }
    }


    @Override
    public Set<Integer> getCpuSet( final UUID containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );

        String path = "peer/container/quota/cpuset";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", containerId.toString() );

        try
        {
            String response = get( path, params, null );

            return JsonUtil.fromJson( response, new TypeToken<Set<Integer>>() {}.getType() );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container cpu set", e );
        }
    }


    @Override
    public void setCpuSet( final UUID containerId, final Set<Integer> cpuSet ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( cpuSet ), "Empty cpu set" );

        String path = "peer/container/quota/cpuset";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", containerId.toString() );
        params.put( "cpuset", JsonUtil.toJson( cpuSet ) );

        try
        {
            post( path, params, null );
        }
        catch ( HTTPException e )
        {
            throw new PeerException( "Error setting container cpu set", e );
        }
    }


    @Override
    public DiskQuota getDiskQuota( final UUID containerId, final DiskPartition diskPartition ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );
        Preconditions.checkNotNull( diskPartition, "Invalid disk partition" );

        String path = "peer/container/quota/disk";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", containerId.toString() );
        params.put( "diskPartition", JsonUtil.toJson( diskPartition ) );

        try
        {
            String response = get( path, params, null );

            return JsonUtil.fromJson( response, new TypeToken<DiskQuota>() {}.getType() );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container disk quota", e );
        }
    }


    @Override
    public void setDiskQuota( final UUID containerId, final DiskQuota diskQuota ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );
        Preconditions.checkNotNull( diskQuota, "Invalid disk quota" );

        String path = "peer/container/quota/disk";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", containerId.toString() );
        params.put( "diskQuota", JsonUtil.toJson( diskQuota ) );

        try
        {
            post( path, params, null );
        }
        catch ( HTTPException e )
        {
            throw new PeerException( "Error setting container disk quota", e );
        }
    }


    @Override
    public int getAvailableRamQuota( final UUID containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );

        String path = "peer/container/quota/ram/available";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", containerId.toString() );

        try
        {
            String response = get( path, params, null );

            return JsonUtil.fromJson( response, Integer.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container available ram quota", e );
        }
    }


    @Override
    public int getAvailableCpuQuota( final UUID containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );

        String path = "peer/container/quota/cpu/available";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", containerId.toString() );

        try
        {
            String response = get( path, params, null );

            return JsonUtil.fromJson( response, Integer.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container available cpu quota", e );
        }
    }


    @Override
    public DiskQuota getAvailableDiskQuota( final UUID containerId, final DiskPartition diskPartition )
            throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );
        Preconditions.checkNotNull( diskPartition, "Invalid disk partition" );

        String path = "peer/container/quota/disk/available";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", containerId.toString() );
        params.put( "diskPartition", JsonUtil.toJson( diskPartition ) );

        try
        {
            String response = get( path, params, null );

            return JsonUtil.fromJson( response, new TypeToken<DiskQuota>() {}.getType() );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container available disk quota", e );
        }
    }


    @Override
    public Set<HostInfoModel> createContainers( final UUID environmentId, final UUID initiatorPeerId,
                                                final UUID ownerId, final List<Template> templates,
                                                final int numberOfContainers, final String strategyId,
                                                final List<Criteria> criteria ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );
        Preconditions.checkNotNull( initiatorPeerId, "Invalid initiator peer id" );
        Preconditions.checkNotNull( ownerId, "Invalid owner id" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( templates ), "Invalid template set" );
        Preconditions.checkArgument( numberOfContainers > 0, "Invalid number of containers" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( strategyId ), "Invalid strategy id" );

        CreateContainersResponse response = sendRequest(
                new CreateContainersRequest( environmentId, initiatorPeerId, ownerId, templates, numberOfContainers,
                        strategyId, criteria ), RecipientType.CONTAINER_CREATE_REQUEST.name(),
                Timeouts.CREATE_CONTAINER_REQUEST_TIMEOUT, CreateContainersResponse.class,
                Timeouts.CREATE_CONTAINER_RESPONSE_TIMEOUT, environmentId );

        if ( response != null )
        {
            return response.getHosts();
        }
        else
        {
            throw new PeerException( "Command timed out" );
        }
    }


    @Override
    public ContainersDestructionResult destroyEnvironmentContainers( final UUID environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        DestroyEnvironmentContainersResponse response =
                sendRequest( new DestroyEnvironmentContainersRequest( environmentId ),
                        RecipientType.CONTAINER_DESTROY_REQUEST.name(), Timeouts.DESTROY_CONTAINER_REQUEST_TIMEOUT,
                        DestroyEnvironmentContainersResponse.class, Timeouts.DESTROY_CONTAINER_RESPONSE_TIMEOUT,
                        environmentId );

        if ( response != null )
        {
            return new ContainersDestructionResultImpl( response.getDestroyedContainersIds(), response.getException() );
        }
        else
        {
            throw new PeerException( "Command timed out" );
        }
    }


    @Override
    public PeerQuotaInfo getQuota( final ContainerHost host, final QuotaType quotaType ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkNotNull( quotaType, "Invalid quota type" );

        String path = "peer/container/quota";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", host.getId().toString() );
        params.put( "quotaType", JsonUtil.toJson( quotaType ) );

        try
        {
            String response = get( path, params, null );

            return JsonUtil.fromJson( response, new TypeToken<PeerQuotaInfo>() {}.getType() );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container quota", e );
        }
    }


    @Override
    public void setQuota( final ContainerHost host, final QuotaInfo quotaInfo ) throws PeerException
    {

        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkNotNull( quotaInfo, "Invalid quota info" );

        String path = "peer/container/quota";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", host.getId().toString() );
        params.put( "quotaInfo", JsonUtil.toJson( quotaInfo ) );

        try
        {
            post( path, params, null );
        }
        catch ( HTTPException e )
        {
            throw new PeerException( "Error setting container quota", e );
        }
    }


    @Override
    public UUID getRemoteId() throws PeerException
    {
        String path = "peer/id";

        try
        {
            return UUID.fromString( get( path, null, null ) );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining peer id", e );
        }
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
