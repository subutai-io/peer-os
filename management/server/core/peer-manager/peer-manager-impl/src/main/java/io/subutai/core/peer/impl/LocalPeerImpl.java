package io.subutai.core.peer.impl;


import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
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

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.subutai.common.environment.CreateEnvironmentContainerGroupRequest;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.Interface;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainersDestructionResult;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostInfoModel;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.InterfacePattern;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.Disposable;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.protocol.Template;
import io.subutai.common.quota.CpuQuotaInfo;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.QuotaException;
import io.subutai.common.quota.QuotaInfo;
import io.subutai.common.quota.QuotaType;
import io.subutai.common.quota.RamQuota;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.settings.Common;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.common.util.StringUtil;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.api.Payload;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.peer.api.RequestListener;
import io.subutai.core.peer.impl.container.ContainersDestructionResultImpl;
import io.subutai.core.peer.impl.container.CreateContainerWrapperTask;
import io.subutai.core.peer.impl.container.DestroyContainerWrapperTask;
import io.subutai.core.peer.impl.dao.ManagementHostDataService;
import io.subutai.core.peer.impl.dao.PeerDAO;
import io.subutai.core.peer.impl.dao.ResourceHostDataService;
import io.subutai.core.peer.impl.entity.AbstractSubutaiHost;
import io.subutai.core.peer.impl.entity.ContainerHostEntity;
import io.subutai.core.peer.impl.entity.ManagementHostEntity;
import io.subutai.core.peer.impl.entity.ResourceHostEntity;
import io.subutai.core.registry.api.TemplateRegistry;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.strategy.api.StrategyException;
import io.subutai.core.strategy.api.StrategyManager;
import io.subutai.core.strategy.api.StrategyNotFoundException;


/**
 * Local peer implementation
 */
public class LocalPeerImpl implements LocalPeer, HostListener, Disposable
{
    private static final Logger LOG = LoggerFactory.getLogger( LocalPeerImpl.class );

    private String externalIpInterface = "eth1";
    private DaoManager daoManager;
    private TemplateRegistry templateRegistry;
    protected ManagementHostEntity managementHost;
    protected Set<ResourceHost> resourceHosts = Sets.newHashSet();
    private CommandExecutor commandExecutor;
    private StrategyManager strategyManager;
    private QuotaManager quotaManager;
    private Monitor monitor;
    protected ManagementHostDataService managementHostDataService;
    protected ResourceHostDataService resourceHostDataService;
    //    protected ContainerHostDataService containerHostDataService;
    //    protected ContainerGroupDataService containerGroupDataService;
    private HostRegistry hostRegistry;
    protected CommandUtil commandUtil = new CommandUtil();
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();
    protected Set<RequestListener> requestListeners = Sets.newHashSet();
    protected PeerInfo peerInfo;
    private SecurityManager securityManager;

    protected boolean initialized = false;


    public LocalPeerImpl( DaoManager daoManager, TemplateRegistry templateRegistry, QuotaManager quotaManager,
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


    public void setExternalIpInterface( final String externalIpInterface )
    {
        this.externalIpInterface = externalIpInterface;
    }


    public void init()
    {
        LOG.debug( "********************************************** Initializing peer "
                + "******************************************" );

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
            Collection<ManagementHostEntity> allManagementHostEntity = managementHostDataService.getAll();
            if ( allManagementHostEntity != null && !allManagementHostEntity.isEmpty() )
            {
                managementHost = allManagementHostEntity.iterator().next();
                managementHost.setPeer( this );
                managementHost.init();
            }

            resourceHostDataService = getResourceHostDataService();
            resourceHosts.clear();
            synchronized ( resourceHosts )
            {
                resourceHosts.addAll( resourceHostDataService.getAll() );
            }
            //            containerHostDataService = new ContainerHostDataService( daoManager.getEntityManagerFactory
            // () );
            //            containerGroupDataService = new ContainerGroupDataService( daoManager
            // .getEntityManagerFactory() );

            setResourceHostTransientFields( resourceHosts );

            for ( ResourceHost resourceHost : getResourceHosts() )
            {
                for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
                {
                    LOG.debug( String.format( "%s %s", resourceHost.getHostname(), containerHost.getHostname() ) );
                }


                //                setContainersTransientFields( resourceHost.getContainerHosts() );
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
        peerInfo = new PeerInfo();
        peerInfo.setId( securityManager.getKeyManager().getPeerId() );
        peerInfo.setName( "Local Subutai server" );
        peerInfo.setOwnerId( securityManager.getKeyManager().getOwnerId() );
        setPeerIp();
        peerInfo.setName( String.format( "Peer %s", peerInfo.getId() ) );

        peerDAO.saveInfo( PeerManager.SOURCE_LOCAL_PEER, peerInfo.getId(), peerInfo );
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
            resourceHost.setPeer( this );
            ( ( ResourceHostEntity ) resourceHost ).setRegistry( templateRegistry );
            ( ( ResourceHostEntity ) resourceHost ).setMonitor( monitor );
            ( ( ResourceHostEntity ) resourceHost ).setHostRegistry( hostRegistry );
        }
    }


    @Override
    public String getId()
    {
        return getPeerInfo().getId();
    }


    @Override
    public String getName()
    {
        return getPeerInfo().getName();
    }


    @Override
    public String getOwnerId()
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
            return containerHost.getStatus();
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
            return resourceHost.createContainer( template.getTemplateName(), containerName, 180 );
        }
        catch ( ResourceHostException e )
        {
            LOG.error( "Failed to create container", e );
            throw new PeerException( e );
        }
    }


    protected ExecutorService getFixedPoolExecutor( int numOfThreads )
    {
        return Executors.newFixedThreadPool( numOfThreads );
    }


    //TODO: refactor following to return set of container host
    public Set<HostInfoModel> createEnvironmentContainerGroup( final CreateEnvironmentContainerGroupRequest request )
            throws PeerException
    {
        Preconditions.checkNotNull( request );

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

        List<Future<ContainerHost>> taskFutures = Lists.newArrayList();
        ExecutorService executorService = getFixedPoolExecutor( request.getNumberOfContainers() );

        Vni environmentVni = getManagementHost().findVniByEnvironmentId( request.getEnvironmentId() );

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
                                String.format( "%s/%s", ipAddress, networkPrefix ), environmentVni.getVlan(), gateway,
                                Common.WAIT_CONTAINER_CONNECTION_SEC ) ) );

                currentIpAddressOffset++;
            }
        }


        //wait for succeeded containers
        Set<ContainerHost> newContainers = Sets.newHashSet();
        Set<HostInfoModel> result = Sets.newHashSet();

        for ( Future<ContainerHost> future : taskFutures )
        {
            try
            {
                ContainerHost containerHost = future.get();


                ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) containerHost;
                //                final ContainerHostInfo containerHostInfo =
                //                        hostRegistry.getContainerHostInfoById( containerHost.getId() );
                //
                containerHostEntity.setEnvironmentId( request.getEnvironmentId() );
                containerHostEntity.setOwnerId( request.getOwnerId() );
                containerHostEntity.setInitiatorPeerId( request.getInitiatorPeerId() );
                newContainers.add( containerHost );
                result.add( new HostInfoModel( containerHost ) );
            }
            catch ( ExecutionException | InterruptedException /*| HostDisconnectedException*/ e )
            {
                LOG.error( "Error creating container", e );
            }
        }

        executorService.shutdown();

        // updating resource host entities
        for ( ResourceHost resourceHost : containerDistribution.keySet() )
        {
            resourceHostDataService.saveOrUpdate( resourceHost );
        }

        return result;
        //        return processRequestCompletion1( taskFutures, executorService, request );
    }


    protected Map<ResourceHost, Set<String>> distributeContainersToResourceHosts(
            final CreateEnvironmentContainerGroupRequest request ) throws PeerException
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

        for ( Map.Entry<ResourceHostMetric, Integer> e : slots.entrySet() )
        {
            Set<String> hostCloneNames = new HashSet<>();
            for ( int i = 0; i < e.getValue(); i++ )
            {
                String newContainerName = StringUtil.trimToSize(
                        String.format( "%s%s", request.getTemplateName(), UUID.randomUUID() ).replace( "-", "" ),
                        Common.MAX_CONTAINER_NAME_LEN );
                hostCloneNames.add( newContainerName );
            }
            ResourceHost resourceHost = getResourceHostByName( e.getKey().getHost() );
            containerDistribution.put( resourceHost, hostCloneNames );
        }
        return containerDistribution;
    }


    //    protected Set<HostInfoModel> processRequestCompletion1( final List<Future<ContainerHost>> taskFutures,
    //                                                            final ExecutorService executorService,
    //                                                            final CreateEnvironmentContainerGroupRequest request )
    //    {
    //        Set<HostInfoModel> result = Sets.newHashSet();
    //        Set<ContainerHost> newContainers = Sets.newHashSet();
    //
    //        //wait for succeeded containers
    //        for ( Future<ContainerHost> future : taskFutures )
    //        {
    //            try
    //            {
    //                ContainerHost containerHost = future.get();
    //
    //                newContainers.add( new ContainerHostEntity( getId(),
    //                        hostRegistry.getContainerHostInfoById( containerHost.getId() ) ) );
    //                result.add( new HostInfoModel( containerHost ) );
    //            }
    //            catch ( ExecutionException | InterruptedException | HostDisconnectedException e )
    //            {
    //                LOG.error( "Error creating container", e );
    //            }
    //        }
    //
    //        executorService.shutdown();
    //
    //        if ( !CollectionUtil.isCollectionEmpty( newContainers ) )
    //        {
    //            ContainerGroupEntity containerGroup;
    //            try
    //            {
    //                //update existing container group to include new containers
    //                containerGroup =
    //                        ( ContainerGroupEntity ) findContainerGroupByEnvironmentId( request.getEnvironmentId() );
    //
    //
    //                Set<String> containerIds = Sets.newHashSet( containerGroup.getContainerIds() );
    //
    //                for ( ContainerHost containerHost : newContainers )
    //                {
    //                    containerIds.add( containerHost.getId() );
    //                }
    //
    //                containerGroup.setContainerIds( containerIds );
    //
    //                containerGroupDataService.update( containerGroup );
    //            }
    //            catch ( ContainerGroupNotFoundException e )
    //            {
    //                //create container group for new containers
    //                containerGroup = new ContainerGroupEntity( request.getEnvironmentId(), request
    // .getInitiatorPeerId(),
    //                        request.getOwnerId() );
    //
    //                Set<String> containerIds = Sets.newHashSet();
    //
    //                for ( ContainerHost containerHost : newContainers )
    //                {
    //                    containerIds.add( containerHost.getId() );
    //                }
    //
    //                containerGroup.setContainerIds( containerIds );
    //
    //                containerGroupDataService.persist( containerGroup );
    //            }
    //        }
    //        return result;
    //    }


    //    @Override
    //    public ContainerGroup findContainerGroupByContainerId( final String containerId )
    //            throws ContainerGroupNotFoundException
    //    {
    //        Preconditions.checkNotNull( containerId, "Container is always null with null container id" );
    //
    //        List<ContainerGroupEntity> containerGroups = ( List<ContainerGroupEntity> ) containerGroupDataService
    // .getAll();
    //
    //        for ( ContainerGroupEntity containerGroup : containerGroups )
    //        {
    //            for ( String containerHostId : containerGroup.getContainerIds() )
    //            {
    //                if ( containerId.equals( containerHostId ) )
    //                {
    //                    return containerGroup;
    //                }
    //            }
    //        }
    //
    //        throw new ContainerGroupNotFoundException();
    //    }


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


    //    @Override
    //    public ContainerGroup findContainerGroupByEnvironmentId( final String environmentId )
    //            throws ContainerGroupNotFoundException
    //    {
    //        Preconditions.checkNotNull( environmentId, "Invalid environment id" );
    //
    //        List<ContainerGroupEntity> containerGroups = ( List<ContainerGroupEntity> ) containerGroupDataService
    // .getAll();
    //
    //        for ( ContainerGroupEntity containerGroup : containerGroups )
    //        {
    //            if ( environmentId.equals( containerGroup.getEnvironmentId() ) )
    //            {
    //                return containerGroup;
    //            }
    //        }
    //
    //        throw new ContainerGroupNotFoundException();
    //    }


    //
    //    @Override
    //    public Set<ContainerGroup> findContainerGroupsByOwnerId( final String ownerId )
    //    {
    //        Preconditions.checkNotNull( ownerId, "Specify valid owner" );
    //
    //        Set<ContainerGroup> result = Sets.newHashSet();
    //
    //        List<ContainerGroupEntity> containerGroups = ( List<ContainerGroupEntity> ) containerGroupDataService
    // .getAll();
    //
    //        for ( ContainerGroupEntity containerGroup : containerGroups )
    //        {
    //
    //            if ( ownerId.equals( containerGroup.getOwnerId() ) )
    //            {
    //                result.add( containerGroup );
    //            }
    //        }
    //
    //        return result;
    //    }
    //
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


    @Override
    public HostInfo getContainerHostInfoById( final String containerHostId ) throws PeerException
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


    @Override
    public ResourceHost getResourceHostByContainerName( final String containerName ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerName ), "Invalid container name" );

        ContainerHost c = getContainerHostByName( containerName );
        ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) c;
        return containerHostEntity.getParent();
    }


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


        if ( getManagementHost().getId().equals( id ) )
        {
            return getManagementHost();
        }

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
    public void startContainer( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Check container host object" );

        ContainerHostEntity containerHost = ( ContainerHostEntity ) bindHost( host.getId() );
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


    @Override
    public void stopContainer( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Cannot operate on null container host" );

        ContainerHostEntity containerHost = ( ContainerHostEntity ) bindHost( host.getId() );
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
        ContainerHostEntity entity = ( ContainerHostEntity ) bindHost( host.getId() );
        ResourceHost resourceHost = entity.getParent();

        try
        {
            resourceHost.destroyContainerHost( host );
            //            containerHostDataService.remove( host.getId() );
            ( ( ResourceHostEntity ) entity.getParent() ).removeContainerHost( entity );

            //update container group
            //            ContainerGroupEntity containerGroup =
            //                    ( ContainerGroupEntity ) findContainerGroupByContainerId( host.getId() );
            //
            //            Set<String> containerIds = containerGroup.getContainerIds();
            //            containerIds.remove( host.getId() );
            cleanupEnvironmentNetworkSettings( /*containerGroup*/ host.getEnvironmentId() );

            //            if ( containerIds.isEmpty() )
            //            {
            //                cleanupEnvironmentNetworkSettings( /*containerGroup*/ host.getEnvironmentId() );
            //            }
            //            else
            //            {
            //                containerGroup.setContainerIds( containerIds );
            //
            //                containerGroupDataService.update( containerGroup );
            //            }
        }
        catch ( ResourceHostException e )
        {

            String errMsg = String.format( "Could not destroy container [%s]", host.getHostname() );
            LOG.error( errMsg, e );
            throw new PeerException( errMsg, e.toString() );
        }
        //        catch ( ContainerGroupNotFoundException e )
        //        {
        //            LOG.warn( "Could not find container group for: " + host.getHostname(), e );
        //        }
        resourceHostDataService.update( ( ResourceHostEntity ) resourceHost );
    }


    @Override
    public void cleanupEnvironmentNetworkSettings( final String environmentId ) throws PeerException
    {
        getManagementHost().cleanupEnvironmentNetworkSettings( environmentId );
    }


    @Override
    public void removeEnvironmentKeyPair( final String environmentId ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );

        KeyManager keyManager = securityManager.getKeyManager();

        keyManager.removeKeyRings( environmentId );
    }


    //    protected void cleanupEnvironmentNetworkSettings( final ContainerGroupEntity containerGroup )
    //    {
    //        containerGroupDataService.remove( containerGroup.getEnvironmentId() );
    //
    //        //cleanup environment network settings
    //        try
    //        {
    //            getManagementHost().cleanupEnvironmentNetworkSettings( containerGroup.getEnvironmentId() );
    //        }
    //        catch ( PeerException e )
    //        {
    //            LOG.error( "Error cleaning up environment network configuration", exceptionUtil.getRootCause( e ) );
    //        }
    //    }


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
        Preconditions.checkNotNull( host, "Host is null" );

        try
        {
            HostInfo hostInfo = hostRegistry.getHostInfoById( host.getId() );
            return !( hostInfo instanceof ContainerHostInfo ) || ContainerHostState.RUNNING
                    .equals( ( ( ContainerHostInfo ) hostInfo ).getStatus() );
        }
        catch ( HostDisconnectedException e )
        {
            return false;
        }
    }


    @Override
    public QuotaInfo getQuotaInfo( ContainerHost host, final QuotaType quota ) throws PeerException
    {
        try
        {
            Host c = bindHost( host.getId() );
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
            Host c = bindHost( host.getId() );
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
            //todo compare id of host with local peer id to determine management host once agent starts supplying
            //todo-> fingerprint of its pgp key as id
            if ( resourceHostInfo.getHostname().equals( "management" ) )
            {
                if ( managementHost == null )
                {
                    managementHost = new ManagementHostEntity( getId(), resourceHostInfo );
                    managementHost.setPeer( this );
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
                    managementHost.updateHostInfo( resourceHostInfo );
                    peerInfo.setIp( managementHost.getIpByInterfaceName( externalIpInterface ) );
                }
                //                ( ( AbstractSubutaiHost ) managementHost ).updateHostInfo( resourceHostInfo );
            }
            else
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
                    host.init();
                    resourceHostDataService.persist( host );
                    addResourceHost( host );
                    HashSet<ResourceHost> a = new HashSet<ResourceHost>();
                    a.add( host );
                    setResourceHostTransientFields( a );
                }
                //                updateResourceHostContainers( host, resourceHostInfo.getContainers() );
                if ( host.updateHostInfo( resourceHostInfo ) )
                {
                    resourceHostDataService.update( host );
                    LOG.debug( String.format( "Resource host %s updated.", host.getId() ) );
                }
            }
        }
    }


    //    protected void updateResourceHostContainers( ResourceHost resourceHost, Set<ContainerHostInfo>
    // containerHostInfos )
    //    {
    //        Set<ContainerHost> oldHosts = resourceHost.getContainerHosts();
    //        Set<String> newContainerIds = Sets.newHashSet();
    //        for ( ContainerHostInfo containerHostInfo : containerHostInfos )
    //        {
    //            newContainerIds.add( containerHostInfo.getId() );
    //
    //            ContainerHostEntity containerHost = new ContainerHostEntity( getId(), containerHostInfo );
    //            setContainersTransientFields( Sets.newHashSet( containerHost ) );
    //            ( ( ResourceHostEntity ) resourceHost ).addContainerHost( containerHost );
    //
    //            //            if ( containerHostDataService.find( containerHostInfo.getId() ) != null )
    //            //            {
    //            //                containerHostDataService.update( ( ContainerHostEntity ) containerHost );
    //            //            }
    //            //            else
    //            //            {
    //            //                containerHostDataService.persist( ( ContainerHostEntity ) containerHost );
    //            //            }
    //        }
    //
    //        for ( ContainerHost oldHost : oldHosts )
    //        {
    //            if ( !newContainerIds.contains( oldHost.getId() ) )
    //            {
    //                //remove container which is missing in heartbeat
    //                //                containerHostDataService.remove( oldHost.getId() );
    //                ( ( ResourceHostEntity ) resourceHost ).removeContainerHost( oldHost );
    //            }
    //        }
    //    }


    //    private void setContainersTransientFields( final Set<ContainerHost> containerHosts )
    //    {
    //        for ( ContainerHost containerHost : containerHosts )
    //        {
    //            ( ( AbstractSubutaiHost ) containerHost ).setPeer( this );
    //            //            ( ( ContainerHostEntity ) containerHost ).setDataService( containerHostDataService );
    //            //            ( ( ContainerHostEntity ) containerHost ).setLocalPeer( this );
    //        }
    //    }


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


    public ContainersDestructionResult destroyContainersByEnvironment( final String environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        Set<Throwable> errors = Sets.newHashSet();
        Set<ContainerHost> destroyedContainers = Sets.newHashSet();
        //        ContainerGroup containerGroup;

        //        try
        //        {
        //            containerGroup = findContainerGroupByEnvironmentId( environmentId );
        //        }
        //        catch ( ContainerGroupNotFoundException e )
        //        {
        //            return new ContainersDestructionResultImpl( getId(), destroyedContainers,
        //                    Common.CONTAINER_GROUP_NOT_FOUND );
        //        }

        Set<ContainerHost> containerHosts = Sets.newHashSet();


        for ( ResourceHost resourceHost : resourceHosts )
        {
            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                if ( environmentId.equals( containerHost.getEnvironmentId() ) )
                {
                    containerHosts.add( containerHost );
                }
            }
        }
        //        for ( String containerId : containerGroup.getContainerIds() )
        //        {
        //            try
        //            {
        //                containerHosts.add( getContainerHostById( containerId ) );
        //            }
        //            catch ( HostNotFoundException e )
        //            {
        //                errors.add( e );
        //            }
        //        }

        destroyedContainers = destroyContainerGroup( containerHosts, errors );

        for ( ContainerHost containerHost : destroyedContainers )
        {
            final ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) containerHost;
            ResourceHostEntity resourceHostEntity = ( ResourceHostEntity ) containerHostEntity.getParent();
            resourceHostEntity.removeContainerHost( containerHostEntity );
            resourceHostDataService.update( resourceHostEntity );
        }

        String exception = null;

        if ( !errors.isEmpty() )
        {
            exception = String.format( "There were errors while destroying containers: %s", errors );
        }

        return new ContainersDestructionResultImpl( getId(), destroyedContainers, exception );
    }


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
    public int setupTunnels( final Map<String, String> peerIps, final String environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( peerIps, "Invalid peer ips set" );
        Preconditions.checkArgument( !peerIps.isEmpty(), "Invalid peer ips set" );
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        return managementHost.setupTunnels( peerIps, environmentId );
    }


    @Override
    public String getVniDomain( final Long vni ) throws PeerException
    {
        Integer vlan = getVlanByVni( vni );

        if ( vlan != null )
        {
            return getManagementHost().getVlanDomain( vlan );
        }
        else
        {

            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    @Override
    public void removeVniDomain( final Long vni ) throws PeerException
    {
        Integer vlan = getVlanByVni( vni );

        if ( vlan != null )
        {
            getManagementHost().removeVlanDomain( vlan );
        }
        else
        {

            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    @Override
    public void setVniDomain( final Long vni, final String domain ) throws PeerException
    {
        Integer vlan = getVlanByVni( vni );

        if ( vlan != null )
        {
            getManagementHost().setVlanDomain( vlan, domain );
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
            return getManagementHost().isIpInVlanDomain( hostIp, vlan );
        }
        else
        {

            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    @Override
    public void addIpToVniDomain( final String hostIp, final Long vni ) throws PeerException
    {
        Integer vlan = getVlanByVni( vni );

        if ( vlan != null )
        {
            getManagementHost().addIpToVlanDomain( hostIp, vlan );
        }
        else
        {

            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    @Override
    public void removeIpFromVniDomain( final String hostIp, final Long vni ) throws PeerException
    {
        Integer vlan = getVlanByVni( vni );

        if ( vlan != null )
        {
            getManagementHost().removeIpFromVlanDomain( hostIp, vlan );
        }
        else
        {

            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    protected Integer getVlanByVni( long vni ) throws PeerException
    {
        Set<Vni> reservedVnis = getManagementHost().getReservedVnis();

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


    /* ***********************************************
     *  Create PEK
     */


    @Override
    public String createEnvironmentKeyPair( String environmentId ) throws PeerException
    {
        KeyManager keyManager = securityManager.getKeyManager();
        EncryptionTool encTool = securityManager.getEncryptionTool();

        try
        {

            KeyPair keyPair = keyManager.generateKeyPair( environmentId, false );


            //**********************************************************************************
            PGPSecretKeyRing secRing = PGPKeyUtil.readSecretKeyRing( keyPair.getSecKeyring() );
            PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( keyPair.getPubKeyring() );
            PGPSecretKeyRing peerSecRing = keyManager.getSecretKeyRing( null );

            //************Sign Key **************************************************************
            pubRing = encTool.signPublicKey( pubRing, getId(), peerSecRing.getSecretKey(), "" );

            //***************Save Keys *********************************************************
            keyManager.saveSecretKeyRing( environmentId, ( short ) 2, secRing );
            keyManager.savePublicKeyRing( environmentId, ( short ) 2, pubRing );

            return encTool.armorByteArrayToString( pubRing.getEncoded() );
        }
        catch ( IOException | PGPException ex )
        {
            throw new PeerException( ex );
        }
    }


    private Set<Interface> getInterfacesByIp( final String pattern )
    {
        LOG.debug( pattern );
        Set<Interface> result = new HashSet<>();
        try
        {
            result = Sets.filter( getManagementHost().getInterfaces(), new Predicate<Interface>()
            {
                @Override
                public boolean apply( final Interface anInterface )
                {
                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( String.format( "%s matches %s = %s", anInterface.getIp(), pattern,
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
        return Collections.unmodifiableSet( result );
    }


    private Set<Interface> getInterfacesByName( final String pattern )
    {
        LOG.debug( pattern );
        Set<Interface> result = new HashSet<>();
        try
        {
            result = Sets.filter( getManagementHost().getInterfaces(), new Predicate<Interface>()
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
    public void setupN2NConnection( final N2NConfig config ) throws PeerException
    {
        LOG.debug( String.format( "Adding local peer to n2n community: %s:%d %s %s %s", config.getSuperNodeIp(),
                config.getN2NPort(), config.getInterfaceName(), config.getCommunityName(), config.getAddress() ) );


        getManagementHost().setupN2NConnection( config );
    }


    @Override
    public void removeN2NConnection( final N2NConfig config ) throws PeerException
    {
        LOG.debug( String.format( "Removing peer from n2n community: %s:%d %s %s %s", config.getSuperNodeIp(),
                config.getN2NPort(), config.getInterfaceName(), config.getCommunityName(), config.getAddress() ) );
        getManagementHost().removeN2NConnection( config );
        getManagementHost().removeTunnel( config.getAddress() );
    }


    @Override
    public void createGateway( final String environmentGatewayIp, final int vlan ) throws PeerException
    {
        getManagementHost().createGateway( environmentGatewayIp, vlan );
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

