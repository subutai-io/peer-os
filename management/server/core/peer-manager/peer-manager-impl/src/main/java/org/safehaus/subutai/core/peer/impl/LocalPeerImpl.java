package org.safehaus.subutai.core.peer.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.host.HostInfo;
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
import org.safehaus.subutai.common.quota.QuotaException;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.executor.api.CommandExecutor;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostDisconnectedException;
import org.safehaus.subutai.core.hostregistry.api.HostListener;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;
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
import org.safehaus.subutai.core.strategy.api.ServerMetric;
import org.safehaus.subutai.core.strategy.api.StrategyException;
import org.safehaus.subutai.core.strategy.api.StrategyManager;
import org.safehaus.subutai.core.strategy.api.StrategyNotAvailable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final int MAX_LXC_NAME = 15;
    private PeerManager peerManager;
    private TemplateRegistry templateRegistry;
    private ManagementHost managementHost;
    private Set<ResourceHost> resourceHosts = Sets.newHashSet();
    private CommandExecutor commandExecutor;
    private StrategyManager strategyManager;
    private QuotaManager quotaManager;
    private Monitor monitor;
    private ConcurrentMap<String, AtomicInteger> sequences;
    private ManagementHostDataService managementHostDataService;
    private ResourceHostDataService resourceHostDataService;
    private ContainerHostDataService containerHostDataService;
    private ContainerGroupDataService containerGroupDataService;
    private HostRegistry hostRegistry;
    private Set<RequestListener> requestListeners;


    public LocalPeerImpl( PeerManager peerManager, TemplateRegistry templateRegistry, QuotaManager quotaManager,
                          StrategyManager strategyManager, Set<RequestListener> requestListeners,
                          CommandExecutor commandExecutor, HostRegistry hostRegistry, Monitor monitor )

    {
        this.strategyManager = strategyManager;
        this.peerManager = peerManager;
        this.templateRegistry = templateRegistry;
        this.quotaManager = quotaManager;
        this.monitor = monitor;
        this.requestListeners = requestListeners;
        this.commandExecutor = commandExecutor;
        this.hostRegistry = hostRegistry;
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
        resourceHosts.addAll( resourceHostDataService.getAll() );

        containerHostDataService = new ContainerHostDataService( peerManager.getEntityManagerFactory() );
        containerGroupDataService = new ContainerGroupDataService( peerManager.getEntityManagerFactory() );

        setResourceHostTransientFields( resourceHosts );
        for ( ResourceHost resourceHost : resourceHosts )
        {

            setContainersTransientFields( resourceHost.getContainerHosts() );
        }


        hostRegistry.addHostListener( this );
        sequences = new ConcurrentHashMap<>();
    }


    private void setResourceHostTransientFields( Set<ResourceHost> resourceHosts )
    {
        for ( ResourceHost resourceHost : resourceHosts )
        {
            ( ( AbstractSubutaiHost ) resourceHost ).setPeer( this );
            ( ( ResourceHostEntity ) resourceHost ).setRegistry( templateRegistry );
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
    public ContainerHostState getContainerHostState( final String containerId ) throws PeerException
    {
        Host host = bindHost( containerId );

        if ( host instanceof ContainerHost )
        {
            ContainerHost containerHost = ( ContainerHost ) host;
            return containerHost.getState();
        }
        else
        {
            throw new UnsupportedOperationException( "Unsupported action." );
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
            return resourceHost.createContainer( template.getTemplateName(), containerName, 180 );
        }
        catch ( ResourceHostException e )
        {
            LOG.error( "Failed to create container", e );
            throw new PeerException( e );
        }
    }


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

        final int waitContainerTimeoutSec = 180;

        //check if strategy exists
        try
        {
            strategyManager.findStrategyById( strategyId );
        }
        catch ( StrategyNotAvailable e )
        {
            throw new PeerException( e );
        }


        //try to register remote templates with local registry
        try
        {
            for ( Template t : templates )
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


        List<ServerMetric> serverMetricMap = new ArrayList<>();
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            //take connected resource hosts for container creation
            //and prepare needed templates
            if ( resourceHost.isConnected() )
            {
                try
                {
                    serverMetricMap.add( resourceHost.getMetric() );
                    resourceHost.prepareTemplates( templates );
                }
                catch ( ResourceHostException e )
                {
                    throw new PeerException( e );
                }
            }
        }

        //calculate placement strategy
        Map<ServerMetric, Integer> slots;
        try
        {
            slots = strategyManager
                    .getPlacementDistribution( serverMetricMap, numberOfContainers, strategyId, criteria );
        }
        catch ( StrategyException e )
        {
            throw new PeerException( e );
        }

        Set<String> existingContainerNames = getContainerNames();

        //distribute new containers' names across selected resource hosts
        Map<ResourceHost, Set<String>> containerDistribution = Maps.newHashMap();
        String templateName = templates.get( templates.size() - 1 ).getTemplateName();

        for ( Map.Entry<ServerMetric, Integer> e : slots.entrySet() )
        {
            Set<String> hostCloneNames = new HashSet<>();
            for ( int i = 0; i < e.getValue(); i++ )
            {
                String newContainerName = nextHostName( templateName, existingContainerNames );
                hostCloneNames.add( newContainerName );
            }
            ResourceHost resourceHost = getResourceHostByName( e.getKey().getHostname() );
            containerDistribution.put( resourceHost, hostCloneNames );
        }


        List<Future<ContainerHost>> taskFutures = Lists.newArrayList();
        ExecutorService executorService = Executors.newFixedThreadPool( numberOfContainers );

        //create containers in parallel on each resource host
        for ( Map.Entry<ResourceHost, Set<String>> resourceHostDistribution : containerDistribution.entrySet() )
        {
            ResourceHostEntity resourceHostEntity = ( ResourceHostEntity ) resourceHostDistribution.getKey();

            for ( String hostname : resourceHostDistribution.getValue() )
            {
                taskFutures.add( executorService.submit(
                        new CreateContainerWrapperTask( resourceHostEntity, templateName, hostname,
                                waitContainerTimeoutSec ) ) );
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
                LOG.error( "Error creating containers", e );
            }
        }

        executorService.shutdown();

        if ( !CollectionUtil.isCollectionEmpty( newContainers ) )
        {
            ContainerGroupEntity containerGroup;

            try
            {
                //update existing container group to include new containers
                containerGroup = ( ContainerGroupEntity ) findContainerGroupByEnvironmentId( environmentId );

                Set<UUID> containerIds = Sets.newHashSet( containerGroup.getContainerIds() );

                for ( ContainerHost containerHost : newContainers )
                {
                    containerIds.add( containerHost.getId() );
                }

                containerGroup.setContainerIds2( containerIds );

                containerGroupDataService.update( containerGroup );
            }
            catch ( ContainerGroupNotFoundException e )
            {
                //create container group for new containers
                containerGroup = new ContainerGroupEntity( environmentId, initiatorPeerId, ownerId, templateName,
                        newContainers );


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


    private void setContainersTransientFields( final Set<ContainerHost> containerHosts )
    {
        for ( ContainerHost containerHost : containerHosts )
        {
            ( ( AbstractSubutaiHost ) containerHost ).setPeer( this );
            ( ( ContainerHostEntity ) containerHost ).setDataService( containerHostDataService );
        }
    }


    @Override
    public String getFreeHostName( final String prefix )
    {
        return nextHostName( prefix, getContainerNames() );
    }


    private String nextHostName( String templateName, Set<String> existingNames )
    {
        AtomicInteger i = sequences.putIfAbsent( templateName, new AtomicInteger() );
        if ( i == null )
        {
            i = sequences.get( templateName );
        }
        while ( true )
        {
            String suffix = String.valueOf( i.incrementAndGet() );
            int prefixLen = MAX_LXC_NAME - suffix.length();
            String name = ( templateName.length() > prefixLen ? templateName.substring( 0, prefixLen ) : templateName )
                    + suffix;
            if ( !existingNames.contains( name ) )
            {
                return name;
            }
        }
    }


    private Set<String> getContainerNames()
    {
        Set<String> result = new HashSet<>();
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                result.add( containerHost.getHostname() );
            }
        }
        return result;
    }


    private void tryToRegister( final Template template ) throws RegistryException
    {
        if ( templateRegistry.getTemplate( template.getTemplateName() ) == null )
        {
            templateRegistry.registerTemplate( template );
        }
    }


    @Override
    public ContainerHost getContainerHostByName( String hostname ) throws HostNotFoundException
    {
        ContainerHost result = null;
        Iterator<ResourceHost> iterator = getResourceHosts().iterator();
        while ( result == null && iterator.hasNext() )
        {
            result = iterator.next().getContainerHostByName( hostname );
        }
        if ( result == null )
        {
            throw new HostNotFoundException( String.format( "Container host %s not found.", hostname ) );
        }
        return result;
    }


    @Override
    public ContainerHost getContainerHostById( final String hostId ) throws HostNotFoundException
    {
        ContainerHost result = null;
        Iterator<ResourceHost> iterator = getResourceHosts().iterator();
        while ( result == null && iterator.hasNext() )
        {
            result = iterator.next().getContainerHostById( hostId );
        }
        if ( result == null )
        {
            throw new HostNotFoundException( String.format( "Container host by id %s not found.", hostId ) );
        }
        return result;
    }


    @Override
    public ResourceHost getResourceHostByName( String hostname ) throws HostNotFoundException
    {
        ResourceHost result = null;
        Iterator iterator = getResourceHosts().iterator();

        while ( result == null && iterator.hasNext() )
        {
            ResourceHost host = ( ResourceHost ) iterator.next();

            if ( host.getHostname().equals( hostname ) )
            {
                result = host;
            }
        }
        if ( result == null )
        {
            throw new HostNotFoundException( String.format( "Resource host %s not found.", hostname ) );
        }
        return result;
    }


    @Override
    public ResourceHost getResourceHostByContainerName( final String containerName ) throws HostNotFoundException
    {
        ContainerHost c = getContainerHostByName( containerName );
        ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) c;
        return containerHostEntity.getParent();
    }


    @Override
    public ResourceHost getResourceHostByContainerId( final String hostId ) throws HostNotFoundException
    {
        ContainerHost c = getContainerHostById( hostId );
        ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) c;
        return containerHostEntity.getParent();
    }


    @Override
    public Host bindHost( String id ) throws HostNotFoundException
    {
        Host result = null;
        Iterator<ResourceHost> iterator = getResourceHosts().iterator();
        while ( result == null && iterator.hasNext() )
        {
            ResourceHost rh = iterator.next();
            if ( rh.getHostId().equals( id ) )
            {
                result = rh;
            }
            else
            {
                result = rh.getContainerHostById( id );
            }
        }

        if ( result == null )
        {
            if ( !getManagementHost().getHostId().equals( id ) )
            {
                throw new HostNotFoundException( String.format( "Host by id %s is not registered.", id ) );
            }
            else
            {
                result = getManagementHost();
            }
        }


        return result;
    }


    @Override
    public Host bindHost( UUID id ) throws HostNotFoundException
    {
        return bindHost( id.toString() );
    }


    @Override
    public <T extends Host> T bindHost( T host ) throws HostNotFoundException
    {
        Host result = null;
        Iterator<ResourceHost> iterator = getResourceHosts().iterator();
        while ( result == null && iterator.hasNext() )
        {
            ResourceHost rh = iterator.next();
            if ( rh.getHostId().equals( host.getHostId() ) )
            {
                result = rh;
            }
            else
            {
                result = rh.getContainerHostById( host.getHostId() );
            }
        }

        if ( result == null )
        {
            if ( !getManagementHost().getHostId().equals( host.getHostId() ) )
            {
                throw new HostNotFoundException(
                        String.format( "Host by id %s is not registered.", host.getHostId() ) );
            }
            else
            {
                result = getManagementHost();
            }
        }
        return ( T ) result;
    }


    @Override
    public void startContainer( final ContainerHost host ) throws PeerException
    {
        Host c = bindHost( host );
        ContainerHostEntity containerHost = ( ContainerHostEntity ) c;
        ResourceHost resourceHost = containerHost.getParent();
        try
        {
            if ( resourceHost.startContainerHost( containerHost ) )
            {
                //                containerHost.setState( ContainerHostState.RUNNING );
            }
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
        Host c = bindHost( host.getHostId() );
        ContainerHostEntity containerHost = ( ContainerHostEntity ) c;
        ResourceHost resourceHost = containerHost.getParent();
        try
        {
            if ( resourceHost.stopContainerHost( containerHost ) )
            {
                //                containerHost.setState( ContainerState.STOPPED );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
    }


    @Override
    public void destroyContainer( final ContainerHost containerHost ) throws PeerException
    {
        try
        {
            ContainerHost result = bindHost( containerHost );
            ContainerHostEntity entity = ( ContainerHostEntity ) result;
            ResourceHost resourceHost = entity.getParent();
            resourceHost.destroyContainerHost( containerHost );
            containerHostDataService.remove( containerHost.getHostId() );
            ( ( ResourceHostEntity ) entity.getParent() ).removeContainerHost( entity );

            //update container group
            ContainerGroupEntity containerGroup =
                    ( ContainerGroupEntity ) findContainerGroupByContainerId( containerHost.getId() );

            Set<UUID> containerIds = containerGroup.getContainerIds();
            containerIds.remove( containerHost.getId() );

            if ( containerIds.isEmpty() )
            {
                containerGroupDataService.remove( containerGroup.getEnvironmentId().toString() );
            }
            else
            {
                containerGroup.setContainerIds2( containerIds );

                containerGroupDataService.update( containerGroup );
            }
        }
        catch ( ResourceHostException e )
        {
            String errMsg = String.format( "Could not destroy container [%s]", containerHost.getHostname() );
            LOG.error( errMsg, e );
            throw new PeerException( errMsg, e.toString() );
        }
        catch ( ContainerGroupNotFoundException e )
        {
            LOG.error( "Could not find container group", e );
        }
    }


    @Override
    public boolean isConnected( final Host host )
    {
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
        return resourceHosts;
    }


    public void addResourceHost( final ResourceHost host )
    {
        Preconditions.checkNotNull( host, "Resource host could not be null." );

        resourceHosts.add( host );
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
        Preconditions.checkNotNull( requestBuilder );
        Preconditions.checkNotNull( aHost );

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
        Preconditions.checkNotNull( requestBuilder );
        Preconditions.checkNotNull( aHost );

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
            //            peerDAO.deleteInfo( SOURCE_MANAGEMENT_HOST, managementHost.getId().toString() );
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
            //            peerDAO.deleteInfo( SOURCE_RESOURCE_HOST, resourceHost.getId().toString() );
        }
        resourceHosts.clear();
    }


    @Override
    public Template getTemplate( final String templateName )
    {
        return templateRegistry.getTemplate( templateName );
    }


    @Override
    public boolean isOnline() throws PeerException
    {
        return true;
    }


    @Override
    public <T, V> V sendRequest( final T request, final String recipient, final int requestTimeout,
                                 final Class<V> responseType, final int responseTimeout ) throws PeerException
    {
        Preconditions.checkNotNull( responseType, "Invalid response type" );

        return sendRequestInternal( request, recipient, responseType );
    }


    @Override
    public <T> void sendRequest( final T request, final String recipient, final int requestTimeout )
            throws PeerException
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
        if ( resourceHostInfo.getHostname().equals( "management" ) )
        {
            if ( managementHost == null )
            {
                managementHost = new ManagementHostEntity( getId().toString(), resourceHostInfo );
                try
                {
                    managementHost.init();
                }
                catch ( Exception e )
                {
                    LOG.error( e.toString() );
                }
                managementHostDataService.persist( ( ManagementHostEntity ) managementHost );
                ( ( AbstractSubutaiHost ) managementHost ).setPeer( this );
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
        for ( ContainerHostInfo containerHostInfo : containerHostInfos )
        {
            if ( containerHostInfo.getInterfaces().size() == 0 )
            {
                continue;
            }
            Host containerHost;
            try
            {
                containerHost = bindHost( containerHostInfo.getId() );
            }
            catch ( HostNotFoundException hnfe )
            {
                containerHost = new ContainerHostEntity( getId().toString(), containerHostInfo );
                ( ( AbstractSubutaiHost ) containerHost ).setPeer( this );
                ( ( ContainerHostEntity ) containerHost ).setDataService( containerHostDataService );
                ( ( ResourceHostEntity ) resourceHost ).addContainerHost( ( ContainerHostEntity ) containerHost );
                containerHostDataService.persist( ( ContainerHostEntity ) containerHost );
            }
            ( ( AbstractSubutaiHost ) containerHost ).updateHostInfo( containerHostInfo );
        }
    }

    // ********** Quota functions *****************


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final UUID containerId, final int processPid )
            throws PeerException
    {
        try
        {
            Host c = bindHost( containerId );
            return monitor.getProcessResourceUsage( ( ContainerHost ) c, processPid );
        }
        catch ( MonitorException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public int getRamQuota( final UUID containerId ) throws PeerException
    {
        try
        {
            return quotaManager.getRamQuota( containerId );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public void setRamQuota( final UUID containerId, final int ramInMb ) throws PeerException
    {
        try
        {
            quotaManager.setRamQuota( containerId, ramInMb );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public int getCpuQuota( final UUID containerId ) throws PeerException
    {
        try
        {
            return quotaManager.getCpuQuota( containerId );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public void setCpuQuota( final UUID containerId, final int cpuPercent ) throws PeerException
    {
        try
        {
            quotaManager.setCpuQuota( containerId, cpuPercent );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public Set<Integer> getCpuSet( final UUID containerId ) throws PeerException
    {
        try
        {
            return quotaManager.getCpuSet( containerId );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public void setCpuSet( final UUID containerId, final Set<Integer> cpuSet ) throws PeerException
    {
        try
        {
            quotaManager.setCpuSet( containerId, cpuSet );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public DiskQuota getDiskQuota( final UUID containerId, final DiskPartition diskPartition ) throws PeerException
    {
        try
        {
            return quotaManager.getDiskQuota( containerId, diskPartition );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public void setDiskQuota( final UUID containerId, final DiskQuota diskQuota ) throws PeerException
    {
        try
        {
            quotaManager.setDiskQuota( containerId, diskQuota );
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

        Set<Exception> errors = Sets.newHashSet();
        Set<UUID> destroyedContainersIds = Sets.newHashSet();

        try
        {
            ContainerGroup containerGroup = findContainerGroupByEnvironmentId( environmentId );

            Set<ContainerHost> containerHosts = Sets.newHashSet();

            for ( UUID containerId : containerGroup.getContainerIds() )
            {
                try
                {
                    containerHosts.add( getContainerHostById( containerId.toString() ) );
                }
                catch ( HostNotFoundException e )
                {
                    errors.add( e );
                }
            }

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
                    errors.add( e );
                }
            }


            executorService.shutdown();

            String exception = null;

            if ( !errors.isEmpty() )
            {
                exception = String.format( "There were errors while destroying containers: %s", errors );
            }

            return new ContainersDestructionResultImpl( destroyedContainersIds, exception );
        }
        catch ( ContainerGroupNotFoundException e )
        {
            throw new PeerException( e );
        }
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

