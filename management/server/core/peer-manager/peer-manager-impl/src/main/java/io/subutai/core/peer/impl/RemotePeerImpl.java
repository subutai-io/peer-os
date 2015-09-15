package io.subutai.core.peer.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandStatus;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.CreateContainerGroupRequest;
import io.subutai.common.environment.CreateEnvironmentContainersRequest;
import io.subutai.common.exception.HTTPException;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.Interface;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainersDestructionResult;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostInfoModel;
import io.subutai.common.peer.InterfacePattern;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.protocol.Template;
import io.subutai.common.quota.CpuQuotaInfo;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.QuotaInfo;
import io.subutai.common.quota.QuotaType;
import io.subutai.common.quota.RamQuota;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SecuritySettings;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.messenger.api.Message;
import io.subutai.core.messenger.api.MessageException;
import io.subutai.core.messenger.api.Messenger;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.Payload;
import io.subutai.core.peer.api.RemotePeer;
import io.subutai.core.peer.impl.command.BlockingCommandCallback;
import io.subutai.core.peer.impl.command.CommandRequest;
import io.subutai.core.peer.impl.command.CommandResponseListener;
import io.subutai.core.peer.impl.command.CommandResultImpl;
import io.subutai.core.peer.impl.container.ContainersDestructionResultImpl;
import io.subutai.core.peer.impl.container.CreateContainerGroupResponse;
import io.subutai.core.peer.impl.container.DestroyEnvironmentContainersRequest;
import io.subutai.core.peer.impl.container.DestroyEnvironmentContainersResponse;
import io.subutai.core.peer.impl.entity.HostInterface;
import io.subutai.core.peer.impl.request.MessageRequest;
import io.subutai.core.peer.impl.request.MessageResponse;
import io.subutai.core.peer.impl.request.MessageResponseListener;


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
    protected RestUtil restUtil = new RestUtil();
    protected JsonUtil jsonUtil = new JsonUtil();
    private String baseUrl;
    private SecurityManager securityManager;


    public RemotePeerImpl( LocalPeer localPeer, final PeerInfo peerInfo, final Messenger messenger,
                           CommandResponseListener commandResponseListener,
                           MessageResponseListener messageResponseListener )
    {
        this.localPeer = localPeer;
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
                url = String.format( "http://%s:%s/cxf", peerInfo.getIp(), peerInfo.getPort() );
                break;
            case ChannelSettings.SECURE_PORT_X1:
            case ChannelSettings.SECURE_PORT_X2:
            case ChannelSettings.SECURE_PORT_X3:
                url = String.format( "https://%s:%s/cxf", peerInfo.getIp(), peerInfo.getPort() );
                break;
        }
        this.baseUrl = url;
    }


    protected String request( RestUtil.RequestType requestType, String path, String alias, Map<String, String> params,
                              Map<String, String> headers ) throws HTTPException
    {
        return restUtil.request( requestType,
                String.format( "%s/%s", baseUrl, path.startsWith( "/" ) ? path.substring( 1 ) : path ), alias, params,
                headers );
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
    public String getRemoteId() throws PeerException
    {
        String path = "peer/id";

        try
        {
            return get( path, SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS, null, null );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining peer id", e );
        }
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

        String path = "peer/template/get";

        Map<String, String> params = Maps.newHashMap();

        params.put( "templateName", templateName );


        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_PEER_ID_SOURCE, localPeer.getId().toString() );
        headers.put( Common.HEADER_PEER_ID_TARGET, peerInfo.getId().toString() );
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


    @Override
    public void startContainer( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Container host is null" );

        String path = "peer/container/start";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            post( path, alias, params, headers );
        }
        catch ( Exception e )
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

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            post( path, alias, params, headers );
        }
        catch ( Exception e )
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

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            post( path, alias, params, headers );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error destroying container", e );
        }
    }


    @Override
    public void setDefaultGateway( final ContainerHost host, final String gatewayIp ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( gatewayIp ) && gatewayIp.matches( Common.IP_REGEX ),
                "Invalid gateway IP" );

        String path = "peer/container/gateway";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );
        params.put( "gatewayIp", gatewayIp );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
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
    public boolean isConnected( final Host host )
    {
        Preconditions.checkNotNull( host, "Container host is null" );
        Preconditions.checkArgument( host instanceof ContainerHost );

        String path = "peer/container/isconnected";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );


        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + ( ( ContainerHost ) host ).getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + ( ( ContainerHost ) host ).getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************


        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;

            return jsonUtil.from( post( path, alias, params, headers ), Boolean.class );
        }
        catch ( Exception e )
        {
            LOG.error( "Error checking container connection", e );
        }
        return false;
    }


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final ContainerHost host, final int processPid )
            throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkArgument( processPid > 0, "Process pid must be greater than 0" );

        String path = "peer/container/resource/usage";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );
        params.put( "processPid", String.valueOf( processPid ) );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();
        String envheader = localPeer.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envheader );
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            String response = get( path, alias, params, headers );

            return jsonUtil.from( response, new TypeToken<ProcessResourceUsage>()
            {}.getType() );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining resource usage", e );
        }
    }


    @Override
    public ContainerHostState getContainerHostState( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        String path = "peer/container/state";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            String response = get( path, alias, params, headers );

            return jsonUtil.from( response, ContainerHostState.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container state", e );
        }
    }


    @Override
    public int getRamQuota( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        String path = "peer/container/quota/ram";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************
        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            String response = get( path, alias, params, headers );

            return jsonUtil.from( response, Integer.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container ram quota", e );
        }
    }


    @Override
    public RamQuota getRamQuotaInfo( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        String path = "peer/container/quota/ram/info";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            String response = get( path, alias, params, headers );

            return jsonUtil.from( response, RamQuota.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container ram quota", e );
        }
    }


    @Override
    public void setRamQuota( final ContainerHost host, final int ramInMb ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkArgument( ramInMb > 0, "Ram quota value must be greater than 0" );

        String path = "peer/container/quota/ram";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );
        params.put( "ram", String.valueOf( ramInMb ) );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************
        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            post( path, alias, params, headers );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error setting container ram quota", e );
        }
    }


    @Override
    public int getCpuQuota( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        String path = "peer/container/quota/cpu";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************
        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            String response = get( path, alias, params, headers );

            return jsonUtil.from( response, Integer.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container cpu quota", e );
        }
    }


    @Override
    public CpuQuotaInfo getCpuQuotaInfo( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        String path = "peer/container/quota/cpu/info";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************
        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            String response = get( path, alias, params, headers );

            return jsonUtil.from( response, CpuQuotaInfo.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container cpu quota", e );
        }
    }


    @Override
    public void setCpuQuota( final ContainerHost host, final int cpuPercent ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkArgument( cpuPercent > 0, "Cpu quota value must be greater than 0" );

        String path = "peer/container/quota/cpu";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );
        params.put( "cpu", String.valueOf( cpuPercent ) );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //**************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            post( path, alias, params, headers );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error setting container cpu quota", e );
        }
    }


    @Override
    public Set<Integer> getCpuSet( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        String path = "peer/container/quota/cpuset";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            String response = get( path, alias, params, headers );

            return jsonUtil.from( response, new TypeToken<Set<Integer>>()
            {}.getType() );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container cpu set", e );
        }
    }


    @Override
    public void setCpuSet( final ContainerHost host, final Set<Integer> cpuSet ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( cpuSet ), "Empty cpu set" );

        String path = "peer/container/quota/cpuset";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );
        params.put( "cpuset", jsonUtil.to( cpuSet ) );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            post( path, alias, params, headers );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error setting container cpu set", e );
        }
    }


    @Override
    public DiskQuota getDiskQuota( final ContainerHost host, final DiskPartition diskPartition ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkNotNull( diskPartition, "Invalid disk partition" );

        String path = "peer/container/quota/disk";

        Map<String, String> params = Maps.newHashMap();

        params.put( "containerId", host.getId().toString() );
        params.put( "diskPartition", jsonUtil.to( diskPartition ) );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            String response = get( path, alias, params, headers );

            return jsonUtil.from( response, new TypeToken<DiskQuota>()
            {}.getType() );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container disk quota", e );
        }
    }


    @Override
    public void setDiskQuota( final ContainerHost host, final DiskQuota diskQuota ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkNotNull( diskQuota, "Invalid disk quota" );

        String path = "peer/container/quota/disk";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );
        params.put( "diskQuota", jsonUtil.to( diskQuota ) );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            post( path, alias, params, headers );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error setting container disk quota", e );
        }
    }


    @Override
    public void setRamQuota( final ContainerHost host, final RamQuota ramQuota ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkNotNull( ramQuota, "Invalid ram quota" );

        String path = "peer/container/quota/ram2";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );
        params.put( "ramQuota", jsonUtil.to( ramQuota ) );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            post( path, alias, params, headers );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error setting ram quota", e );
        }
    }


    @Override
    public int getAvailableRamQuota( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        String path = "peer/container/quota/ram/available";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************
        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            String response = get( path, alias, params, headers );

            return jsonUtil.from( response, Integer.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container available ram quota", e );
        }
    }


    @Override
    public int getAvailableCpuQuota( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        String path = "peer/container/quota/cpu/available";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            String response = get( path, alias, params, headers );

            return jsonUtil.from( response, Integer.class );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container available cpu quota", e );
        }
    }


    @Override
    public DiskQuota getAvailableDiskQuota( final ContainerHost host, final DiskPartition diskPartition )
            throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkNotNull( diskPartition, "Invalid disk partition" );

        String path = "peer/container/quota/disk/available";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );
        params.put( "diskPartition", jsonUtil.to( diskPartition ) );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            String response = get( path, alias, params, headers );

            return jsonUtil.from( response, new TypeToken<DiskQuota>()
            {}.getType() );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error obtaining container available disk quota", e );
        }
    }


    @Override
    public QuotaInfo getQuotaInfo( final ContainerHost host, final QuotaType quotaType ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkNotNull( quotaType, "Invalid quota type" );

        String path = "peer/container/quota/info";

        Map<String, String> params = Maps.newHashMap();
        params.put( "containerId", host.getId().toString() );
        params.put( "quotaType", jsonUtil.to( quotaType ) );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            String response = get( path, alias, params, headers );

            return jsonUtil.from( response, new TypeToken<QuotaInfo>()
            {}.getType() );
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
        params.put( "quotaInfo", jsonUtil.to( quotaInfo ) );

        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();

        String envHeaderSource = localPeer.getId() + "-" + host.getEnvironmentId();
        String envHeaderTarget = peerInfo.getId() + "-" + host.getEnvironmentId();

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //*************************************************************

        try
        {
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;
            post( path, alias, params, headers );
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error setting container quota", e );
        }
    }


    @Override
    public HostInfo getContainerHostInfoById( final String containerHostId ) throws PeerException
    {
        String path = String.format( "peer/container/info" );
        try
        {
            //*********construct Secure Header ****************************
            Map<String, String> headers = Maps.newHashMap();
            String envId = localPeer.getContainerHostById( containerHostId ).getEnvironmentId();
            String envHeaderSource = localPeer.getId() + "-" + envId;
            String envHeaderTarget = peerInfo.getId() + "-" + envId;

            headers.put( Common.HEADER_SPECIAL, "ENC" );
            headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
            headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
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


    //DONE


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

        String environmentId = ( ( ContainerHost ) host ).getEnvironmentId();
        CommandRequest request = new CommandRequest( requestBuilder, host.getId(), environmentId );
        //cache callback
        commandResponseListener.addCallback( request.getRequestId(), callback, requestBuilder.getTimeout(), semaphore );

        //send command request to remote peer counterpart
        try
        {
            //*********construct Secure Header ****************************
            Map<String, String> headers = Maps.newHashMap();
            String envHeaderSource = localPeer.getId() + "-" + environmentId;
            String envHeaderTarget = peerInfo.getId() + "-" + environmentId;

            headers.put( Common.HEADER_SPECIAL, "ENC" );
            headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
            headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
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


    @Override
    public Set<HostInfoModel> createEnvironmentContainers( final CreateEnvironmentContainersRequest request )
            throws PeerException
    {
        Preconditions.checkNotNull( request, "Invalid request" );


        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();
        String envId = request.getEnvironmentId();
        String envHeaderSource = localPeer.getId() + "-" + envId;
        String envHeaderTarget = peerInfo.getId() + "-" + envId;

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //************************************************************************

        CreateContainerGroupResponse response =
                sendRequest( request, RecipientType.CREATE_ENVIRONMENT_CONTAINERS_REQUEST.name(),
                        Timeouts.CREATE_CONTAINER_REQUEST_TIMEOUT, CreateContainerGroupResponse.class,
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


    @Override
    public Set<HostInfoModel> createContainerGroup( final CreateContainerGroupRequest request ) throws PeerException
    {

        Preconditions.checkNotNull( request, "Invalid request" );


        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();
        String envId = request.getEnvironmentId().toString();
        String envHeaderSource = localPeer.getId() + "-" + envId;
        String envHeaderTarget = peerInfo.getId() + "-" + envId;

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //************************************************************************

        CreateContainerGroupResponse response =
                sendRequest( request, RecipientType.CREATE_CONTAINER_GROUP_REQUEST.name(),
                        Timeouts.CREATE_CONTAINER_REQUEST_TIMEOUT, CreateContainerGroupResponse.class,
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


    @Override
    public ContainersDestructionResult destroyEnvironmentContainers( final String environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );


        //*********construct Secure Header ****************************
        Map<String, String> headers = Maps.newHashMap();
        String envHeaderSource = localPeer.getId() + "-" + environmentId;
        String envHeaderTarget = peerInfo.getId() + "-" + environmentId;

        headers.put( Common.HEADER_SPECIAL, "ENC" );
        headers.put( Common.HEADER_ENV_ID_SOURCE, envHeaderSource );
        headers.put( Common.HEADER_ENV_ID_TARGET, envHeaderTarget );
        //**************************************************************************


        DestroyEnvironmentContainersResponse response =
                sendRequest( new DestroyEnvironmentContainersRequest( environmentId ),
                        RecipientType.CONTAINER_DESTROY_REQUEST.name(), Timeouts.DESTROY_CONTAINER_REQUEST_TIMEOUT,
                        DestroyEnvironmentContainersResponse.class, Timeouts.DESTROY_CONTAINER_RESPONSE_TIMEOUT,
                        headers );

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


    @Override
    public int reserveVni( final Vni vni ) throws PeerException
    {
        Preconditions.checkNotNull( vni, "Invalid vni" );

        String path = "peer/vni";

        try
        {
            //*********construct Secure Header ****************************
            Map<String, String> headers = Maps.newHashMap();

            headers.put( Common.HEADER_SPECIAL, "ENC" );
            headers.put( Common.HEADER_PEER_ID_SOURCE, localPeer.getId().toString() );
            headers.put( Common.HEADER_PEER_ID_TARGET, peerInfo.getId().toString() );

            //*************************************************************

            Map<String, String> params = Maps.newHashMap();

            params.put( "vni", jsonUtil.to( vni ) );

            String response = post( path, SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS, params, headers );

            return Integer.parseInt( response );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Error reserving vni %s on peer %s", vni, getName() ), e );
        }
    }
    //************ END ENVIRONMENT SPECIFIC REST


    @Override
    public Set<Gateway> getGateways() throws PeerException
    {
        String path = "peer/gateways";
        try
        {
            //*********construct Secure Header ****************************
            Map<String, String> headers = Maps.newHashMap();

            headers.put( Common.HEADER_SPECIAL, "ENC" );
            headers.put( Common.HEADER_PEER_ID_SOURCE, localPeer.getId().toString() );
            headers.put( Common.HEADER_PEER_ID_TARGET, peerInfo.getId().toString() );
            //*************************************************************

            String response = get( path, SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS, null, headers );

            return jsonUtil.from( response, new TypeToken<Set<Gateway>>()
            {}.getType() );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Error obtaining gateways from peer %s", getName() ), e );
        }
    }


    @Override
    public Set<Vni> getReservedVnis() throws PeerException
    {
        String path = "peer/vni";

        try
        {
            //*********construct Secure Header ****************************
            Map<String, String> headers = Maps.newHashMap();

            headers.put( Common.HEADER_SPECIAL, "ENC" );
            headers.put( Common.HEADER_PEER_ID_SOURCE, localPeer.getId().toString() );
            headers.put( Common.HEADER_PEER_ID_TARGET, peerInfo.getId().toString() );
            //*************************************************************

            String response = get( path, SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS, null, headers );

            return jsonUtil.from( response, new TypeToken<Set<Vni>>()
            {}.getType() );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Error obtaining reserved VNIs from peer %s", getName() ), e );
        }
    }


    /* ***************************************************
     *
     */
    @Override
    public String createEnvironmentKeyPair( String environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environmentId" );

        String path = "peer/createpek";

        try
        {
            //*********construct Secure Header ****************************
            Map<String, String> headers = Maps.newHashMap();

            headers.put( Common.HEADER_SPECIAL, "ENC" );
            headers.put( Common.HEADER_PEER_ID_SOURCE, localPeer.getId().toString() );
            headers.put( Common.HEADER_PEER_ID_TARGET, peerInfo.getId().toString() );
            //*************************************************************

            Map<String, String> params = Maps.newHashMap();
            params.put( "environmentId", environmentId );

            String response = post( path, SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS, params, headers );

            return response;
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Error creating PEK on peer %s", getName() ), e );
        }
    }


    /* ***************************************************
     *
     */
    private String buildPath( String path )
    {
        return String.format( "%s/%s", baseUrl, path.startsWith( "/" ) ? path.substring( 1 ) : path );
    }


    @Override
    public Set<Interface> getNetworkInterfaces( final InterfacePattern pattern )
    {
        Preconditions.checkNotNull( pattern, "Pattern could not be null" );


        String path = buildPath( "peer/interfaces" );

        //TODO: implement as singleton
        List<Object> providers = new ArrayList<Object>();
        providers.add( new JacksonJaxbJsonProvider() );

        WebClient client =
                restUtil.createTrustedWebClientWithAuthAndProviders( path, SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS,
                        providers );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        Collection interfaces = client.postAndGetCollection( pattern, HostInterface.class );
        LOG.debug( String.format( "%d", interfaces.size() ) );
        return new HashSet<Interface>( interfaces );
    }


    @Override
    public void setupN2NConnection( final N2NConfig config )
    {
        LOG.debug( String.format( "Adding remote peer to n2n community: %s:%d %s %s %s", config.getSuperNodeIp(),
                config.getN2NPort(), config.getInterfaceName(), config.getCommunityName(), config.getAddress() ) );

        String path = "peer/n2ntunnel";
        LOG.debug( String.format( "%s %s %s", peerInfo.getIp(), peerInfo.getPort(), baseUrl ) );


        //TODO: implement as singleton
        List<Object> providers = new ArrayList<Object>();
        providers.add( new JacksonJaxbJsonProvider() );

        WebClient client = restUtil.createTrustedWebClientWithAuthAndProviders( buildPath( path ),
                SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS, providers );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        Response response = client.post( config );
    }


    @Override
    public void removeN2NConnection( final N2NConfig config ) throws PeerException
    {
        LOG.debug( String.format( "Removing remote peer from n2n community: %s:%d %s %s %s", config.getSuperNodeIp(),
                config.getN2NPort(), config.getInterfaceName(), config.getCommunityName(), config.getAddress() ) );

        String path = String.format( "peer/n2ntunnel/%s/%s", config.getInterfaceName(), config.getCommunityName() );
        LOG.debug( String.format( "%s %s %s", peerInfo.getIp(), peerInfo.getPort(), baseUrl ) );


        //TODO: implement as singleton
        List<Object> providers = new ArrayList<Object>();
        providers.add( new JacksonJaxbJsonProvider() );

        WebClient client = restUtil.createTrustedWebClientWithAuthAndProviders( buildPath( path ),
                SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS, providers );

        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        Response response = client.delete();
        LOG.debug( String.format( "%s", response ) );
    }


    @Override
    public void createGateway( final String environmentGatewayIp, final int vlan ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentGatewayIp ) );
        Preconditions.checkArgument( vlan > 0 );

        String path = "peer/creategateway";

        try
        {
            //*********construct Secure Header ****************************
            Map<String, String> headers = Maps.newHashMap();

            headers.put( Common.HEADER_SPECIAL, "ENC" );
            headers.put( Common.HEADER_PEER_ID_SOURCE, localPeer.getId() );
            headers.put( Common.HEADER_PEER_ID_TARGET, peerInfo.getId() );
            //*************************************************************
            Map<String, String> params = Maps.newHashMap();
            params.put( "gatewayIp", environmentGatewayIp );
            params.put( "vlan", String.valueOf( vlan ) );

            String response = post( path, SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS, params, headers );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Error creating gateway on peer %s", getName() ), e );
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
