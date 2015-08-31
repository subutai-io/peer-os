package io.subutai.core.peer.impl;


import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
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
import io.subutai.common.environment.CreateContainerGroupRequest;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.Interface;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainersDestructionResult;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostInfoModel;
import io.subutai.common.peer.InterfacePattern;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.protocol.Disposable;
import io.subutai.common.protocol.Template;
import io.subutai.common.quota.CpuQuotaInfo;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.QuotaException;
import io.subutai.common.quota.QuotaInfo;
import io.subutai.common.quota.QuotaType;
import io.subutai.common.quota.RamQuota;
import io.subutai.common.security.SecurityProvider;
import io.subutai.common.security.crypto.certificate.CertificateData;
import io.subutai.common.security.crypto.certificate.CertificateManager;
import io.subutai.common.security.crypto.key.KeyManager;
import io.subutai.common.security.crypto.key.KeyPairType;
import io.subutai.common.security.crypto.keystore.KeyStoreData;
import io.subutai.common.security.crypto.keystore.KeyStoreManager;
import io.subutai.common.settings.Common;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.common.util.StringUtil;
import io.subutai.common.util.UUIDUtil;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hostregistry.api.ContainerHostInfo;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.hostregistry.api.ResourceHostInfo;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.peer.api.ContainerGroup;
import io.subutai.core.peer.api.ContainerGroupNotFoundException;
import io.subutai.core.peer.api.HostNotFoundException;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.api.Payload;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.peer.api.RequestListener;
import io.subutai.core.peer.api.ResourceHost;
import io.subutai.core.peer.api.ResourceHostException;
import io.subutai.core.peer.impl.container.ContainersDestructionResultImpl;
import io.subutai.core.peer.impl.container.CreateContainerWrapperTask;
import io.subutai.core.peer.impl.container.DestroyContainerWrapperTask;
import io.subutai.core.peer.impl.dao.ContainerGroupDataService;
import io.subutai.core.peer.impl.dao.ContainerHostDataService;
import io.subutai.core.peer.impl.dao.ManagementHostDataService;
import io.subutai.core.peer.impl.dao.PeerDAO;
import io.subutai.core.peer.impl.dao.ResourceHostDataService;
import io.subutai.core.peer.impl.entity.AbstractSubutaiHost;
import io.subutai.core.peer.impl.entity.ContainerGroupEntity;
import io.subutai.core.peer.impl.entity.ContainerHostEntity;
import io.subutai.core.peer.impl.entity.HostInterface;
import io.subutai.core.peer.impl.entity.ManagementHostEntity;
import io.subutai.core.peer.impl.entity.ResourceHostEntity;
import io.subutai.core.registry.api.RegistryException;
import io.subutai.core.registry.api.TemplateRegistry;
import io.subutai.core.strategy.api.StrategyException;
import io.subutai.core.strategy.api.StrategyManager;
import io.subutai.core.strategy.api.StrategyNotFoundException;
import io.subutai.core.http.manager.api.HttpContextManager;


/**
 * Local peer implementation
 */
public class LocalPeerImpl implements LocalPeer, HostListener, Disposable
{
    private String peerIdPath = String.format( "%s/id", Common.SUBUTAI_APP_DATA_PATH );
    private String peerIdFile = "peer_id";
    private String externalIpInterface = "eth1";
    private static final Logger LOG = LoggerFactory.getLogger( LocalPeerImpl.class );

    // 5 min
    private static final long HOST_INACTIVE_TIME = 5 * 1000 * 60;

    private static final int WAIT_CONTAINER_CONNECTION_SEC = 300;
    private DaoManager daoManager;
    private TemplateRegistry templateRegistry;
    protected ManagementHost managementHost;
    protected Set<ResourceHost> resourceHosts = Sets.newHashSet();
    private CommandExecutor commandExecutor;
    private StrategyManager strategyManager;
    private QuotaManager quotaManager;
    private Monitor monitor;
    protected ManagementHostDataService managementHostDataService;
    protected ResourceHostDataService resourceHostDataService;
    protected ContainerHostDataService containerHostDataService;
    protected ContainerGroupDataService containerGroupDataService;
    private HostRegistry hostRegistry;
    protected CommandUtil commandUtil = new CommandUtil();
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();
    protected Set<RequestListener> requestListeners = Sets.newHashSet();
    private PeerInfo peerInfo;
    private HttpContextManager httpContextManager;

    protected boolean initialized = false;


    public LocalPeerImpl( DaoManager daoManager, TemplateRegistry templateRegistry, QuotaManager quotaManager,
                          StrategyManager strategyManager, CommandExecutor commandExecutor, HostRegistry hostRegistry,
                          Monitor monitor, HttpContextManager httpContextManager )

    {
        this.strategyManager = strategyManager;
        this.daoManager = daoManager;
        this.templateRegistry = templateRegistry;
        this.quotaManager = quotaManager;
        this.monitor = monitor;
        this.commandExecutor = commandExecutor;
        this.hostRegistry = hostRegistry;
        this.httpContextManager = httpContextManager;
    }


    public void setPeerIdPath( final String peerIdPath )
    {
        this.peerIdPath = peerIdPath;
    }


    public void setPeerIdFile( final String peerIdFile )
    {
        this.peerIdFile = peerIdFile;
    }


    public void setExternalIpInterface( final String externalIpInterface )
    {
        this.externalIpInterface = externalIpInterface;
    }


    public void init()
    {
        PeerDAO peerDAO;
        try
        {
            peerDAO = new PeerDAO( daoManager );


            List<PeerInfo> result = peerDAO.getInfo( PeerManager.SOURCE_LOCAL_PEER, PeerInfo.class );
            if ( result.isEmpty() )
            {
                initPeerInfo( peerDAO );
            }
            else
            {
                peerInfo = result.get( 0 );
            }

            managementHostDataService = getManagementHostDataService();
            Collection allManagementHostEntity = managementHostDataService.getAll();
            if ( allManagementHostEntity != null && !allManagementHostEntity.isEmpty() )
            {
                managementHost = ( ManagementHost ) allManagementHostEntity.iterator().next();
                ( ( AbstractSubutaiHost ) managementHost ).setPeer( this );
                managementHost.init();
            }

            resourceHostDataService = getResourceHostDataService();
            resourceHosts.clear();
            synchronized ( resourceHosts )
            {
                resourceHosts.addAll( resourceHostDataService.getAll() );
            }
            containerHostDataService = new ContainerHostDataService( daoManager.getEntityManagerFactory() );
            containerGroupDataService = new ContainerGroupDataService( daoManager.getEntityManagerFactory() );

            setResourceHostTransientFields( getResourceHosts() );
            for ( ResourceHost resourceHost : getResourceHosts() )
            {

                setContainersTransientFields( resourceHost.getContainerHosts() );
            }

            initialized = true;
        }
        catch ( Exception e )
        {
            throw new PeerInitializationError( "Failed to init Local Peer", e );
        }
    }


    protected ManagementHostDataService getManagementHostDataService()
    {
        return new ManagementHostDataService( daoManager.getEntityManagerFactory() );
    }


    protected ResourceHostDataService getResourceHostDataService()
    {
        return new ResourceHostDataService( daoManager.getEntityManagerFactory() );
    }


    protected void initPeerInfo( PeerDAO peerDAO )
    {
        //obtain id from fs
        File scriptsDirectory = new File( peerIdPath );
        if ( !scriptsDirectory.exists() )
        {
            boolean created = scriptsDirectory.mkdirs();
            if ( created )
            {
                LOG.info( "Peer id directory created" );
            }
        }
        Path peerIdFilePath = Paths.get( peerIdPath, peerIdFile );
        File peerIdFile = peerIdFilePath.toFile();
        UUID peerId;
        try
        {
            if ( !peerIdFile.exists() )
            {
                //generate new id and save to fs
                peerId = UUID.randomUUID();
                FileUtils.writeStringToFile( peerIdFile, peerId.toString() );
            }
            else
            {
                //read id from file
                peerId = UUID.fromString( FileUtils.readFileToString( peerIdFile ) );
            }
        }
        catch ( Exception e )
        {
            throw new PeerInitializationError( "Failed to obtain peer id file", e );
        }
        peerInfo = new PeerInfo();
        peerInfo.setId( peerId );
        peerInfo.setName( "Local Subutai server" );
        //TODO get ownerId from persistent storage
        peerInfo.setOwnerId( UUID.randomUUID() );
        setPeerIp();
        peerInfo.setName( String.format( "Peer %s", peerInfo.getId() ) );

        peerDAO.saveInfo( PeerManager.SOURCE_LOCAL_PEER, peerInfo.getId().toString(), peerInfo );
    }


    private void setPeerIp()
    {
        try
        {
            Enumeration<InetAddress> addressEnumeration =
                    NetworkInterface.getByName( externalIpInterface ).getInetAddresses();
            while ( addressEnumeration.hasMoreElements() )
            {
                InetAddress address = addressEnumeration.nextElement();
                if ( address instanceof Inet4Address )
                {
                    peerInfo.setIp( address.getHostAddress() );
                }
            }
        }
        catch ( SocketException e )
        {
            LOG.error( "Error getting network interfaces", e );
        }
    }


    public void dispose()
    {
        if ( managementHost != null )
        {
            ( ( Disposable ) managementHost ).dispose();
        }

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
            ( ( ResourceHostEntity ) resourceHost ).setRegistry( templateRegistry );
            ( ( ResourceHostEntity ) resourceHost ).setMonitor( monitor );
            ( ( ResourceHostEntity ) resourceHost ).setHostRegistry( hostRegistry );
        }
    }


    @Override
    public UUID getId()
    {
        return getPeerInfo().getId();
    }


    @Override
    public String getName()
    {
        return getPeerInfo().getName();
    }


    @Override
    public UUID getOwnerId()
    {
        return getPeerInfo().getOwnerId();
    }


    @Override
    public PeerInfo getPeerInfo()
    {
        return peerInfo;
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
        Preconditions.checkNotNull( resourceHost, "Resource host is null value" );
        Preconditions.checkNotNull( template, "Pass valid template object" );
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( containerName ), "Cannot create container with null name" );

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


    protected ExecutorService getFixedExecutor( int numOfThreads )
    {
        return Executors.newFixedThreadPool( numOfThreads );
    }


    @Override
    public Set<HostInfoModel> createContainerGroup( final CreateContainerGroupRequest request ) throws PeerException
    {

        Preconditions.checkNotNull( request, "Container create request shouldn't be null" );

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


        //create gateway if initiator is not local peer
        if ( !getId().equals( request.getInitiatorPeerId() ) )
        {
            managementHost.createGateway( cidr.getInfo().getLowAddress(), vlan );
        }

        registerRemoteTemplates( request );

        Map<ResourceHost, Set<String>> containerDistribution = distributeContainersToResourceHosts( request );

        String templateName = request.getTemplates().get( request.getTemplates().size() - 1 ).getTemplateName();

        String networkPrefix = cidr.getInfo().getCidrSignature().split( "/" )[1];
        String[] allAddresses = cidr.getInfo().getAllAddresses();
        String gateway = cidr.getInfo().getLowAddress();
        int currentIpAddressOffset = 0;

        List<Future<ContainerHost>> taskFutures = Lists.newArrayList();
        ExecutorService executorService = getFixedExecutor( request.getNumberOfContainers() );

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

        return processRequestCompletion( taskFutures, executorService, request );
    }


    protected void registerRemoteTemplates( final CreateContainerGroupRequest request ) throws PeerException
    {
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
    }


    protected Map<ResourceHost, Set<String>> distributeContainersToResourceHosts(
            final CreateContainerGroupRequest request ) throws PeerException
    {
        //collect resource host metrics  & prepare templates on each of them
        List<ResourceHostMetric> serverMetricMap = Lists.newArrayList();
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            //take connected resource hosts for container creation
            //and prepare needed templates
            if ( resourceHost.isConnected() )
            {
                try
                {
                    serverMetricMap.add( resourceHost.getHostMetric() );
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
        return containerDistribution;
    }


    protected Set<HostInfoModel> processRequestCompletion( final List<Future<ContainerHost>> taskFutures,
                                                           final ExecutorService executorService,
                                                           final CreateContainerGroupRequest request )
    {
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

                LOG.error( "Error creating container group #createContainerGroup", e );
            }
        }
        return result;
    }


    @Override
    public ContainerGroup findContainerGroupByContainerId( final UUID containerId )
            throws ContainerGroupNotFoundException
    {
        Preconditions.checkNotNull( containerId, "Container is always null with null container id" );

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
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

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
        Preconditions.checkNotNull( ownerId, "Specify valid owner" );

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


    @Override
    public ContainerHost getContainerHostById( final UUID hostId ) throws HostNotFoundException
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
        Preconditions.checkNotNull( hostId, "Container host id is invalid" );

        ContainerHost c = getContainerHostById( hostId );
        ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) c;
        return containerHostEntity.getParent();
    }


    @Override
    public Host bindHost( String id ) throws HostNotFoundException
    {
        Preconditions.checkArgument( UUIDUtil.isStringAUuid( id ), "Does container host id be null?" );

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
                catch ( HostNotFoundException ignore )
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
        Preconditions.checkNotNull( id, "Host id is null" );

        return bindHost( id.toString() );
    }


    @Override
    public void startContainer( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Check container host object" );

        ContainerHostEntity containerHost = ( ContainerHostEntity ) bindHost( host.getId() );
        ResourceHost resourceHost = containerHost.getParent();
        try
        {
            resourceHost.startContainerHost( containerHost );
        }
        catch ( ResourceHostException e )
        {
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
        Preconditions.checkNotNull( host, "Cannot operate on null container host" );

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
        Preconditions.checkNotNull( host, "Container host is already null" );

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
                cleanupEnvironmentNetworkSettings( containerGroup );
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


    protected void cleanupEnvironmentNetworkSettings( final ContainerGroupEntity containerGroup )
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


    @Override
    public void setDefaultGateway( final ContainerHost host, final String gatewayIp ) throws PeerException
    {

        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( gatewayIp ) && gatewayIp.matches( Common.IP_REGEX ),
                "Invalid gateway IP" );

        try
        {
            commandUtil.execute( new RequestBuilder( String.format( "route add default gw %s %s", gatewayIp,
                            Common.DEFAULT_CONTAINER_INTERFACE ) ), bindHost( host.getId() ) );
        }
        catch ( CommandException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public boolean isConnected( final Host host )
    {
        Preconditions.checkNotNull( host, "Host is null, nothig to do here" );

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
            LOG.error( "Error checking host connected status #isConnected", e );
            return false;
        }
    }


    private boolean isTimedOut( long lastHeartbeat, long timeoutInMillis )
    {
        return ( System.currentTimeMillis() - lastHeartbeat ) > timeoutInMillis;
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
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo )
    {
        if ( initialized )
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
                    LOG.warn( "Host not found in #onHeartbeat", e );
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
    }


    protected void saveResourceHostContainers( ResourceHost resourceHost, Set<ContainerHostInfo> containerHostInfos )
    {
        Set<ContainerHost> oldHosts = resourceHost.getContainerHosts();
        Set<UUID> newContainerIds = Sets.newHashSet();
        for ( ContainerHostInfo containerHostInfo : containerHostInfos )
        {
            newContainerIds.add( containerHostInfo.getId() );

            ContainerHost containerHost = new ContainerHostEntity( getId().toString(), containerHostInfo );
            setContainersTransientFields( Sets.newHashSet( containerHost ) );
            ( ( ResourceHostEntity ) resourceHost ).addContainerHost( containerHost );

            if ( containerHostDataService.find( containerHostInfo.getId().toString() ) != null )
            {
                containerHostDataService.update( ( ContainerHostEntity ) containerHost );
            }
            else
            {
                containerHostDataService.persist( ( ContainerHostEntity ) containerHost );
            }
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
    public RamQuota getRamQuotaInfo( final ContainerHost host ) throws PeerException
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
                LOG.error( "error destroying environment containers #destroyEnvironmentContainers", e );
            }
        }

        destroyContainers( containerHosts, destroyedContainersIds, errors, containerGroup );

        String exception = null;

        if ( !errors.isEmpty() )
        {
            exception = String.format( "There were errors while destroying containers: %s", errors );
        }

        return new ContainersDestructionResultImpl( getId(), destroyedContainersIds, exception );
    }


    private void destroyContainers( final Set<ContainerHost> containerHosts, final Set<UUID> destroyedContainersIds,
                                    final Set<Throwable> errors, final ContainerGroup containerGroup )
    {
        if ( !containerHosts.isEmpty() )
        {
            List<Future<UUID>> taskFutures = Lists.newArrayList();
            ExecutorService executorService = getFixedExecutor( containerHosts.size() );

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
                    getManagementHost().cleanupEnvironmentNetworkSettings( containerGroup.getEnvironmentId() );
                }
                catch ( PeerException e )
                {
                    errors.add( exceptionUtil.getRootCause( e ) );
                }
            }
        }
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

                keyStoreManager.importCertificateHEXString( keyStore, keyStoreData );
                //***********************************************************************
                LOG.debug( String.format( "Importing new certificate to trustStore with alias: %s", alias ) );
                httpContextManager.reloadTrustStore();
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
        String alias = String.format( "env_%s_%s", getPeerInfo().getId().toString(), environmentId.toString() );

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

                httpContextManager.reloadKeyStore();
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
                String[] parseId = alias.split( "_" );
                LOG.info( String.format( "Parsing alias: %s", alias ) );
                UUID envIdFromAlias = parseId.length == 2 ? UUID.fromString( parseId[2] ) : null;
                if ( envIdFromAlias != null && envIdFromAlias.equals( environmentId ) )
                {
                    LOG.debug( String.format( "Removing environment certificate with alias: %s", alias ) );
                    storeData.setAlias( alias );
                    KeyStore keyStoreToRemove = trustStoreManager.load( storeData );
                    trustStoreManager.deleteEntry( keyStoreToRemove, storeData );
                }
            }

            //***********************************************************************
            httpContextManager.reloadTrustStore();
        }
        catch ( KeyStoreException e )
        {
            LOG.error( "Error removing environment certificate.", e );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in #removeEnvironmentCertificateFromStore", e );
        }
    }


    @Override
    public int setupTunnels( final Set<String> peerIps, final UUID environmentId ) throws PeerException
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( peerIps ), "Invalid peer ips set" );
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        return managementHost.setupTunnels( peerIps, environmentId );
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


    private Set<Interface> getInterfacesByIp( final String pattern )
    {
        LOG.debug( pattern );
        Set<Interface> result = new HashSet<>();
        try
        {
            if ( LOG.isDebugEnabled() )
            {
                for ( Interface i : getManagementHost().getNetInterfaces() )
                {
                    LOG.debug( String.format( "%s %s %s", i.getInterfaceName(), i.getIp(), i.getMac() ) );
                }
            }
            result = Sets.filter( getManagementHost().getNetInterfaces(), new Predicate<Interface>()
            {
                @Override
                public boolean apply( final Interface anInterface )
                {
                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( String.format( "%s match %s = %s", anInterface.getIp(), pattern,
                                anInterface.getIp().matches( pattern ) ) );
                    }
                    return anInterface.getIp().matches( pattern );
                }
            } );
        }
        catch ( HostNotFoundException e )
        {
            LOG.error( e.getMessage(), e );
        }
        return result;
    }


    private Set<Interface> getInterfacesByName( final String pattern )
    {
        LOG.debug( pattern );
        Set<Interface> result = new HashSet<>();
        try
        {
            result = Sets.filter( getManagementHost().getNetInterfaces(), new Predicate<Interface>()
            {
                @Override
                public boolean apply( final Interface anInterface )
                {
                    return anInterface.getInterfaceName().matches( pattern );
                }
            } );
        }
        catch ( HostNotFoundException e )
        {
            LOG.error( e.getMessage(), e );
        }
        return result;
    }


    @Override
    public Set<Interface> getNetworkInterfaces( final InterfacePattern pattern )
    {
        if ( "ip".equals( pattern.getField() ) )
        {
            return getInterfacesByIp( pattern.getPattern() );
        }
        else if ( "name".equals( pattern.getField() ) )
        {
            return getInterfacesByName( pattern.getPattern() );
        }
        throw new IllegalArgumentException( "Unknown field." );
    }


    @Override
    public void addToSubnet( final String superNodeIp, final int n2nPort, final String interfaceName,
                             final String communityName, final String address, final String sharedKey )
            throws PeerException
    {
        LOG.debug( String.format( "Adding local peer to n2n community: %s:%d %s %s %s", superNodeIp, n2nPort,
                interfaceName, communityName, address ) );

        getManagementHost().addToSubnet( superNodeIp, n2nPort, interfaceName, communityName, address, sharedKey );
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
}

