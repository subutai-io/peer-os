package io.subutai.core.localpeer.impl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.naming.NamingException;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.ContainerDistributionType;
import io.subutai.common.environment.ContainersDestructionResultImpl;
import io.subutai.common.environment.CreateEnvironmentContainerGroupRequest;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.NullHostInterface;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.mdc.SubutaiExecutors;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.ContainerGateway;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.ContainerType;
import io.subutai.common.peer.ContainersDestructionResult;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.RequestListener;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.Disposable;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.quota.ContainerQuotaHolder;
import io.subutai.common.quota.QuotaException;
import io.subutai.common.resource.HistoricalMetrics;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.common.settings.Common;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.common.util.N2NUtil;
import io.subutai.common.util.NumUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.common.util.StringUtil;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.localpeer.impl.command.CommandRequestListener;
import io.subutai.core.localpeer.impl.container.CreateContainerWrapperTask;
import io.subutai.core.localpeer.impl.container.CreateEnvironmentContainerGroupRequestListener;
import io.subutai.core.localpeer.impl.container.DestroyContainerWrapperTask;
import io.subutai.core.localpeer.impl.container.DestroyEnvironmentContainerGroupRequestListener;
import io.subutai.core.localpeer.impl.dao.ResourceHostDataService;
import io.subutai.core.localpeer.impl.dao.TunnelDataService;
import io.subutai.core.localpeer.impl.entity.AbstractSubutaiHost;
import io.subutai.core.localpeer.impl.entity.ContainerHostEntity;
import io.subutai.core.localpeer.impl.entity.ResourceHostEntity;
import io.subutai.core.localpeer.impl.entity.TunnelEntity;
import io.subutai.core.localpeer.impl.tasks.CreateGatewayTask;
import io.subutai.core.localpeer.impl.tasks.ReserveVniTask;
import io.subutai.core.localpeer.impl.tasks.SetupTunnelsTask;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.repository.api.RepositoryException;
import io.subutai.core.repository.api.RepositoryManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.strategy.api.StrategyManager;
import io.subutai.core.strategy.api.StrategyNotFoundException;

//import io.subutai.core.localpeer.impl.dao.ManagementHostDataService;


/**
 * Local peer implementation
 */
@PermitAll
public class LocalPeerImpl implements LocalPeer, HostListener, Disposable
{
    private static final Logger LOG = LoggerFactory.getLogger( LocalPeerImpl.class );

    public static final String PEER_SUBNET_MASK = "255.255.255.0";
    private static final int N2N_PORT = 5000;
    private static final String GATEWAY_INTERFACE_NAME_REGEX = "^br-(\\d+)$";
    private static final Pattern GATEWAY_INTERFACE_NAME_PATTERN = Pattern.compile( GATEWAY_INTERFACE_NAME_REGEX );

    private DaoManager daoManager;
    private TemplateManager templateRegistry;
    protected Host managementHost;
    protected Set<ResourceHost> resourceHosts = Sets.newHashSet();
    private CommandExecutor commandExecutor;
    private StrategyManager strategyManager;
    private QuotaManager quotaManager;
    private Monitor monitor;
    protected ResourceHostDataService resourceHostDataService;
    protected TunnelDataService tunnelDataService;
    private HostRegistry hostRegistry;
    protected CommandUtil commandUtil = new CommandUtil();
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();
    protected Set<RequestListener> requestListeners = Sets.newHashSet();
    protected PeerInfo peerInfo;
    private SecurityManager securityManager;
    protected ServiceLocator serviceLocator = new ServiceLocator();


    protected boolean initialized = false;
    protected ExecutorService singleThreadExecutorService = SubutaiExecutors.newSingleThreadExecutor();


    public LocalPeerImpl( DaoManager daoManager, TemplateManager templateRegistry, QuotaManager quotaManager,
                          StrategyManager strategyManager, CommandExecutor commandExecutor, HostRegistry hostRegistry,
                          Monitor monitor, SecurityManager securityManager )
    {
        this.strategyManager = strategyManager;
        this.daoManager = daoManager;
        this.templateRegistry = templateRegistry;
        this.quotaManager = quotaManager;
        this.monitor = monitor;
        this.commandExecutor = commandExecutor;
        this.hostRegistry = hostRegistry;
        this.securityManager = securityManager;
    }


    public void init() throws PeerException
    {
        LOG.debug( "********************************************** Initializing peer "
                + "******************************************" );

        //add command request listener
        addRequestListener( new CommandRequestListener() );
        //add command response listener

        //add create container requests listener
        addRequestListener( new CreateEnvironmentContainerGroupRequestListener( this ) );
        //add destroy environment containers requests listener
        addRequestListener( new DestroyEnvironmentContainerGroupRequestListener( this ) );


        try
        {

            tunnelDataService = createTunnelDataService();


            resourceHostDataService = createResourceHostDataService();
            resourceHosts.clear();
            synchronized ( resourceHosts )
            {
                for ( ResourceHost resourceHost : resourceHostDataService.getAll() )
                {
                    if ( "management".equals( resourceHost.getHostname() ) )
                    {
                        managementHost = resourceHost;
                    }
                    resourceHosts.add( resourceHost );
                }
            }

            setResourceHostTransientFields( resourceHosts );
        }
        catch ( Exception e )
        {
            throw new LocalPeerInitializationError( "Failed to init Local Peer", e );
        }

        addRequestListener( new CreateEnvironmentContainerGroupRequestListener( this ) );
        //add destroy environment containers requests listener
        addRequestListener( new DestroyEnvironmentContainerGroupRequestListener( this ) );

        initialized = true;
    }


    @Override
    public void setPeerInfo( final PeerInfo peerInfo )
    {
        this.peerInfo = peerInfo;
    }


    protected ResourceHostDataService createResourceHostDataService()
    {
        return new ResourceHostDataService( daoManager.getEntityManagerFactory() );
    }


    protected TunnelDataService createTunnelDataService()
    {
        return new TunnelDataService( daoManager.getEntityManagerFactory() );
    }


    @Override
    public void dispose()
    {
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            ( ( Disposable ) resourceHost ).dispose();
        }
    }


    private void setResourceHostTransientFields( Set<ResourceHost> resourceHosts )
    {
        for ( ResourceHost resourceHost : resourceHosts )
        {
            ( ( AbstractSubutaiHost ) resourceHost ).setPeer( this );
            final ResourceHostEntity resourceHostEntity = ( ResourceHostEntity ) resourceHost;
            resourceHostEntity.setRegistry( templateRegistry );
            resourceHostEntity.setHostRegistry( hostRegistry );
        }
    }


    @Override
    public String getId()
    {
        return peerInfo.getId();
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
    public ContainerHostState getContainerState( final ContainerId containerId )
    {
        try
        {
            ContainerHostInfo containerHostInfo =
                    ( ContainerHostInfo ) hostRegistry.getHostInfoById( containerId.getId() );
            return containerHostInfo.getState();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            return ContainerHostState.UNKNOWN;
        }
    }


    protected ExecutorService getFixedPoolExecutor( int numOfThreads )
    {
        return Executors.newFixedThreadPool( numOfThreads );
    }


    @RolesAllowed( "Environment-Management|A|Write" )
    @Override
    public Set<ContainerHostInfoModel> createEnvironmentContainerGroup(
            final CreateEnvironmentContainerGroupRequest request ) throws PeerException
    {
        Preconditions.checkNotNull( request );

        Set<ContainerHostInfoModel> result;
        if ( request.getContainerDistributionType() == ContainerDistributionType.AUTO )
        {
            result = createByStrategy( request );
        }
        else
        {
            result = createByHost( request );
        }

        return result;
    }


    private Set<ContainerHostInfoModel> createByHost( final CreateEnvironmentContainerGroupRequest request )
            throws PeerException
    {
        SubnetUtils cidr;
        try
        {
            cidr = new SubnetUtils( request.getSubnetCidr() );
        }
        catch ( IllegalArgumentException e )
        {
            throw new PeerException( "Failed to parse subnet CIDR", e );
        }

        ResourceHost resourceHost = getResourceHostById( request.getHost() );
        Set<String> containerDistribution =
                generateCloneNames( request.getTemplateName(), request.getNumberOfContainers() );
        String networkPrefix = cidr.getInfo().getCidrSignature().split( "/" )[1];
        String[] allAddresses = cidr.getInfo().getAllAddresses();
        String gateway = cidr.getInfo().getLowAddress();
        int currentIpAddressOffset = 0;
        Vni environmentVni = findVniByEnvironmentId( request.getEnvironmentId() );

        if ( environmentVni == null )
        {
            throw new PeerException(
                    String.format( "No reserved vni found for environment %s", request.getEnvironmentId() ) );
        }

        Set<ContainerHostInfoModel> result = Sets.newHashSet();

        ContainerQuotaHolder containerQuota = quotaManager.getDefaultContainerQuota( request.getContainerType() );
        if ( containerQuota == null )
        {
            LOG.warn( "Quota not found for container type: " + request.getContainerType() );
            containerQuota = quotaManager.getDefaultContainerQuota( ContainerType.SMALL );
        }


        for ( String cloneName : containerDistribution )
        {
            String ipAddress = allAddresses[request.getIpAddressOffset() + currentIpAddressOffset];

            try
            {
                ContainerHostInfo hostInfo = resourceHost.createContainer( request.getTemplateName(), cloneName,
                        String.format( "%s/%s", ipAddress, networkPrefix ), environmentVni.getVlan(),
                        Common.WAIT_CONTAINER_CONNECTION_SEC, request.getEnvironmentId() );

                TemplateKurjun template = getTemplateByName( request.getTemplateName() );

                ContainerHostEntity containerHostEntity =
                        new ContainerHostEntity( getId(), hostInfo, template.getName(), template.getArchitecture() );
                containerHostEntity.setEnvironmentId( request.getEnvironmentId() );
                containerHostEntity.setOwnerId( request.getOwnerId() );
                containerHostEntity.setInitiatorPeerId( request.getInitiatorPeerId() );
                containerHostEntity.setContainerType( request.getContainerType() );

                //TODO: sign container host key with PEK
                resourceHost.addContainerHost( containerHostEntity );

                resourceHostDataService.saveOrUpdate( resourceHost );

                quotaManager.setQuota( containerHostEntity.getContainerId(), containerQuota );
                result.add( new ContainerHostInfoModel( hostInfo ) );
            }
            catch ( ResourceHostException | QuotaException e )
            {
                LOG.error( "Error creating container", e );
            }

            currentIpAddressOffset++;
        }

        return result;
    }


    private Set<ContainerHostInfoModel> createByStrategy( final CreateEnvironmentContainerGroupRequest request )
            throws PeerException
    {
        //check if strategy exists
        try
        {
            strategyManager.findStrategyById( request.getStrategyId() );
        }
        catch ( StrategyNotFoundException e )
        {
            throw new PeerException( e );
        }

        SubnetUtils cidr;
        try
        {
            cidr = new SubnetUtils( request.getSubnetCidr() );
        }
        catch ( IllegalArgumentException e )
        {
            throw new PeerException( "Failed to parse subnet CIDR", e );
        }

        Map<ResourceHost, Set<String>> containerDistribution = distributeContainersToResourceHosts( request );


        String networkPrefix = cidr.getInfo().getCidrSignature().split( "/" )[1];
        String[] allAddresses = cidr.getInfo().getAllAddresses();
        String gateway = cidr.getInfo().getLowAddress();
        int currentIpAddressOffset = 0;

        List<Future<CreateContainerWrapperTask>> taskFutures = Lists.newArrayList();
        ExecutorService executorService = getFixedPoolExecutor( request.getNumberOfContainers() );

        Vni environmentVni = findVniByEnvironmentId( request.getEnvironmentId() );

        if ( environmentVni == null )
        {
            throw new PeerException(
                    String.format( "No reserved vni found for environment %s", request.getEnvironmentId() ) );
        }

        //create containers in parallel on each resource host
        for ( Map.Entry<ResourceHost, Set<String>> resourceHostDistribution : containerDistribution.entrySet() )
        {
            ResourceHostEntity resourceHostEntity = ( ResourceHostEntity ) resourceHostDistribution.getKey();

            for ( String hostname : resourceHostDistribution.getValue() )
            {

                String ipAddress = allAddresses[request.getIpAddressOffset() + currentIpAddressOffset];
                taskFutures.add( executorService.submit(
                        new CreateContainerWrapperTask( resourceHostEntity, request.getTemplateName(), hostname,
                                String.format( "%s/%s", ipAddress, networkPrefix ), environmentVni.getVlan(),
                                Common.WAIT_CONTAINER_CONNECTION_SEC, request.getEnvironmentId() ) ) );

                currentIpAddressOffset++;
            }
        }


        //wait for succeeded containers
        Set<ContainerHostInfoModel> result = Sets.newHashSet();

        ContainerQuotaHolder containerQuota = quotaManager.getDefaultContainerQuota( request.getContainerType() );
        if ( containerQuota == null )
        {
            LOG.warn( "Quota not found for container type: " + request.getContainerType() );
            containerQuota = quotaManager.getDefaultContainerQuota( ContainerType.SMALL );
        }

        for ( Future<CreateContainerWrapperTask> future : taskFutures )
        {
            try
            {
                CreateContainerWrapperTask task = future.get();
                ContainerHostInfo hostInfo = task.getHostInfo();
                ResourceHost resourceHost = task.getResourceHost();

                TemplateKurjun template = getTemplateByName( request.getTemplateName() );

                ContainerHostEntity containerHostEntity =
                        new ContainerHostEntity( getId(), hostInfo, template.getName(), template.getArchitecture() );
                containerHostEntity.setEnvironmentId( request.getEnvironmentId() );
                containerHostEntity.setOwnerId( request.getOwnerId() );
                containerHostEntity.setInitiatorPeerId( request.getInitiatorPeerId() );
                containerHostEntity.setContainerType( request.getContainerType() );

                //TODO: sign container host key with PEK
                resourceHost.addContainerHost( containerHostEntity );

                resourceHostDataService.saveOrUpdate( resourceHost );

                quotaManager.setQuota( containerHostEntity.getContainerId(), containerQuota );
                result.add( new ContainerHostInfoModel( hostInfo ) );
            }
            catch ( ExecutionException | InterruptedException | QuotaException e )
            {
                LOG.error( "Error creating container", e );
            }
        }

        executorService.shutdown();

        return result;
    }


    protected Map<ResourceHost, Set<String>> distributeContainersToResourceHosts(
            final CreateEnvironmentContainerGroupRequest request ) throws PeerException
    {
        //temporarily disabled metric calculation
        //todo use new monitor binding and new approach to calculate container placement
        //todo approach should consider instance types requested in blueprint @TimurB see this
        Map<ResourceHost, Integer> slots = Maps.newHashMap();
        Set<ResourceHost> resourceHosts = getResourceHosts();
        Iterator<ResourceHost> rhIt = resourceHosts.iterator();
        while ( rhIt.hasNext() )
        {
            ResourceHost rh = rhIt.next();
            if ( !rh.isConnected() )
            {
                rhIt.remove();
            }
        }
        if ( resourceHosts.isEmpty() )
        {
            throw new PeerException( "There are no connected resource hosts" );
        }
        int numOfRequestedContainers = request.getNumberOfContainers();
        int j = 0;
        int leftOver = numOfRequestedContainers;
        int avgNumOfContainersPerRh = numOfRequestedContainers / resourceHosts.size();
        for ( final ResourceHost resourceHost : resourceHosts )
        {
            j++;
            if ( j < resourceHosts.size() )
            {
                slots.put( resourceHost, avgNumOfContainersPerRh );
                leftOver -= avgNumOfContainersPerRh;
            }
            else
            {
                slots.put( resourceHost, leftOver );
            }
        }

        //distribute new containers' names across selected resource hosts
        Map<ResourceHost, Set<String>> containerDistribution = Maps.newHashMap();

        for ( Map.Entry<ResourceHost, Integer> e : slots.entrySet() )
        {
            Set<String> hostCloneNames = new HashSet<>();
            for ( int i = 0; i < e.getValue(); i++ )
            {
                String newContainerName = StringUtil.trimToSize(
                        String.format( "%s%s", request.getTemplateName(), UUID.randomUUID() ).replace( "-", "" ),
                        Common.MAX_CONTAINER_NAME_LEN );
                hostCloneNames.add( newContainerName );
            }
            ResourceHost resourceHost = getResourceHostByName( e.getKey().getHostname() );
            containerDistribution.put( resourceHost, hostCloneNames );
        }
        return containerDistribution;
    }


    protected Set<String> generateCloneNames( String templateName, int count ) throws PeerException
    {
        Set<String> result = new HashSet<>();

        for ( int i = 0; i < count; i++ )
        {
            String newContainerName = StringUtil
                    .trimToSize( String.format( "%s%s", templateName, UUID.randomUUID() ).replace( "-", "" ),
                            Common.MAX_CONTAINER_NAME_LEN );
            result.add( newContainerName );
        }

        return result;
    }


    @PermitAll
    @Override
    public Set<ContainerHost> findContainersByEnvironmentId( final String environmentId )
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        Set<ContainerHost> result = new HashSet<>();

        for ( ResourceHost resourceHost : resourceHosts )
        {
            result.addAll( resourceHost.getContainerHostsByEnvironmentId( environmentId ) );
        }
        return result;
    }


    @Override
    public ContainerHost findContainerById( final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );
        Preconditions.checkNotNull( containerId.getId(), "Invalid container id" );

        ContainerHost result = null;
        for ( ResourceHost resourceHost : resourceHosts )
        {
            try
            {
                result = resourceHost.getContainerHostById( containerId.getId() );
                break;
            }
            catch ( HostNotFoundException ignore )
            {
                // ignore
            }
        }
        return result;
    }


    @Override
    public Set<ContainerHost> findContainersByOwnerId( final String ownerId )
    {
        Preconditions.checkNotNull( ownerId, "Specify valid owner" );


        Set<ContainerHost> result = new HashSet<>();

        for ( ResourceHost resourceHost : resourceHosts )
        {
            result.addAll( resourceHost.getContainerHostsByOwnerId( ownerId ) );
        }
        return result;
    }


    @PermitAll
    @Override
    public ContainerHost getContainerHostByName( String hostname ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Container hostname shouldn't be null" );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            try
            {
                return resourceHost.getContainerHostByName( hostname );
            }
            catch ( HostNotFoundException ignore )
            {
                //ignore
            }
        }

        throw new HostNotFoundException( String.format( "No container host found for name %s", hostname ) );
    }


    @PermitAll
    @Override
    public ContainerHost getContainerHostById( final String hostId ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( hostId, "Invalid container host id" );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            try
            {
                return resourceHost.getContainerHostById( hostId );
            }
            catch ( HostNotFoundException e )
            {
                //ignore
            }
        }

        throw new HostNotFoundException( String.format( "Container host not found by id %s", hostId ) );
    }


    @PermitAll
    @Override
    public ContainerHostInfo getContainerHostInfoById( final String containerHostId ) throws PeerException
    {
        ContainerHost containerHost = getContainerHostById( containerHostId );

        return new ContainerHostInfoModel( containerHost );
    }


    @PermitAll
    @Override
    public ResourceHost getResourceHostByName( String hostname ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid resource host hostname" );


        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            if ( resourceHost.getHostname().equalsIgnoreCase( hostname ) )
            {
                return resourceHost;
            }
        }
        throw new HostNotFoundException( String.format( "Resource host not found by hostname %s", hostname ) );
    }


    @PermitAll
    @Override
    public ResourceHost getResourceHostById( final String hostId ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( hostId, "Resource host id is null" );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            if ( resourceHost.getId().equals( hostId ) )
            {
                return resourceHost;
            }
        }
        throw new HostNotFoundException( String.format( "Resource host not found by id %s", hostId ) );
    }


    @PermitAll
    @Override
    public ResourceHost getResourceHostByContainerName( final String containerName ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerName ), "Invalid container name" );

        ContainerHost c = getContainerHostByName( containerName );
        ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) c;
        return containerHostEntity.getParent();
    }


    @PermitAll
    @Override
    public ResourceHost getResourceHostByContainerId( final String hostId ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( hostId, "Container host id is invalid" );

        ContainerHost c = getContainerHostById( hostId );
        ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) c;
        return containerHostEntity.getParent();
    }


    @Override
    public Host bindHost( String id ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( id );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            if ( resourceHost.getId().equals( id ) )
            {
                return resourceHost;
            }
            else
            {
                try
                {
                    return resourceHost.getContainerHostById( id );
                }
                catch ( HostNotFoundException ignore )
                {
                    //ignore
                }
            }
        }

        throw new HostNotFoundException( String.format( "Host by id %s is not registered", id ) );
    }


    @Override
    public ContainerHostEntity bindHost( final ContainerId containerId ) throws HostNotFoundException
    {
        return ( ContainerHostEntity ) bindHost( containerId.getId() );
    }


    @RolesAllowed( "Environment-Management|A|Update" )
    @Override
    public void startContainer( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Cannot operate on null container id" );

        ContainerHostEntity containerHost = bindHost( containerId );
        ResourceHost resourceHost = containerHost.getParent();
        try
        {
            resourceHost.startContainerHost( containerHost );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Could not start LXC container [%s]", e.toString() ) );
        }
    }


    @RolesAllowed( "Environment-Management|A|Update" )
    @Override
    public void stopContainer( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Cannot operate on null container id" );

        ContainerHostEntity containerHost = bindHost( containerId );
        ResourceHost resourceHost = containerHost.getParent();
        try
        {
            resourceHost.stopContainerHost( containerHost );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
    }


    @RolesAllowed( "Environment-Management|A|Delete" )
    @Override
    public void destroyContainer( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Cannot operate on null container id" );

        ContainerHostEntity host = bindHost( containerId );
        ResourceHost resourceHost = host.getParent();

        try
        {
            resourceHost.destroyContainerHost( host );
        }
        catch ( ResourceHostException e )
        {
            String errMsg = String.format( "Could not destroy container [%s]", host.getHostname() );
            LOG.error( errMsg, e );
            throw new PeerException( errMsg, e.toString() );
        }

        resourceHostDataService.update( ( ResourceHostEntity ) resourceHost );
    }


    @RolesAllowed( "Environment-Management|A|Delete" )
    @Override
    public void cleanupEnvironmentNetworkSettings( final EnvironmentId environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        try
        {
            getNetworkManager().cleanupEnvironmentNetworkSettings( environmentId );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException(
                    String.format( "Error cleaning up environment %s network settings", environmentId ), e );
        }
    }


    @RolesAllowed( "Environment-Management|A|Delete" )
    @Override
    public void removePeerEnvironmentKeyPair( final EnvironmentId environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId );

        KeyManager keyManager = securityManager.getKeyManager();

        keyManager.removeKeyData( environmentId.getId() );
        keyManager.removeKeyData( getId() + "-" + environmentId.getId() );
    }


    @RolesAllowed( "Environment-Management|A|Write" )
    @Override
    public void setDefaultGateway( final ContainerGateway gateway ) throws PeerException
    {
        Preconditions.checkNotNull( gateway, "Invalid gateway" );

        try
        {
            commandUtil.execute( new RequestBuilder( String.format( "route add default gw %s %s", gateway.getGateway(),
                    Common.DEFAULT_CONTAINER_INTERFACE ) ), bindHost( gateway.getContainerId() ) );
        }
        catch ( CommandException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public boolean isConnected( final HostId hostId )
    {
        Preconditions.checkNotNull( hostId, "Host id null" );

        try
        {
            HostInfo hostInfo = hostRegistry.getHostInfoById( hostId.getId() );
            return hostInfo.getId().equals( hostId.getId() );
        }
        catch ( HostDisconnectedException e )
        {
            return false;
        }
    }


    @Override
    public Host getManagementHost() throws HostNotFoundException
    {
        if ( managementHost == null )
        {
            throw new HostNotFoundException( "Management host not found." );
        }
        return managementHost;
    }


    @Override
    public Set<ResourceHost> getResourceHosts()
    {
        synchronized ( resourceHosts )
        {
            return Sets.newConcurrentHashSet( this.resourceHosts );
        }
    }


    public void addResourceHost( final ResourceHost host )
    {
        Preconditions.checkNotNull( host, "Resource host could not be null." );

        synchronized ( resourceHosts )
        {
            resourceHosts.add( host );
        }
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host host ) throws CommandException
    {
        return execute( requestBuilder, host, null );
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host aHost,
                                  final CommandCallback callback ) throws CommandException
    {
        Preconditions.checkNotNull( requestBuilder, "Invalid request" );
        Preconditions.checkNotNull( aHost, "Invalid host" );

        Host host;
        try
        {
            host = bindHost( aHost.getId() );
        }
        catch ( PeerException e )
        {
            throw new CommandException( "Host is not registered" );
        }
        if ( !host.isConnected() )
        {
            throw new CommandException( "Host is not connected" );
        }


        CommandResult result;

        if ( callback == null )
        {
            result = commandExecutor.execute( host.getId(), requestBuilder );
        }
        else
        {
            result = commandExecutor.execute( host.getId(), requestBuilder, callback );
        }

        return result;
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final Host aHost, final CommandCallback callback )
            throws CommandException
    {
        Preconditions.checkNotNull( requestBuilder, "Invalid request" );
        Preconditions.checkNotNull( aHost, "Invalid host" );

        Host host;
        try
        {
            host = bindHost( aHost.getId() );
        }
        catch ( PeerException e )
        {
            throw new CommandException( "Host not register." );
        }
        if ( !host.isConnected() )
        {
            throw new CommandException( "Host disconnected." );
        }


        if ( callback == null )
        {
            commandExecutor.executeAsync( host.getId(), requestBuilder );
        }
        else
        {
            commandExecutor.executeAsync( host.getId(), requestBuilder, callback );
        }
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final Host host ) throws CommandException
    {
        executeAsync( requestBuilder, host, null );
    }


    @Override
    public boolean isLocal()
    {
        return true;
    }


    @Override
    public TemplateKurjun getTemplate( final String templateName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );

        return templateRegistry.getTemplate( templateName );
    }


    @PermitAll
    @Override
    public boolean isOnline()
    {
        return true;
    }


    @Override
    public <T, V> V sendRequest( final T request, final String recipient, final int requestTimeout,
                                 final Class<V> responseType, final int responseTimeout, Map<String, String> headers )
            throws PeerException
    {
        Preconditions.checkNotNull( responseType, "Invalid response type" );

        return sendRequestInternal( request, recipient, responseType );
    }


    @Override
    public <T> void sendRequest( final T request, final String recipient, final int requestTimeout,
                                 Map<String, String> headers ) throws PeerException
    {
        sendRequestInternal( request, recipient, null );
    }


    protected <T, V> V sendRequestInternal( final T request, final String recipient, final Class<V> responseType )
            throws PeerException
    {
        Preconditions.checkNotNull( request, "Invalid request" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipient ), "Invalid recipient" );

        for ( RequestListener requestListener : requestListeners )
        {
            if ( recipient.equalsIgnoreCase( requestListener.getRecipient() ) )
            {
                try
                {
                    Object response = requestListener.onRequest( new Payload( request, getId() ) );

                    if ( response != null && responseType != null )
                    {
                        return responseType.cast( response );
                    }
                }
                catch ( Exception e )
                {
                    throw new PeerException( e );
                }
            }
        }

        return null;
    }

    @Override
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo, Set<QuotaAlertValue> alerts )
    {
        LOG.debug( "On heartbeat: " + resourceHostInfo.getHostname() );
        if ( initialized )
        {
            ResourceHostEntity host;
            try
            {
                host = ( ResourceHostEntity ) getResourceHostByName( resourceHostInfo.getHostname() );
            }
            catch ( HostNotFoundException e )
            {
                LOG.debug( "Host not found in #onHeartbeat", e );
                host = new ResourceHostEntity( getId(), resourceHostInfo );
                resourceHostDataService.persist( host );
                addResourceHost( host );
                Set<ResourceHost> a = Sets.newHashSet();
                a.add( host );
                setResourceHostTransientFields( a );
            }
            if ( host.updateHostInfo( resourceHostInfo ) )
            {
                resourceHostDataService.update( host );
                LOG.debug( String.format( "Resource host %s updated.", host.getId() ) );
            }
            if ( resourceHostInfo.getHostname().equals( "management" ) )
            {
                managementHost = host;
            }
        }
    }


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final ContainerId containerId, int pid ) throws PeerException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkArgument( pid > 0, "Process pid must be greater than 0" );

        try
        {
            return monitor.getProcessResourceUsage( containerId, pid );
        }
        catch ( MonitorException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public Set<Integer> getCpuSet( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        try
        {
            return quotaManager.getCpuSet( host.getContainerId() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @RolesAllowed( "Environment-Management|A|Update" )
    @Override
    public void setCpuSet( final ContainerHost host, final Set<Integer> cpuSet ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( cpuSet ), "Empty cpu set" );

        try
        {
            quotaManager.setCpuSet( host.getContainerId(), cpuSet );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @RolesAllowed( "Environment-Management|A|Delete" )
    @Override
    public ContainersDestructionResult destroyContainersByEnvironment( final String environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        Set<Throwable> errors = Sets.newHashSet();
        Set<ContainerHost> destroyedContainers;

        Set<ContainerHost> containerHosts = Sets.newHashSet();


        for ( ResourceHost resourceHost : resourceHosts )
        {
            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                if ( environmentId.equals( containerHost.getEnvironmentId().getId() ) )
                {
                    containerHosts.add( containerHost );
                }
            }
        }


        destroyedContainers = destroyContainerGroup( containerHosts, errors );

        String exception =
                errors.isEmpty() ? null : String.format( "There were errors while destroying containers: %s", errors );
        return new ContainersDestructionResultImpl( getId(), destroyedContainers, exception );
    }


    @RolesAllowed( "Environment-Management|A|Delete" )
    private Set<ContainerHost> destroyContainerGroup( final Set<ContainerHost> containerHosts,
                                                      final Set<Throwable> errors )
    {
        Set<ContainerHost> destroyedContainers = new HashSet<>();
        if ( !containerHosts.isEmpty() )
        {
            List<Future<ContainerHost>> taskFutures = Lists.newArrayList();
            ExecutorService executorService = getFixedPoolExecutor( containerHosts.size() );

            for ( ContainerHost containerHost : containerHosts )
            {

                taskFutures.add( executorService.submit( new DestroyContainerWrapperTask( this, containerHost ) ) );
            }

            for ( Future<ContainerHost> taskFuture : taskFutures )
            {
                try
                {
                    destroyedContainers.add( taskFuture.get() );
                }
                catch ( ExecutionException | InterruptedException e )
                {
                    errors.add( exceptionUtil.getRootCause( e ) );
                }
            }

            executorService.shutdown();
        }

        return destroyedContainers;
    }


    //networking


    @Override
    public Set<Gateway> getGateways() throws PeerException
    {
        Set<Gateway> gateways = Sets.newHashSet();

        //TODO: use findByName method
        for ( HostInterface iface : getManagementHost().getHostInterfaces().getAll() )
        {
            Matcher matcher = GATEWAY_INTERFACE_NAME_PATTERN.matcher( iface.getName().trim() );
            if ( matcher.find() )
            {
                int vlan = Integer.parseInt( matcher.group( 1 ) );
                String ip = iface.getIp();

                gateways.add( new Gateway( vlan, ip ) );
            }
        }

        return gateways;
    }


    @RolesAllowed( "Environment-Management|A|Write" )
    @Override
    public Vni reserveVni( final Vni vni ) throws PeerException
    {
        Preconditions.checkNotNull( vni, "Invalid vni" );

        //need to execute sequentially since other parallel executions can take the same VNI
        Future<Vni> future = queueSequentialTask( new ReserveVniTask( getNetworkManager(), vni, this ) );

        try
        {
            return future.get();
        }
        catch ( InterruptedException e )
        {
            throw new PeerException( e );
        }
        catch ( ExecutionException e )
        {
            if ( e.getCause() instanceof PeerException )
            {
                throw ( PeerException ) e.getCause();
            }
            throw new PeerException( "Error reserving VNI", e.getCause() );
        }
    }


    @Override
    public Set<Vni> getReservedVnis() throws PeerException
    {
        try
        {
            return getNetworkManager().getReservedVnis();
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public String getVniDomain( final Long vni ) throws PeerException
    {
        Integer vlan = getVlanByVni( vni );

        if ( vlan != null )
        {
            try
            {
                return getNetworkManager().getVlanDomain( vlan );
            }
            catch ( NetworkManagerException e )
            {
                throw new PeerException( String.format( "Error obtaining domain by vlan %d", vlan ), e );
            }
        }
        else
        {

            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    @RolesAllowed( "Environment-Management|A|Delete" )
    @Override
    public void removeVniDomain( final Long vni ) throws PeerException
    {
        Integer vlan = getVlanByVni( vni );

        if ( vlan != null )
        {
            try
            {
                getNetworkManager().removeVlanDomain( vlan );
            }
            catch ( NetworkManagerException e )
            {
                throw new PeerException( String.format( "Error removing domain by vlan %d", vlan ), e );
            }
        }
        else
        {

            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    @RolesAllowed( "Environment-Management|A|Update" )
    @Override
    public void setVniDomain( final Long vni, final String domain,
                              final DomainLoadBalanceStrategy domainLoadBalanceStrategy, final String sslCertPath )
            throws PeerException
    {
        Integer vlan = getVlanByVni( vni );

        if ( vlan != null )
        {
            try
            {
                getNetworkManager().setVlanDomain( vlan, domain, domainLoadBalanceStrategy, sslCertPath );
            }
            catch ( NetworkManagerException e )
            {
                throw new PeerException( String.format( "Error setting domain by vlan %d", vlan ), e );
            }
        }
        else
        {

            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    @Override
    public boolean isIpInVniDomain( final String hostIp, final Long vni ) throws PeerException
    {
        Integer vlan = getVlanByVni( vni );

        if ( vlan != null )
        {
            try
            {
                return getNetworkManager().isIpInVlanDomain( hostIp, vlan );
            }
            catch ( NetworkManagerException e )
            {
                throw new PeerException( String.format( "Error checking domain by ip %s and vlan %d", hostIp, vlan ),
                        e );
            }
        }
        else
        {

            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    @RolesAllowed( "Environment-Management|A|Update" )
    @Override
    public void addIpToVniDomain( final String hostIp, final Long vni ) throws PeerException
    {
        Integer vlan = getVlanByVni( vni );

        if ( vlan != null )
        {
            try
            {
                getNetworkManager().addIpToVlanDomain( hostIp, vlan );
            }
            catch ( NetworkManagerException e )
            {
                throw new PeerException( String.format( "Error adding ip %s to domain by vlan %d", hostIp, vlan ), e );
            }
        }
        else
        {

            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    @RolesAllowed( "Environment-Management|A|Update" )
    @Override
    public void removeIpFromVniDomain( final String hostIp, final Long vni ) throws PeerException
    {
        Integer vlan = getVlanByVni( vni );

        if ( vlan != null )
        {
            try
            {
                getNetworkManager().removeIpFromVlanDomain( hostIp, vlan );
            }
            catch ( NetworkManagerException e )
            {
                throw new PeerException( String.format( "Error removing ip %s from domain by vlan %d", hostIp, vlan ),
                        e );
            }
        }
        else
        {

            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    @RolesAllowed( "Environment-Management|A|Update" )
    @Override
    public int setupContainerSsh( final String containerHostId, final int sshIdleTimeout ) throws PeerException
    {
        ContainerHost containerHost = getContainerHostById( containerHostId );

        HostInterface hostInterface = containerHost.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE );

        if ( hostInterface instanceof NullHostInterface )
        {
            throw new PeerException( "Container IP not found" );
        }

        try
        {
            return getNetworkManager().setupContainerSsh( hostInterface.getIp(), sshIdleTimeout );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( String.format( "Error setting up ssh for container ip %s", hostInterface.getIp() ),
                    e );
        }
        //        return getManagementHost().setupContainerSsh( hostInterface.getIp(), sshIdleTimeout );
    }


    protected Integer getVlanByVni( long vni ) throws PeerException
    {
        Set<Vni> reservedVnis = getReservedVnis();

        for ( Vni reservedVni : reservedVnis )
        {
            if ( reservedVni.getVni() == vni )
            {
                return reservedVni.getVlan();
            }
        }

        return null;
    }


    @Override
    public void addRequestListener( final RequestListener listener )
    {
        if ( listener != null )
        {
            requestListeners.add( listener );
        }
    }


    @Override
    public void removeRequestListener( final RequestListener listener )
    {
        if ( listener != null )
        {
            requestListeners.remove( listener );
        }
    }


    @Override
    public Set<RequestListener> getRequestListeners()
    {
        return Collections.unmodifiableSet( requestListeners );
    }

    //todo Create Environment Key (EK )  with Environment ID
    //todo Sign EK with UserKey (getActiveSession.getUser.getSecurityKeyID)
    //todo Create PEK
    //todo Sign PEK with EK and PEER Key


    /* ***********************************************
     *  Create PEK
     */
    @RolesAllowed( "Environment-Management|A|Write" )
    @Override
    public PublicKeyContainer createPeerEnvironmentKeyPair( EnvironmentId envId ) throws PeerException
    {
        KeyManager keyManager = securityManager.getKeyManager();
        EncryptionTool encTool = securityManager.getEncryptionTool();
        String pairId = String.format( "%s-%s", getId(), envId.getId() );
        final PGPSecretKeyRing peerSecKeyRing = securityManager.getKeyManager().getSecretKeyRing( null );
        try
        {
            KeyPair keyPair = keyManager.generateKeyPair( pairId, false );

            //******Create PEK *****************************************************************
            PGPSecretKeyRing secRing = PGPKeyUtil.readSecretKeyRing( keyPair.getSecKeyring() );
            PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( keyPair.getPubKeyring() );

            //***************Save Keys *********************************************************
            keyManager.saveSecretKeyRing( pairId, SecurityKeyType.PeerEnvironmentKey.getId(), secRing );
            keyManager.savePublicKeyRing( pairId, SecurityKeyType.PeerEnvironmentKey.getId(), pubRing );

            pubRing =
                    securityManager.getKeyManager().setKeyTrust( peerSecKeyRing, pubRing, KeyTrustLevel.Full.getId() );

            return new PublicKeyContainer( getId(), pubRing.getPublicKey().getFingerprint(),
                    encTool.armorByteArrayToString( pubRing.getEncoded() ) );
        }
        catch ( IOException | PGPException ex )
        {
            throw new PeerException( ex );
        }
    }


    @Override
    public void updatePeerEnvironmentPubKey( final EnvironmentId environmentId, final PGPPublicKeyRing pubKeyRing )
            throws PeerException
    {
        securityManager.getKeyManager().updatePublicKeyRing( pubKeyRing );
    }


    @Override
    public HostInterfaces getInterfaces() throws HostNotFoundException
    {
        return getManagementHost().getHostInterfaces();
    }


    @RolesAllowed( "Environment-Management|A|Update" )
    @Override
    public void setupN2NConnection( final N2NConfig config ) throws PeerException
    {
        LOG.debug( String.format( "Adding local peer to n2n community: %s:%d %s %s %s", config.getSuperNodeIp(),
                config.getN2NPort(), config.getInterfaceName(), config.getCommunityName(), config.getAddress() ) );

        try
        {
            getNetworkManager()
                    .setupN2NConnection( config.getSuperNodeIp(), config.getN2NPort(), config.getInterfaceName(),
                            config.getCommunityName(), config.getAddress(), NetworkManager.N2N_STRING_KEY,
                            config.getSharedKey() );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( "Unable add host to n2n tunnel.", e );
        }

        TunnelEntity tunnel = new TunnelEntity();

        tunnel.setEnvironmentId( config.getEnvironmentId() );
        tunnel.setCommunityName( config.getCommunityName() );
        tunnel.setInterfaceName( config.getInterfaceName() );
        tunnel.setTunnelAddress( config.getAddress() );
        tunnelDataService.saveOrUpdate( tunnel );
    }


    /**
     * Returns set of currently used n2n subnets of given peers.
     *
     * @param peers set of peers
     *
     * @return set of currently used n2n subnets.
     */
    private Set<String> getN2NSubnets( final Set<Peer> peers ) throws PeerException
    {
        Set<String> result = new HashSet<>();

        for ( Peer peer : peers )
        {
            HostInterfaces intfs = peer.getInterfaces();

            Set<HostInterfaceModel> r = intfs.filterByIp( N2NUtil.N2N_INTERFACE_IP_PATTERN );

            Collection peerSubnets = CollectionUtils.<String>collect( r, new Transformer()
            {
                @Override
                public Object transform( final Object o )
                {
                    HostInterface i = ( HostInterface ) o;
                    SubnetUtils u = new SubnetUtils( i.getIp(), PEER_SUBNET_MASK );
                    return u.getInfo().getNetworkAddress();
                }
            } );

            result.addAll( peerSubnets );
        }

        return result;
    }


    @Override
    public List<N2NConfig> setupN2NConnection( final String environmentId, final Set<Peer> peers ) throws PeerException
    {
        Set<String> usedN2NSubnets = getN2NSubnets( peers );
        LOG.debug( String.format( "Found %d n2n subnets:", usedN2NSubnets.size() ) );
        for ( String s : usedN2NSubnets )
        {
            LOG.debug( s );
        }

        String freeSubnet = N2NUtil.findFreeTunnelNetwork( usedN2NSubnets );

        LOG.debug( String.format( "Free subnet for peer: %s", freeSubnet ) );
        try
        {
            if ( freeSubnet == null )
            {
                throw new IllegalStateException( "Could not calculate subnet." );
            }
            String superNodeIp = getExternalIp();
            String interfaceName = N2NUtil.generateInterfaceName( freeSubnet );
            String communityName = N2NUtil.generateCommunityName( freeSubnet );
            String sharedKey = UUID.randomUUID().toString();
            SubnetUtils.SubnetInfo subnetInfo = new SubnetUtils( freeSubnet, N2NUtil.N2N_SUBNET_MASK ).getInfo();
            final String[] addresses = subnetInfo.getAllAddresses();
            int counter = 0;

            ExecutorService taskExecutor = Executors.newFixedThreadPool( peers.size() );

            ExecutorCompletionService<N2NConfig> executorCompletionService =
                    new ExecutorCompletionService<>( taskExecutor );


            List<N2NConfig> result = new ArrayList<>( peers.size() );
            for ( Peer peer : peers )
            {
                N2NConfig config =
                        new N2NConfig( peer.getId(), environmentId, superNodeIp, N2N_PORT, interfaceName, communityName,
                                addresses[counter], sharedKey );
                executorCompletionService.submit( new SetupN2NConnectionTask( peer, config ) );
                counter++;
            }

            for ( Peer ignored : peers )
            {
                final Future<N2NConfig> f = executorCompletionService.take();
                N2NConfig config = f.get();
                result.add( config );
                counter++;
            }

            taskExecutor.shutdown();

            return result;
        }
        catch ( Exception e )
        {
            throw new PeerException( "Could not create n2n tunnel.", e );
        }
    }


    @RolesAllowed( "Environment-Management|A|Delete" )
    @Override
    public void removeN2NConnection( final EnvironmentId environmentId ) throws PeerException
    {
        Collection<TunnelEntity> tunnels = tunnelDataService.findByEnvironmentId( environmentId );

        for ( TunnelEntity tunnel : tunnels )
        {
            try
            {
                N2NConfig config = new N2NConfig( tunnel.getTunnelAddress(), tunnel.getInterfaceName(),
                        tunnel.getCommunityName() );

                LOG.debug( String.format( "Removing peer from n2n community: %s:%d %s %s %s", config.getSuperNodeIp(),
                        config.getN2NPort(), config.getInterfaceName(), config.getCommunityName(),
                        config.getAddress() ) );
                try
                {
                    getNetworkManager().removeN2NConnection( config.getInterfaceName(), config.getCommunityName() );
                }
                catch ( PeerException | NetworkManagerException e )
                {
                    LOG.warn( "Unable remove host from n2n tunnel.", e );
                }

                removeTunnel( config.getAddress() );
                tunnelDataService.remove( tunnel.getId() );
            }
            catch ( Exception e )
            {
                LOG.warn( e.getMessage(), e );
            }
        }
    }


    @RolesAllowed( "Environment-Management|A|Write" )
    @Override
    public void createGateway( final Gateway gateway ) throws PeerException
    {
        Preconditions.checkNotNull( gateway );
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( gateway.getIp() ) && gateway.getIp().matches( Common.IP_REGEX ),
                        "Invalid gateway IP" );
        Preconditions.checkArgument( NumUtil.isIntBetween( gateway.getVlan(), Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ),
                String.format( "VLAN must be in the range from %d to %d", Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );

        //need to execute sequentially since other parallel executions can take the same gateway
        Future<Boolean> future = queueSequentialTask( new CreateGatewayTask( gateway, getNetworkManager(), this ) );

        try
        {
            future.get();
        }
        catch ( InterruptedException e )
        {
            throw new PeerException( e );
        }
        catch ( ExecutionException e )
        {
            if ( e.getCause() instanceof PeerException )
            {
                throw ( PeerException ) e.getCause();
            }
            throw new PeerException( "Error creating gateway", e.getCause() );
        }
    }


    @Override
    public void removeGateway( final int vlan ) throws PeerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ),
                String.format( "VLAN must be in the range from %d to %d", Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );

        try
        {
            getNetworkManager().removeGateway( vlan );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( String.format( "Error removing gateway tap device with VLAN %d", vlan ), e );
        }
    }


    @Override
    public ResourceHostMetrics getResourceHostMetrics()
    {
        return monitor.getResourceHostMetrics();
    }


    @Override
    public void addToTunnel( final N2NConfig config ) throws PeerException
    {
        setupN2NConnection( config );
    }


    @Override
    public List<TemplateKurjun> getTemplates()
    {
        return templateRegistry.list();
    }


    @Override
    public TemplateKurjun getTemplateByName( final String name )
    {
        return templateRegistry.getTemplate( name );
    }


    @Override
    public ResourceValue getQuota( final ContainerHost containerHost, final ResourceType resourceType )
            throws PeerException
    {
        Preconditions.checkNotNull( containerHost );
        Preconditions.checkNotNull( resourceType );
        try
        {
            return quotaManager.getQuota( containerHost.getContainerId(), resourceType );
        }
        catch ( QuotaException e )
        {
            throw new PeerException(
                    String.format( "Could not obtain quota for: %s %s", containerHost.getId(), resourceType ) );
        }
    }


    @Override
    public void setQuota( final ContainerHost containerHost, final ResourceType resourceType,
                          final ResourceValue resourceValue ) throws PeerException
    {
        Preconditions.checkNotNull( containerHost );
        Preconditions.checkNotNull( resourceType );
        try
        {
            quotaManager.setQuota( containerHost.getContainerId(), resourceType, resourceValue );
        }
        catch ( QuotaException e )
        {
            throw new PeerException(
                    String.format( "Could not set quota for: %s %s", containerHost.getId(), resourceType ) );
        }
    }


    @Override
    public ResourceValue getAvailableQuota( final ContainerId containerId, final ResourceType resourceType )
            throws PeerException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( resourceType );
        try
        {
            ContainerHost containerHost = getContainerHostById( containerId.getId() );
            return quotaManager.getAvailableQuota( containerHost.getContainerId(), resourceType );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( String.format( "Could not obtain quota for: %s %s", containerId, resourceType ) );
        }
    }


    @Override
    public ResourceValue getQuota( final ContainerId containerId, final ResourceType resourceType ) throws PeerException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( resourceType );
        try
        {
            ContainerHost containerHost = getContainerHostById( containerId.getId() );
            return quotaManager.getQuota( containerHost.getContainerId(), resourceType );
        }
        catch ( QuotaException e )
        {
            throw new PeerException(
                    String.format( "Could not obtain quota for: %s %s", containerId.getId(), resourceType ) );
        }
    }


    @Override
    public void setQuota( final ContainerId containerId, final ResourceType resourceType,
                          final ResourceValue resourceValue ) throws PeerException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( resourceType );
        try
        {
            ContainerHost containerHost = getContainerHostById( containerId.getId() );
            quotaManager.setQuota( containerHost.getContainerId(), resourceType, resourceValue );
        }
        catch ( QuotaException e )
        {
            throw new PeerException(
                    String.format( "Could not set quota for: %s %s", containerId.getId(), resourceType ) );
        }
    }


    @Override
    public ResourceValue getAvailableQuota( final ContainerHost containerHost, final ResourceType resourceType )
            throws PeerException
    {
        Preconditions.checkNotNull( containerHost );
        Preconditions.checkNotNull( resourceType );
        try
        {
            return quotaManager.getAvailableQuota( containerHost.getContainerId(), resourceType );
        }
        catch ( QuotaException e )
        {
            throw new PeerException(
                    String.format( "Could not obtain quota for: %s %s", containerHost.getId(), resourceType ) );
        }
    }


    @Override
    public void alert( AlertEvent alert )
    {
        monitor.addAlert( alert );
    }


    @Override
    public HistoricalMetrics getHistoricalMetrics( final String hostname, final Date startTime, final Date endTime )
            throws PeerException
    {
        try
        {
            Host host = findHostByName( hostname );
            return monitor.getHistoricalMetrics( host, startTime, endTime );
        }
        catch ( HostNotFoundException e )
        {
            throw new PeerException( e.getMessage(), e );
        }
    }


    private Host findHostByName( final String hostname ) throws HostNotFoundException
    {
        if ( getManagementHost().getHostname().equals( hostname ) )
        {
            return managementHost;
        }
        Host result = null;

        for ( ResourceHost resourceHost : resourceHosts )
        {
            if ( resourceHost.getHostname().equals( hostname ) )
            {
                return resourceHost;
            }
            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                if ( containerHost.getHostname().equals( hostname ) )
                {
                    return containerHost;
                }
            }
        }

        throw new HostNotFoundException( "Host by name '" + hostname + "' not found." );
    }


    public <T> Future<T> queueSequentialTask( Callable<T> callable )
    {
        return singleThreadExecutorService.submit( callable );
    }


    protected RepositoryManager getRepositoryManager() throws PeerException
    {
        try
        {
            return serviceLocator.getService( RepositoryManager.class );
        }
        catch ( NamingException e )
        {
            throw new PeerException( e );
        }
    }


    protected NetworkManager getNetworkManager() throws PeerException
    {
        try
        {
            return serviceLocator.getService( NetworkManager.class );
        }
        catch ( NamingException e )
        {
            throw new PeerException( e );
        }
    }


    private class SetupN2NConnectionTask implements Callable<N2NConfig>
    {
        private Peer peer;
        private N2NConfig n2NConfig;


        public SetupN2NConnectionTask( final Peer peer, final N2NConfig config )
        {
            this.peer = peer;
            this.n2NConfig = config;
        }


        @Override
        public N2NConfig call() throws Exception
        {
            peer.setupN2NConnection( n2NConfig );
            return n2NConfig;
        }
    }


    public void addRepository( final String ip ) throws PeerException
    {
        try
        {
            getRepositoryManager().addRepository( ip );
        }
        catch ( RepositoryException e )
        {
            //            throw new PeerException( "Error adding repository", e );
            LOG.error( "Error adding repository", e );
        }
    }


    public void removeRepository( final String host, final String ip ) throws PeerException
    {
        try
        {
            getRepositoryManager().removeRepository( ip );
        }
        catch ( RepositoryException e )
        {
            //            throw new PeerException( "Error removing repository", e );
            LOG.error( "Error removing repository", e );
        }
    }


    @RolesAllowed( "Environment-Management|A|Write" )
    @Override
    public int setupTunnels( final Map<String, String> peerIps, final String environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( peerIps, "Invalid peer ips set" );
        Preconditions.checkArgument( !peerIps.isEmpty(), "Invalid peer ips set" );
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        //need to execute sequentially since other parallel executions can setup the same tunnel
        Future<Integer> future =
                queueSequentialTask( new SetupTunnelsTask( getNetworkManager(), this, environmentId, peerIps ) );

        try
        {
            return future.get();
        }
        catch ( InterruptedException e )
        {
            throw new PeerException( e );
        }
        catch ( ExecutionException e )
        {
            if ( e.getCause() instanceof PeerException )
            {
                throw ( PeerException ) e.getCause();
            }
            throw new PeerException( "Error setting up tunnels", e.getCause() );
        }
    }


    @Override
    public Vni findVniByEnvironmentId( String environmentId ) throws PeerException
    {
        //check if vni is already reserved
        for ( Vni aVni : getReservedVnis() )
        {
            if ( aVni.getEnvironmentId().equals( environmentId ) )
            {
                return aVni;
            }
        }

        return null;
    }


    @Override
    public void removeTunnel( final String tunnelIp )
    {
        try
        {
            SubnetUtils.SubnetInfo subnetInfo = new SubnetUtils( tunnelIp, "255.255.255.0" ).getInfo();
            Set<Tunnel> tunnels = listTunnels();
            LOG.debug( String.format( "Found %d tunnels.", tunnels.size() ) );
            for ( final Tunnel tunnel : tunnels )
            {
                if ( subnetInfo.isInRange( tunnel.getTunnelIp() ) )
                {
                    getNetworkManager().removeTunnel( tunnel.getTunnelId() );
                    LOG.debug( String.format( "Tunnel '%s' destroyed successfully.", tunnel.getTunnelName() ) );
                }
            }
        }
        catch ( PeerException | NetworkManagerException e )
        {
            LOG.warn( "Error removing tunnel", e );
        }
    }


    protected Set<Tunnel> listTunnels() throws PeerException
    {
        try
        {
            return getNetworkManager().listTunnels();
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( "Error retrieving peer tunnels", e );
        }
    }


    @Override
    public String getExternalIp()
    {
        return getPeerInfo().getIp();
    }
}

