package org.safehaus.subutai.core.peer.impl;


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.environment.CreateContainerGroupRequest;
import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.metric.ResourceHostMetric;
import org.safehaus.subutai.common.network.Gateway;
import org.safehaus.subutai.common.network.Vni;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.ContainersDestructionResult;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.quota.CpuQuotaInfo;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.MemoryQuotaInfo;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaException;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.quota.RamQuota;
import org.safehaus.subutai.common.security.SecurityProvider;
import org.safehaus.subutai.common.security.crypto.certificate.CertificateData;
import org.safehaus.subutai.common.security.crypto.certificate.CertificateManager;
import org.safehaus.subutai.common.security.crypto.key.KeyManager;
import org.safehaus.subutai.common.security.crypto.key.KeyPairType;
import org.safehaus.subutai.common.security.crypto.keystore.KeyStoreData;
import org.safehaus.subutai.common.security.crypto.keystore.KeyStoreManager;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.ExceptionUtil;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.executor.api.CommandExecutor;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostDisconnectedException;
import org.safehaus.subutai.core.hostregistry.api.HostListener;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.peer.api.ContainerGroup;
import org.safehaus.subutai.core.peer.api.ContainerGroupNotFoundException;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.api.ResourceHostException;
import org.safehaus.subutai.core.peer.impl.container.ContainersDestructionResultImpl;
import org.safehaus.subutai.core.peer.impl.container.CreateContainerWrapperTask;
import org.safehaus.subutai.core.peer.impl.container.DestroyContainerWrapperTask;
import org.safehaus.subutai.core.peer.impl.dao.ContainerGroupDataService;
import org.safehaus.subutai.core.peer.impl.dao.ContainerHostDataService;
import org.safehaus.subutai.core.peer.impl.dao.ManagementHostDataService;
import org.safehaus.subutai.core.peer.impl.dao.ResourceHostDataService;
import org.safehaus.subutai.core.peer.impl.entity.AbstractSubutaiHost;
import org.safehaus.subutai.core.peer.impl.entity.ContainerGroupEntity;
import org.safehaus.subutai.core.peer.impl.entity.ContainerHostEntity;
import org.safehaus.subutai.core.peer.impl.entity.ManagementHostEntity;
import org.safehaus.subutai.core.peer.impl.entity.ResourceHostEntity;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.ssl.manager.api.CustomSslContextFactory;
import org.safehaus.subutai.core.strategy.api.StrategyException;
import org.safehaus.subutai.core.strategy.api.StrategyManager;
import org.safehaus.subutai.core.strategy.api.StrategyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


/**
 * Local peer implementation
 */
public class LocalPeerImpl implements LocalPeer, HostListener
{
    private static final Logger LOG = LoggerFactory.getLogger( LocalPeerImpl.class );

    private static final long HOST_INACTIVE_TIME = 5 * 1000 * 60; // 5 min
    private static final int WAIT_CONTAINER_CONNECTION_SEC = 300;
    private PeerManager peerManager;
    private TemplateRegistry templateRegistry;
    private ManagementHost managementHost;
    private Set<ResourceHost> resourceHosts = Sets.newHashSet();
    private CommandExecutor commandExecutor;
    private StrategyManager strategyManager;
    private QuotaManager quotaManager;
    private Monitor monitor;
    private IdentityManager identityManager;
    private ManagementHostDataService managementHostDataService;
    private ResourceHostDataService resourceHostDataService;
    private ContainerHostDataService containerHostDataService;
    private ContainerGroupDataService containerGroupDataService;
    private HostRegistry hostRegistry;
    private Set<RequestListener> requestListeners;
    private CommandUtil commandUtil = new CommandUtil();
    private ExceptionUtil exceptionUtil = new ExceptionUtil();

    private CustomSslContextFactory sslContextFactory;


    public LocalPeerImpl( PeerManager peerManager, TemplateRegistry templateRegistry, QuotaManager quotaManager,
                          StrategyManager strategyManager, Set<RequestListener> requestListeners,
                          CommandExecutor commandExecutor, HostRegistry hostRegistry, Monitor monitor,
                          IdentityManager identityManager )

    {
        this.strategyManager = strategyManager;
        this.peerManager = peerManager;
        this.templateRegistry = templateRegistry;
        this.quotaManager = quotaManager;
        this.monitor = monitor;
        this.requestListeners = requestListeners;
        this.commandExecutor = commandExecutor;
        this.hostRegistry = hostRegistry;
        this.identityManager = identityManager;
    }


    public void init()
    {
        managementHostDataService = new ManagementHostDataService( peerManager.getEntityManagerFactory() );
        Collection allManagementHostEntity = managementHostDataService.getAll();
        if ( allManagementHostEntity != null && allManagementHostEntity.size() > 0 )
        {
            managementHost = ( ManagementHost ) allManagementHostEntity.iterator().next();
            ( ( AbstractSubutaiHost ) managementHost ).setPeer( this );
            managementHost.init();
        }

        resourceHostDataService = new ResourceHostDataService( peerManager.getEntityManagerFactory() );
        resourceHosts = Sets.newHashSet();
        synchronized ( resourceHosts )
        {
            resourceHosts.addAll( resourceHostDataService.getAll() );
        }
        containerHostDataService = new ContainerHostDataService( peerManager.getEntityManagerFactory() );
        containerGroupDataService = new ContainerGroupDataService( peerManager.getEntityManagerFactory() );

        setResourceHostTransientFields( getResourceHosts() );
        for ( ResourceHost resourceHost : getResourceHosts() )
        {

            setContainersTransientFields( resourceHost.getContainerHosts() );
        }


        hostRegistry.addHostListener( this );
    }


    private void setResourceHostTransientFields( Set<ResourceHost> resourceHosts )
    {
        for ( ResourceHost resourceHost : resourceHosts )
        {
            ( ( AbstractSubutaiHost ) resourceHost ).setPeer( this );
            ( ( ResourceHostEntity ) resourceHost ).setRegistry( templateRegistry );
            ( ( ResourceHostEntity ) resourceHost ).setMonitor( monitor );
            ( ( ResourceHostEntity ) resourceHost ).setHostRegistry( hostRegistry );
        }
    }


    public void shutdown()
    {
        hostRegistry.removeHostListener( this );
    }


    @Override
    public UUID getId()
    {
        return peerManager.getLocalPeerInfo().getId();
    }


    @Override
    public String getName()
    {
        return peerManager.getLocalPeerInfo().getName();
    }


    @Override
    public UUID getOwnerId()
    {
        return peerManager.getLocalPeerInfo().getOwnerId();
    }


    @Override
    public PeerInfo getPeerInfo()
    {
        return peerManager.getLocalPeerInfo();
    }


    @Override
    public ContainerHostState getContainerHostState( final ContainerHost host ) throws PeerException
    {
        Host ahost = bindHost( host.getId() );

        if ( ahost instanceof ContainerHost )
        {
            ContainerHost containerHost = ( ContainerHost ) ahost;
            return containerHost.getState();
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }


    @Override
    public ContainerHost createContainer( final ResourceHost resourceHost, final Template template,
                                          final String containerName ) throws PeerException
    {
        Preconditions.checkNotNull( resourceHost, "Invalid resource host" );
        Preconditions.checkNotNull( template, "Invalid template" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerName ), "Invalid container name" );

        getResourceHostByName( resourceHost.getHostname() );

        if ( templateRegistry.getTemplate( template.getTemplateName() ) == null )
        {
            throw new PeerException( String.format( "Template %s not registered", template.getTemplateName() ) );
        }

        try
        {
            return resourceHost
                    .createContainer( template.getTemplateName(), /*Arrays.asList( template ), */containerName, 180 );
        }
        catch ( ResourceHostException e )
        {
            LOG.error( "Failed to create container", e );
            throw new PeerException( e );
        }
    }


    @Override
    public Set<HostInfoModel> createContainerGroup( final CreateContainerGroupRequest request ) throws PeerException
    {

        Preconditions.checkNotNull( request, "Invalid request" );

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

        //setup networking
        int vlan = setupTunnels( request.getPeerIps(), request.getEnvironmentId() );

        //create gateway
        managementHost.createGateway( cidr.getInfo().getLowAddress(), vlan );


        //try to register remote templates with local registry
        try
        {
            for ( Template t : request.getTemplates() )
            {
                if ( t.isRemote() )
                {
                    tryToRegister( t );
                }
            }
        }
        catch ( RegistryException e )
        {
            throw new PeerException( e );
        }


        //collect resource host metrics  & prepare templates on each of them
        List<ResourceHostMetric> serverMetricMap = new ArrayList<>();
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            //take connected resource hosts for container creation
            //and prepare needed templates
            if ( resourceHost.isConnected() )
            {
                try
                {
                    serverMetricMap.add( resourceHost.getHostMetric() );
                    //                    resourceHost.prepareTemplates( request.getTemplates() );
                }
                catch ( ResourceHostException e )
                {
                    throw new PeerException( e );
                }
            }
        }

        //calculate placement strategy
        Map<ResourceHostMetric, Integer> slots;
        try
        {
            slots = strategyManager.getPlacementDistribution( serverMetricMap, request.getNumberOfContainers(),
                    request.getStrategyId(), request.getCriteria() );
        }
        catch ( StrategyException e )
        {
            throw new PeerException( e );
        }


        //distribute new containers' names across selected resource hosts
        Map<ResourceHost, Set<String>> containerDistribution = Maps.newHashMap();
        String templateName = request.getTemplates().get( request.getTemplates().size() - 1 ).getTemplateName();

        for ( Map.Entry<ResourceHostMetric, Integer> e : slots.entrySet() )
        {
            Set<String> hostCloneNames = new HashSet<>();
            for ( int i = 0; i < e.getValue(); i++ )
            {
                String newContainerName = StringUtil
                        .trimToSize( String.format( "%s%s", templateName, UUID.randomUUID() ).replace( "-", "" ),
                                Common.MAX_CONTAINER_NAME_LEN );
                hostCloneNames.add( newContainerName );
            }
            ResourceHost resourceHost = getResourceHostByName( e.getKey().getHost() );
            containerDistribution.put( resourceHost, hostCloneNames );
        }


        String networkPrefix = cidr.getInfo().getCidrSignature().split( "/" )[1];
        String[] allAddresses = cidr.getInfo().getAllAddresses();
        String gateway = cidr.getInfo().getLowAddress();
        int currentIpAddressOffset = 0;

        List<Future<ContainerHost>> taskFutures = Lists.newArrayList();
        ExecutorService executorService = Executors.newFixedThreadPool( request.getNumberOfContainers() );

        //create containers in parallel on each resource host
        for ( Map.Entry<ResourceHost, Set<String>> resourceHostDistribution : containerDistribution.entrySet() )
        {
            ResourceHostEntity resourceHostEntity = ( ResourceHostEntity ) resourceHostDistribution.getKey();

            for ( String hostname : resourceHostDistribution.getValue() )
            {

                String ipAddress = allAddresses[request.getIpAddressOffset() + currentIpAddressOffset];
                taskFutures.add( executorService.submit(
                        new CreateContainerWrapperTask( resourceHostEntity, templateName, /*request.getTemplates(),*/
                                hostname, String.format( "%s/%s", ipAddress, networkPrefix ), vlan, gateway,
                                WAIT_CONTAINER_CONNECTION_SEC ) ) );

                currentIpAddressOffset++;
            }
        }

        Set<HostInfoModel> result = Sets.newHashSet();
        Set<ContainerHost> newContainers = Sets.newHashSet();

        //wait for succeeded containers
        for ( Future<ContainerHost> future : taskFutures )
        {
            try
            {
                ContainerHost containerHost = future.get();
                newContainers.add( new ContainerHostEntity( getId().toString(),
                        hostRegistry.getContainerHostInfoById( containerHost.getId() ) ) );
                result.add( new HostInfoModel( containerHost ) );
            }
            catch ( ExecutionException | InterruptedException | HostDisconnectedException e )
            {
                LOG.error( "Error creating container", e );
            }
        }

        executorService.shutdown();

        if ( !CollectionUtil.isCollectionEmpty( newContainers ) )
        {
            ContainerGroupEntity containerGroup;
            try
            {
                //update existing container group to include new containers
                containerGroup =
                        ( ContainerGroupEntity ) findContainerGroupByEnvironmentId( request.getEnvironmentId() );


                Set<UUID> containerIds = Sets.newHashSet( containerGroup.getContainerIds() );

                for ( ContainerHost containerHost : newContainers )
                {
                    containerIds.add( containerHost.getId() );
                }

                containerGroup.setContainerIds( containerIds );

                containerGroupDataService.update( containerGroup );
            }
            catch ( ContainerGroupNotFoundException e )
            {
                //create container group for new containers
                containerGroup = new ContainerGroupEntity( request.getEnvironmentId(), request.getInitiatorPeerId(),
                        request.getOwnerId() );

                Set<UUID> containerIds = Sets.newHashSet();

                for ( ContainerHost containerHost : newContainers )
                {
                    containerIds.add( containerHost.getId() );
                }

                containerGroup.setContainerIds( containerIds );

                containerGroupDataService.persist( containerGroup );
            }
        }

        return result;
    }


    @Override
    public ContainerGroup findContainerGroupByContainerId( final UUID containerId )
            throws ContainerGroupNotFoundException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );

        List<ContainerGroupEntity> containerGroups = ( List<ContainerGroupEntity> ) containerGroupDataService.getAll();

        for ( ContainerGroupEntity containerGroup : containerGroups )
        {
            for ( UUID containerHostId : containerGroup.getContainerIds() )
            {
                if ( containerId.equals( containerHostId ) )
                {
                    return containerGroup;
                }
            }
        }

        throw new ContainerGroupNotFoundException();
    }


    @Override
    public ContainerGroup findContainerGroupByEnvironmentId( final UUID environmentId )
            throws ContainerGroupNotFoundException
    {
        Preconditions.checkNotNull( environmentId, "Invalid container id" );

        List<ContainerGroupEntity> containerGroups = ( List<ContainerGroupEntity> ) containerGroupDataService.getAll();

        for ( ContainerGroupEntity containerGroup : containerGroups )
        {
            if ( environmentId.equals( containerGroup.getEnvironmentId() ) )
            {
                return containerGroup;
            }
        }

        throw new ContainerGroupNotFoundException();
    }


    @Override
    public Set<ContainerGroup> findContainerGroupsByOwnerId( final UUID ownerId )
    {
        Preconditions.checkNotNull( ownerId, "Invalid owner id" );

        Set<ContainerGroup> result = Sets.newHashSet();

        List<ContainerGroupEntity> containerGroups = ( List<ContainerGroupEntity> ) containerGroupDataService.getAll();

        for ( ContainerGroupEntity containerGroup : containerGroups )
        {

            if ( ownerId.equals( containerGroup.getOwnerId() ) )
            {
                result.add( containerGroup );
            }
        }

        return result;
    }


    private void tryToRegister( final Template template ) throws RegistryException
    {
        LOG.debug( String.format( "Trying to register template %s...", template.getTemplateName() ) );
        if ( templateRegistry.getTemplate( template.getTemplateName() ) == null )
        {
            templateRegistry.registerTemplate( template );
        }
    }


    @Override
    public ContainerHost getContainerHostByName( String hostname ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid container hostname" );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            try
            {
                return resourceHost.getContainerHostByName( hostname );
            }
            catch ( HostNotFoundException e )
            {
                //ignore
            }
        }

        throw new HostNotFoundException( String.format( "No container host found for name %s", hostname ) );
    }


    @Override
    public ContainerHost getContainerHostById( final UUID hostId ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( hostId, "Invalid container id" );

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


    @Override
    public HostInfo getContainerHostInfoById( final UUID containerHostId ) throws PeerException
    {
        ContainerHost containerHost = getContainerHostById( containerHostId );

        return new HostInfoModel( containerHost );
    }


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


    @Override
    public ResourceHost getResourceHostById( final UUID hostId ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( hostId, "Invalid resource host id" );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            if ( resourceHost.getId().equals( hostId ) )
            {
                return resourceHost;
            }
        }
        throw new HostNotFoundException( String.format( "Resource host not found by id %s", hostId ) );
    }


    @Override
    public ResourceHost getResourceHostByContainerName( final String containerName ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerName ), "Invalid container name" );

        ContainerHost c = getContainerHostByName( containerName );
        ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) c;
        return containerHostEntity.getParent();
    }


    @Override
    public ResourceHost getResourceHostByContainerId( final UUID hostId ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( hostId, "Invalid container id" );

        ContainerHost c = getContainerHostById( hostId );
        ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) c;
        return containerHostEntity.getParent();
    }


    @Override
    public Host bindHost( String id ) throws HostNotFoundException
    {
        Preconditions.checkArgument( UUIDUtil.isStringAUuid( id ), "Invalid host id" );

        UUID hostId = UUID.fromString( id );

        if ( getManagementHost().getId().equals( hostId ) )
        {
            return getManagementHost();
        }

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            if ( resourceHost.getId().equals( hostId ) )
            {
                return resourceHost;
            }
            else
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
        }

        throw new HostNotFoundException( String.format( "Host by id %s is not registered", id ) );
    }


    @Override
    public Host bindHost( UUID id ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( id, "Invalid host id" );

        return bindHost( id.toString() );
    }


    @Override
    public void startContainer( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Container host is null" );

        ContainerHostEntity containerHost = ( ContainerHostEntity ) bindHost( host.getId() );
        ResourceHost resourceHost = containerHost.getParent();
        try
        {
            resourceHost.startContainerHost( containerHost );
        }
        catch ( ResourceHostException e )
        {
            //            containerHost.setState( ContainerState.UNKNOWN );
            throw new PeerException( String.format( "Could not start LXC container [%s]", e.toString() ) );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
    }


    @Override
    public void stopContainer( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Container host is null" );

        ContainerHostEntity containerHost = ( ContainerHostEntity ) bindHost( host.getHostId() );
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


    @Override
    public void destroyContainer( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Container host is null" );

        try
        {
            ContainerHostEntity entity = ( ContainerHostEntity ) bindHost( host.getId() );
            ResourceHost resourceHost = entity.getParent();
            resourceHost.destroyContainerHost( host );
            containerHostDataService.remove( host.getHostId() );
            ( ( ResourceHostEntity ) entity.getParent() ).removeContainerHost( entity );

            //update container group
            ContainerGroupEntity containerGroup =
                    ( ContainerGroupEntity ) findContainerGroupByContainerId( host.getId() );

            Set<UUID> containerIds = containerGroup.getContainerIds();
            containerIds.remove( host.getId() );

            if ( containerIds.isEmpty() )
            {
                containerGroupDataService.remove( containerGroup.getEnvironmentId().toString() );

                //cleanup environment network settings
                try
                {
                    getManagementHost().cleanupEnvironmentNetworkSettings( containerGroup.getEnvironmentId() );
                }
                catch ( PeerException e )
                {
                    LOG.error( "Error cleaning up environment network configuration", exceptionUtil.getRootCause( e ) );
                }
            }
            else
            {
                containerGroup.setContainerIds( containerIds );

                containerGroupDataService.update( containerGroup );
            }
        }
        catch ( ResourceHostException e )
        {
            String errMsg = String.format( "Could not destroy container [%s]", host.getHostname() );
            LOG.error( errMsg, e );
            throw new PeerException( errMsg, e.toString() );
        }
        catch ( ContainerGroupNotFoundException e )
        {
            LOG.error( "Could not find container group", e );
        }
    }


    @Override
    public void setDefaultGateway( final ContainerHost host, final String gatewayIp ) throws PeerException
    {

        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( gatewayIp ) && gatewayIp.matches( Common.IP_REGEX ),
                "Invalid gateway IP" );

        try
        {
            commandUtil.execute( new RequestBuilder(
                    String.format( "route add default gw %s %s", gatewayIp, Common.DEFAULT_CONTAINER_INTERFACE ) ),
                    bindHost( host.getId() ) );
        }
        catch ( CommandException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public boolean isConnected( final Host host )
    {
        Preconditions.checkNotNull( host, "Container host is null" );

        try
        {
            HostInfo hostInfo = hostRegistry.getHostInfoById( host.getId() );
            if ( hostInfo instanceof ContainerHostInfo )
            {
                return ContainerHostState.RUNNING.equals( ( ( ContainerHostInfo ) hostInfo ).getStatus() );
            }

            Host h = bindHost( host.getId() );
            return !isTimedOut( h.getLastHeartbeat(), HOST_INACTIVE_TIME );
        }
        catch ( PeerException | HostDisconnectedException e )
        {
            return false;
        }
    }


    private boolean isTimedOut( long lastHeartbeat, long timeoutInMillis )
    {
        return ( System.currentTimeMillis() - lastHeartbeat ) > timeoutInMillis;
    }


    @Override
    public PeerQuotaInfo getQuota( ContainerHost host, final QuotaType quota ) throws PeerException
    {
        try
        {
            Host c = bindHost( host.getHostId() );
            return quotaManager.getQuota( c.getHostname(), quota );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public QuotaInfo getQuotaInfo( ContainerHost host, final QuotaType quota ) throws PeerException
    {
        try
        {
            Host c = bindHost( host.getHostId() );
            return quotaManager.getQuotaInfo( c.getId(), quota );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public void setQuota( ContainerHost host, final QuotaInfo quota ) throws PeerException
    {
        try
        {
            Host c = bindHost( host.getHostId() );
            quotaManager.setQuota( c.getHostname(), quota );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public ManagementHost getManagementHost() throws HostNotFoundException
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
            return Sets.newConcurrentHashSet( resourceHosts );
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


    public void cleanDb()
    {
        if ( managementHost != null && managementHost.getId() != null )
        {
            managementHostDataService.remove( managementHost.getHostId() );
            managementHost = null;
        }

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                containerHostDataService.remove( containerHost.getHostId() );
                ( ( ResourceHostEntity ) resourceHost ).removeContainerHost( containerHost );
            }
            resourceHostDataService.remove( resourceHost.getHostId() );
        }
        synchronized ( resourceHosts )
        {
            resourceHosts.clear();
        }
    }


    @Override
    public Template getTemplate( final String templateName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );

        return templateRegistry.getTemplate( templateName );
    }


    @Override
    public boolean isOnline() throws PeerException
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


    private <T, V> V sendRequestInternal( final T request, final String recipient, final Class<V> responseType )
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
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo )
    {
        //todo put updating host fields logic to updateHostInfo method
        if ( resourceHostInfo.getHostname().equals( "management" ) )
        {
            if ( managementHost == null )
            {
                managementHost = new ManagementHostEntity( getId().toString(), resourceHostInfo );
                ( ( AbstractSubutaiHost ) managementHost ).setPeer( this );
                try
                {
                    managementHost.init();
                }
                catch ( Exception e )
                {
                    LOG.error( "Error initializing management host", e );
                }
                managementHostDataService.persist( ( ManagementHostEntity ) managementHost );
            }
            else
            {
                ( ( AbstractSubutaiHost ) managementHost ).setNetInterfaces( resourceHostInfo.getInterfaces() );
                managementHostDataService.update( ( ManagementHostEntity ) managementHost );
            }
            ( ( AbstractSubutaiHost ) managementHost ).updateHostInfo( resourceHostInfo );
        }
        else
        {
            ResourceHost host;
            try
            {
                host = getResourceHostByName( resourceHostInfo.getHostname() );

                saveResourceHostContainers( host, resourceHostInfo.getContainers() );
            }
            catch ( HostNotFoundException e )
            {
                host = new ResourceHostEntity( getId().toString(), resourceHostInfo );
                host.init();
                resourceHostDataService.persist( ( ResourceHostEntity ) host );
                addResourceHost( host );
                setResourceHostTransientFields( Sets.newHashSet( host ) );

                saveResourceHostContainers( host, resourceHostInfo.getContainers() );
            }
            ( ( AbstractSubutaiHost ) host ).updateHostInfo( resourceHostInfo );
        }
    }


    private void saveResourceHostContainers( ResourceHost resourceHost, Set<ContainerHostInfo> containerHostInfos )
    {
        //todo put updating host fields logic to updateHostInfo method
        Set<ContainerHost> oldHosts = resourceHost.getContainerHosts();
        Set<UUID> newContainerIds = Sets.newHashSet();
        for ( ContainerHostInfo containerHostInfo : containerHostInfos )
        {

            newContainerIds.add( containerHostInfo.getId() );

            ContainerHost containerHost = null;
            try
            {
                containerHost = resourceHost.getContainerHostById( containerHostInfo.getId() );
            }
            catch ( HostNotFoundException e )
            {
                //ignore
            }


            if ( containerHost == null )
            {
                containerHost = new ContainerHostEntity( getId().toString(), containerHostInfo );
                setContainersTransientFields( Sets.newHashSet( containerHost ) );
                ( ( ResourceHostEntity ) resourceHost ).addContainerHost( containerHost );
                containerHostDataService.persist( ( ContainerHostEntity ) containerHost );
            }
            else
            {
                //update network interfaces
                ( ( AbstractSubutaiHost ) containerHost ).setNetInterfaces( containerHostInfo.getInterfaces() );
                containerHostDataService.update( ( ContainerHostEntity ) containerHost );
            }
            ( ( ContainerHostEntity ) containerHost ).updateHostInfo( containerHostInfo );
        }

        for ( ContainerHost oldHost : oldHosts )
        {
            if ( !newContainerIds.contains( oldHost.getId() ) )
            {
                //remove container which is missing in heartbeat
                containerHostDataService.remove( oldHost.getHostId() );
                ( ( ResourceHostEntity ) resourceHost ).removeContainerHost( oldHost );
            }
        }
    }


    private void setContainersTransientFields( final Set<ContainerHost> containerHosts )
    {
        for ( ContainerHost containerHost : containerHosts )
        {
            ( ( AbstractSubutaiHost ) containerHost ).setPeer( this );
            ( ( ContainerHostEntity ) containerHost ).setDataService( containerHostDataService );
            ( ( ContainerHostEntity ) containerHost ).setLocalPeer( this );
        }
    }


    // ********** Quota functions *****************


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final ContainerHost host, final int processPid )
            throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkArgument( processPid > 0, "Process pid must be greater than 0" );

        try
        {
            Host c = bindHost( host.getId() );
            return monitor.getProcessResourceUsage( ( ContainerHost ) c, processPid );
        }
        catch ( MonitorException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public int getRamQuota( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        try
        {
            return quotaManager.getRamQuota( host.getId() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public MemoryQuotaInfo getRamQuotaInfo( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        try
        {
            return quotaManager.getRamQuotaInfo( host.getId() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public void setRamQuota( final ContainerHost host, final int ramInMb ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkArgument( ramInMb > 0, "Ram quota value must be greater than 0" );

        try
        {
            quotaManager.setRamQuota( host.getId(), ramInMb );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public int getCpuQuota( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        try
        {
            return quotaManager.getCpuQuota( host.getId() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public CpuQuotaInfo getCpuQuotaInfo( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        try
        {
            return quotaManager.getCpuQuotaInfo( host.getId() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public void setCpuQuota( final ContainerHost host, final int cpuPercent ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkArgument( cpuPercent > 0, "Cpu quota value must be greater than 0" );

        try
        {
            quotaManager.setCpuQuota( host.getId(), cpuPercent );
        }
        catch ( QuotaException e )
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
            return quotaManager.getCpuSet( host.getId() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public void setCpuSet( final ContainerHost host, final Set<Integer> cpuSet ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( cpuSet ), "Empty cpu set" );

        try
        {
            quotaManager.setCpuSet( host.getId(), cpuSet );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public DiskQuota getDiskQuota( final ContainerHost host, final DiskPartition diskPartition ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkNotNull( diskPartition, "Invalid disk partition" );

        try
        {
            return quotaManager.getDiskQuota( host.getId(), diskPartition );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public void setDiskQuota( final ContainerHost host, final DiskQuota diskQuota ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkNotNull( diskQuota, "Invalid disk quota" );

        try
        {
            quotaManager.setDiskQuota( host.getId(), diskQuota );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public void setRamQuota( final ContainerHost host, final RamQuota ramQuota ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkNotNull( ramQuota, "Invalid ram quota" );

        try
        {
            quotaManager.setRamQuota( host.getId(), ramQuota );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public int getAvailableRamQuota( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        try
        {
            return quotaManager.getAvailableRamQuota( host.getId() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public int getAvailableCpuQuota( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        try
        {
            return quotaManager.getAvailableCpuQuota( host.getId() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public DiskQuota getAvailableDiskQuota( final ContainerHost host, final DiskPartition diskPartition )
            throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkNotNull( diskPartition, "Invalid disk partition" );

        try
        {
            return quotaManager.getAvailableDiskQuota( host.getId(), diskPartition );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public ContainersDestructionResult destroyEnvironmentContainers( final UUID environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        Set<Throwable> errors = Sets.newHashSet();
        Set<UUID> destroyedContainersIds = Sets.newHashSet();
        ContainerGroup containerGroup;

        try
        {
            containerGroup = findContainerGroupByEnvironmentId( environmentId );
        }
        catch ( ContainerGroupNotFoundException e )
        {
            return new ContainersDestructionResultImpl( getId(), destroyedContainersIds, "Container group not found" );
        }

        Set<ContainerHost> containerHosts = Sets.newHashSet();

        for ( UUID containerId : containerGroup.getContainerIds() )
        {
            try
            {
                containerHosts.add( getContainerHostById( containerId ) );
            }
            catch ( HostNotFoundException e )
            {
                errors.add( e );
            }
        }

        if ( !containerHosts.isEmpty() )
        {
            List<Future<UUID>> taskFutures = Lists.newArrayList();
            ExecutorService executorService = Executors.newFixedThreadPool( containerHosts.size() );

            for ( ContainerHost containerHost : containerHosts )
            {

                taskFutures.add( executorService.submit( new DestroyContainerWrapperTask( this, containerHost ) ) );
            }

            for ( Future<UUID> taskFuture : taskFutures )
            {
                try
                {
                    destroyedContainersIds.add( taskFuture.get() );
                }
                catch ( ExecutionException | InterruptedException e )
                {
                    errors.add( exceptionUtil.getRootCause( e ) );
                }
            }

            executorService.shutdown();

            //cleanup environment network settings
            if ( containerGroup.getContainerIds().size() == destroyedContainersIds.size() )
            {
                try
                {
                    getManagementHost().cleanupEnvironmentNetworkSettings( environmentId );
                }
                catch ( PeerException e )
                {
                    errors.add( exceptionUtil.getRootCause( e ) );
                }
            }

        }

        String exception = null;

        if ( !errors.isEmpty() )
        {
            exception = String.format( "There were errors while destroying containers: %s", errors );
        }

        return new ContainersDestructionResultImpl( getId(), destroyedContainersIds, exception );
    }


    //networking


    public Set<Gateway> getGateways() throws PeerException
    {
        return getManagementHost().getGateways();
    }


    @Override
    public int reserveVni( final Vni vni ) throws PeerException
    {
        Preconditions.checkNotNull( vni, "Invalid vni" );

        return getManagementHost().reserveVni( vni );
    }


    @Override
    public Set<Vni> getReservedVnis() throws PeerException
    {
        return getManagementHost().getReservedVnis();
    }


    @Override
    public void importCertificate( final String cert, final String alias ) throws PeerException
    {
        //************ Save Trust SSL Cert **************************************
        try
        {
            KeyStoreData keyStoreData = new KeyStoreData();

            keyStoreData.setupTrustStorePx2();
            keyStoreData.setHEXCert( cert );
            keyStoreData.setAlias( alias );

            KeyStoreManager keyStoreManager = new KeyStoreManager();
            KeyStore keyStore = keyStoreManager.load( keyStoreData );
            List<String> aliasList = Collections.list( keyStore.aliases() );
            if ( !aliasList.contains( alias ) )
            {
                keyStoreData.setAlias( alias );

                //        keyStoreManager.getEntries(  )

                keyStoreManager.importCertificateHEXString( keyStore, keyStoreData );
                //***********************************************************************
                LOG.debug( String.format( "Importing new certificate to trustStore with alias: %s", alias ) );
                this.sslContextFactory.reloadTrustStore();
                //        new Thread( new RestartCoreServlet( 4 ) ).start();
            }
        }
        catch ( KeyStoreException e )
        {
            LOG.error( "Error getting aliases", e );
        }
    }


    /**
     * Exports certificate with alias passed and returns cert in HEX String format. And stores new certificate in
     * keyStore.
     *
     * @param environmentId - environmentId to generate cert for
     *
     * @return - certificate in HEX format
     */
    @Override
    public String exportEnvironmentCertificate( final UUID environmentId ) throws PeerException
    {
        String alias = String.format( "env_%s_%s", peerManager.getLocalPeer().getPeerInfo().getId().toString(),
                environmentId.toString() );

        KeyStoreData environmentKeyStoreData = new KeyStoreData();
        environmentKeyStoreData.setupKeyStorePx2();
        environmentKeyStoreData.setAlias( alias );

        KeyStoreManager keyStoreManager = new KeyStoreManager();
        KeyStore keyStore = keyStoreManager.load( environmentKeyStoreData );

        try
        {
            List<String> aliasList = Collections.list( keyStore.aliases() );
            if ( !aliasList.contains( alias ) )
            {
                KeyManager keyManager = new KeyManager();
                KeyPairGenerator keyPairGenerator = keyManager.prepareKeyPairGeneration( KeyPairType.RSA, 1024 );
                KeyPair keyPair = keyManager.generateKeyPair( keyPairGenerator );

                CertificateData certData = new CertificateData();
                //TODO Instead of envId CN Will be gpg fingerprint.
                certData.setCommonName( alias );

                CertificateManager certManager = new CertificateManager();
                certManager.setDateParamaters();

                X509Certificate cert = certManager
                        .generateSelfSignedCertificate( keyStore, keyPair, SecurityProvider.BOUNCY_CASTLE, certData );

                keyStoreManager.saveX509Certificate( keyStore, environmentKeyStoreData, cert, keyPair );

                sslContextFactory.reloadKeyStore();
                LOG.debug( String.format( "Saving new certificate to keyStore with alias: %s", alias ) );
            }
        }
        catch ( KeyStoreException e )
        {
            LOG.error( "Error getting environment", e );
        }
        LOG.debug( String.format( "Returning certificate for alias %s", alias ) );
        return keyStoreManager.exportCertificateHEXString( keyStore, environmentKeyStoreData );
    }


    /**
     * Remove specific environment related certificates from trustStore of local peer.
     *
     * @param environmentId - environment whose certificates need to be removed
     */
    @Override
    public void removeEnvironmentCertificates( final UUID environmentId ) throws PeerException
    {
        KeyStoreData storeData = new KeyStoreData();

        storeData.setupTrustStorePx2();
        removeEnvironmentCertificateFromStore( environmentId, storeData );
        LOG.debug( "clearing up trustStore" );


        storeData.setupKeyStorePx2();
        removeEnvironmentCertificateFromStore( environmentId, storeData );
        LOG.debug( "clearing up keyStore" );
    }


    private void removeEnvironmentCertificateFromStore( UUID environmentId, KeyStoreData storeData )
    {
        try
        {
            //************ Delete Trust SSL Cert **************************************
            KeyStore trustStore;
            KeyStoreManager trustStoreManager;

            trustStoreManager = new KeyStoreManager();

            trustStore = trustStoreManager.load( storeData );

            List<String> aliasList = Collections.list( trustStore.aliases() );
            for ( final String alias : aliasList )
            {
                String parseId[] = alias.split( "_" );
                LOG.info( String.format( "Parsing alias: %s", alias ) );
                if ( parseId.length == 2 )
                {
                    UUID envIdFromAlias = UUID.fromString( parseId[2] );
                    if ( envIdFromAlias.equals( environmentId ) )
                    {
                        LOG.debug( String.format( "Removing environment certificate with alias: %s", alias ) );
                        storeData.setAlias( alias );
                        KeyStore keyStoreToRemove = trustStoreManager.load( storeData );
                        trustStoreManager.deleteEntry( keyStoreToRemove, storeData );
                    }
                }
            }

            //***********************************************************************
            //            new Thread( new RestartCoreServlet() ).start();
            sslContextFactory.reloadTrustStore();
        }
        catch ( KeyStoreException e )
        {
            LOG.error( "Error removing environment certificate.", e );
        }
    }


    ;


    @Override
    public int setupTunnels( final Set<String> peerIps, final UUID environmentId ) throws PeerException
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( peerIps ), "Invalid peer ips set" );
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        return managementHost.setupTunnels( peerIps, environmentId );
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof LocalPeerImpl ) )
        {
            return false;
        }

        final LocalPeerImpl that = ( LocalPeerImpl ) o;

        return getId().equals( that.getId() );
    }


    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }


    public void setSslContextFactory( final CustomSslContextFactory sslContextFactory )
    {
        this.sslContextFactory = sslContextFactory;
    }


    public CustomSslContextFactory getSslContextFactory()
    {
        return sslContextFactory;
    }
}

