package org.safehaus.subutai.core.peer.impl;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.executor.api.CommandExecutor;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostListener;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;
import org.safehaus.subutai.core.lxc.quota.api.QuotaException;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.peer.api.ContainerCreateOrder;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.ContainerState;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.PeerEvent;
import org.safehaus.subutai.core.peer.api.PeerEventListener;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerInfo;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.api.ResourceHostException;
import org.safehaus.subutai.core.peer.api.SubutaiInitException;
import org.safehaus.subutai.core.peer.impl.dao.ManagementHostDataService;
import org.safehaus.subutai.core.peer.impl.dao.PeerDAO;
import org.safehaus.subutai.core.peer.impl.dao.ResourceHostDataService;
import org.safehaus.subutai.core.peer.impl.model.ContainerHostEntity;
import org.safehaus.subutai.core.peer.impl.model.ManagementHostEntity;
import org.safehaus.subutai.core.peer.impl.model.ResourceHostEntity;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.strategy.api.ServerMetric;
import org.safehaus.subutai.core.strategy.api.StrategyException;
import org.safehaus.subutai.core.strategy.api.StrategyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * Local peer implementation
 */
public class LocalPeerImpl implements LocalPeer, HostListener, PeerEventListener
{
    private static final Logger LOG = LoggerFactory.getLogger( LocalPeerImpl.class );

    private static final String SOURCE_MANAGEMENT_HOST = "MANAGEMENT_HOST";
    private static final String SOURCE_RESOURCE_HOST = "RESOURCE_HOST";
    private static final long HOST_INACTIVE_TIME = 5 * 1000 * 60; // 5 min
    private static final int MAX_LXC_NAME = 15;
    private PeerManager peerManager;
    private TemplateRegistry templateRegistry;
    //    private CommunicationManager communicationManager;
    private PeerDAO peerDAO;
    private ManagementHost managementHost;
    private Set<ResourceHost> resourceHosts = Sets.newHashSet();
    private CommandExecutor commandExecutor;
    private AgentManager agentManager;
    private StrategyManager strategyManager;
    private QuotaManager quotaManager;
    private ConcurrentMap<String, AtomicInteger> sequences;
    private ManagementHostDataService managementHostDataService;
    private ResourceHostDataService resourceHostDataService;
    private HostRegistry hostRegistry;
    private Set<RequestListener> requestListeners;


    public LocalPeerImpl( PeerManager peerManager, AgentManager agentManager, TemplateRegistry templateRegistry,
                          PeerDAO peerDao, QuotaManager quotaManager, StrategyManager strategyManager,
                          Set<RequestListener> requestListeners, CommandExecutor commandExecutor,
                          HostRegistry hostRegistry )

    {
        this.agentManager = agentManager;
        this.strategyManager = strategyManager;
        this.peerManager = peerManager;
        //        this.containerManager = containerManager;
        this.templateRegistry = templateRegistry;
        this.peerDAO = peerDao;
        //        this.communicationManager = communicationManager;
        this.quotaManager = quotaManager;
        this.requestListeners = requestListeners;
        //        this.managementHostDataService = managementHostDataService;
        this.commandExecutor = commandExecutor;
        this.hostRegistry = hostRegistry;
    }


    @Override
    public void init()
    {
        //        List<ManagementHost> r1 = peerDAO.getInfo( SOURCE_MANAGEMENT_HOST, ManagementHost.class );
        //        if ( r1.size() > 0 )
        //        {
        //            managementHost = r1.get( 0 );
        //            managementHost.resetHeartbeat();
        //        }

        managementHostDataService = new ManagementHostDataService( peerManager.getEntityManagerFactory() );
        Collection allManagementHostEntity = managementHostDataService.getAll();
        if ( allManagementHostEntity != null && allManagementHostEntity.size() > 0 )
        {
            managementHost = ( ManagementHost ) allManagementHostEntity.iterator().next();
        }

        resourceHostDataService = new ResourceHostDataService( peerManager.getEntityManagerFactory() );

        resourceHosts = Sets.newHashSet();
        resourceHosts.addAll( resourceHostDataService.getAll() );

        for ( ResourceHost resourceHost : resourceHosts )
        {
            resourceHost.resetHeartbeat();
        }
        //        communicationManager.addListener( this );
        hostRegistry.addHostListener( this );
        sequences = new ConcurrentHashMap<>();
    }


    @Override
    public void shutdown()
    {
        //        communicationManager.removeListener( this );
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
    public ContainerHost createContainer( final String hostName, final String templateName, final String cloneName,
                                          final UUID environmentId ) throws PeerException
    {
        String nodeGroup = UUID.randomUUID().toString();

        ContainerCreateOrder containerCreateOrder =
                new ContainerCreateOrder( getId().toString(), cloneName, nodeGroup, environmentId.toString(), null,
                        Lists.newArrayList( getTemplate( templateName ) ) );
        ResourceHost resourceHost = getResourceHostByName( hostName );
        resourceHost.createContainer( containerCreateOrder );


        Set<ContainerHost> containerHosts =
                waitContainerOrders( resourceHost, Lists.newArrayList( containerCreateOrder ) );
        ContainerHost result = containerHosts.iterator().next();
        return result;
    }


    private Set<ContainerHost> waitContainerOrders( ResourceHost resourceHost, List<ContainerCreateOrder> orders )
            throws PeerException
    {

        Set<ContainerHost> result = new HashSet<>();
        int quantity = orders.size();
        long threshold = System.currentTimeMillis() + 120 * quantity * 1000;
        Set<String> cloneNames = new HashSet<>();
        while ( result.size() != quantity && threshold - System.currentTimeMillis() > 0 )
        {
            try
            {
                Date date = new Date( threshold - System.currentTimeMillis() );
                DateFormat formatter = new SimpleDateFormat( "HH:mm:ss" );
                LOG.info( String.format( "Waiting for container(s) on %s: %d. Ready: %d container(s). Timeout: %s ",
                        resourceHost.getHostname(), quantity, result.size(), formatter.format( date ) ) );
                Thread.sleep( 5000 );
            }
            catch ( InterruptedException ignore )
            {
            }
            result = resourceHost.getContainerHostsByNameList( cloneNames );
        }
        if ( result.size() != quantity )
        {
            throw new PeerException( String.format(
                    "Not all containers created for %s. Count of cloned container=%d. Expected count=%d).",
                    resourceHost, result.size(), quantity ) );
        }


        return result;
    }


    @Override
    public Set<ContainerHost> createContainers( final UUID creatorPeerId, final UUID environmentId,
                                                final List<Template> templates, final int quantity,
                                                final String strategyId, final List<Criteria> criteria,
                                                String nodeGroupName ) throws PeerException
    {
        LOG.info( String.format( "=============> Received: %s %d %s", nodeGroupName, quantity,
                creatorPeerId.toString() ) );

        try
        {
            for ( Template t : templates )
            {
                if ( t.isRemote() )
                {
                    tryToRegister( t );
                }
            }
            String templateName = templates.get( templates.size() - 1 ).getTemplateName();


            List<ServerMetric> serverMetricMap = new ArrayList<>();
            for ( ResourceHost resourceHost : getResourceHosts() )
            {
                if ( resourceHost.isConnected() )
                {
                    serverMetricMap.add( resourceHost.getMetric() );
                }
            }
            Map<ServerMetric, Integer> slots;
            try
            {
                slots = strategyManager.getPlacementDistribution( serverMetricMap, quantity, strategyId, criteria );
            }
            catch ( StrategyException e )
            {
                throw new PeerException( e.getMessage() );
            }

            Set<String> existingContainerNames = getContainerNames();

            // clone specified number of instances and store their names
            Map<ResourceHost, Set<String>> cloneNames = new HashMap<>();

            for ( Map.Entry<ServerMetric, Integer> e : slots.entrySet() )
            {
                Set<String> hostCloneNames = new HashSet<>();
                for ( int i = 0; i < e.getValue(); i++ )
                {
                    String newContainerName = nextHostName( templateName, existingContainerNames );
                    hostCloneNames.add( newContainerName );
                }
                ResourceHost resourceHost = getResourceHostByName( e.getKey().getHostname() );
                cloneNames.put( resourceHost, hostCloneNames );
            }

            Map<ResourceHost, List<ContainerCreateOrder>> orders = new HashMap<>();
            for ( final Map.Entry<ResourceHost, Set<String>> e : cloneNames.entrySet() )
            {
                ResourceHost rh = e.getKey();
                Set<String> clones = e.getValue();
                ResourceHost resourceHost = getResourceHostByName( rh.getHostname() );
                List<ContainerCreateOrder> containerCreateOrders = new ArrayList<>();
                for ( String cloneName : clones )
                {
                    LOG.info(
                            String.format( "+++++++++++++++++++++> Ordered: %s on %s", cloneName, rh.getHostname() ) );
                    //                    resourceHost.createContainer( this, creatorPeerId.toString(), environmentId
                    // .toString(), templates,
                    //                            cloneName, nodeGroupName );

                    ContainerCreateOrder containerCreateOrder =
                            new ContainerCreateOrder( creatorPeerId.toString(), cloneName, nodeGroupName,
                                    environmentId.toString(), null, templates );
                    containerCreateOrders.add( containerCreateOrder );
                    resourceHost.createContainer( containerCreateOrder );
                }
                orders.put( rh, containerCreateOrders );
            }
            HashSet<ContainerHost> result = new HashSet<>();

            for ( final Map.Entry<ResourceHost, List<ContainerCreateOrder>> e : orders.entrySet() )
            {
                result.addAll( waitContainerOrders( e.getKey(), e.getValue() ) );
            }

            for ( final Map.Entry<ResourceHost, List<ContainerCreateOrder>> e : orders.entrySet() )
            {
                ResourceHost resourceHost = e.getKey();
                for ( ContainerCreateOrder containerCreateOrder : e.getValue() )
                {
                    ContainerHost containerHost =
                            resourceHost.getContainerHostByName( containerCreateOrder.getHostname() );
                    containerHost.setCreatorPeerId( containerCreateOrder.getCustomerId() );
                    containerHost.setNodeGroupName( containerCreateOrder.getNodeGroupName() );
                    containerHost.setEnvironmentId( containerCreateOrder.getEnvironmentId() );
                }
            }

            return result;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            //TODO: destroy environment containers
            throw new PeerException( e.toString() );
        }
    }


    @Override
    public ContainerHost getContainerHost( final HostInfo hostInfo, final String creatorPeerId,
                                           final String environmentId, final String nodeGroupName )
    {
        Host host = null;

        if ( getId().equals( creatorPeerId ) )
        {
            try
            {
                host = bindHost( hostInfo.getId() );
            }
            catch ( HostNotFoundException ignore )
            {

            }
        }
        if ( host == null )
        {
            host = new ContainerHostEntity( getId().toString(), creatorPeerId, environmentId.toString(), nodeGroupName,
                    hostInfo );
        }
        return ( ContainerHost ) host;
    }


    @Override
    public void onPeerEvent( PeerEvent event )
    {
        LOG.info( String.format( "Received onPeerEvent: %s %s.", event.getType(), event.getObject() ) );
        try
        {
            switch ( event.getType() )
            {
                case CONTAINER_CREATE_SUCCESS:
                    ContainerHostEntity containerHost = ( ContainerHostEntity ) event.getObject();
                    //                    try
                    //                    {
                    //                        ResourceHost resourceHost = containerHost.getParent();
                    //                        if ( resourceHost == null )
                    //                        {
                    //                            throw new PeerException( "Resource host not found to register
                    // container." );
                    //                        }
                    //                        LOG.info( String.format( "Resource host %s. Containers count: =%d",
                    // resourceHost.getHostname(),
                    //                                resourceHost.getContainerHosts().size() ) );
                    //                        resourceHost.addContainerHost( containerHost );
                    //                        LOG.info( String.format( "Registered new container: %s %s %s",
                    // containerHost.getHostname(),
                    //                                containerHost.getEnvironmentId(), containerHost
                    // .getNodeGroupName() ) );
                    //                        peerDAO.saveInfo( SOURCE_RESOURCE_HOST, resourceHost.getId()
                    // .toString(), resourceHost );

                    //                        resourceHostDataService.update( ( ResourceHostEntity ) resourceHost );
                    //                        LOG.info( String.format( "Resource host %s saved. Containers count:
                    // =%d",
                    //                                resourceHost.getHostname(), resourceHost.getContainerHosts()
                    // .size() ) );
                    //                    }
                    //                    catch ( PeerException e )
                    //                    {
                    //                        LOG.error( "Error in onPeerEvent", e );
                    //                    }
                    break;
                case CONTAINER_CREATE_FAIL:
                    Exception e = ( Exception ) event.getObject();
                    LOG.error( "Container clone failed.", e );
                    break;
            }
        }
        catch ( Exception e )
        {
            LOG.error( "onPeerEvent: unhandled exception.", e );
        }
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


    private Set<String> getContainerNames() throws PeerException
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
    public Set<ContainerHost> getContainerHostsByEnvironmentId( final UUID environmentId )
    {
        Set<ContainerHost> result = new HashSet<>();
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            result.addAll( resourceHost.getContainerHostsByEnvironmentId( environmentId ) );
        }
        return result;
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
            try
            {
                if ( getManagementHost().getHostId().equals( id ) )
                {
                    result = managementHost;
                }
            }
            catch ( HostNotFoundException e )
            {
                throw new HostNotFoundException( String.format( "Host by id %s is not registered.", id ) );
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
            try
            {
                if ( getManagementHost().getHostId().equals( host.getHostId() ) )
                {
                    result = getManagementHost();
                }
            }
            catch ( HostNotFoundException e )
            {
                throw new HostNotFoundException(
                        String.format( "Host by id %s is not registered.", host.getHostId() ) );
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
                containerHost.setState( ContainerState.RUNNING );
            }
        }
        catch ( ResourceHostException e )
        {
            containerHost.setState( ContainerState.UNKNOWN );
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
                containerHost.setState( ContainerState.STOPPED );
            }
        }
        catch ( ResourceHostException e )
        {
            containerHost.setState( ContainerState.UNKNOWN );
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
    }


    @Override
    public void destroyContainer( final ContainerHost containerHost ) throws PeerException
    {
        Host result = bindHost( containerHost.getId() );
        if ( result == null )
        {
            throw new PeerException( "Container Host not found." );
        }

        try
        {
            ResourceHost resourceHost = getResourceHostByName( containerHost.getAgent().getParentHostName() );
            resourceHost.destroyContainerHost( containerHost );
            resourceHost.removeContainerHost( result );
            peerDAO.saveInfo( SOURCE_RESOURCE_HOST, resourceHost.getId().toString(), resourceHost );
        }
        catch ( ResourceHostException e )
        {
            throw new PeerException( e.toString() );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
    }


    @Override
    public boolean isConnected( final Host host ) throws PeerException
    {
        try
        {
            Host h = bindHost( host.getId() );
            if ( isTimedOut( h.getLastHeartbeat(), HOST_INACTIVE_TIME ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        catch ( HostNotFoundException e )
        {
            return false;
        }
    }


    private boolean isTimedOut( long lastHeartbeat, long timeoutInMillis )
    {
        return ( System.currentTimeMillis() - lastHeartbeat ) > timeoutInMillis;
    }


    @Override
    public String getQuota( ContainerHost host, final QuotaEnum quota ) throws PeerException
    {
        try
        {
            Host c = bindHost( host.getHostId() );
            ContainerHostEntity containerHost = ( ContainerHostEntity ) c;
            ResourceHost resourceHost = containerHost.getParent();
            return quotaManager.getQuota( host.getHostname(), quota, resourceHost.getAgent() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e.toString() );
        }
    }


    @Override
    public void setQuota( ContainerHost host, final QuotaEnum quota, final String value ) throws PeerException
    {
        try
        {
            Host c = bindHost( host.getHostId() );
            ContainerHostEntity containerHost = ( ContainerHostEntity ) c;
            ResourceHost resourceHost = containerHost.getParent();
            quotaManager.setQuota( host.getHostname(), quota, value, resourceHost.getAgent() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e.toString() );
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


    @Override
    public List<String> getTemplates()
    {
        List<Template> templates = templateRegistry.getAllTemplates();

        List<String> result = new ArrayList<>();
        for ( Template template : templates )
        {
            result.add( template.getTemplateName() );
        }
        return result;
    }


    public void addResourceHost( final ResourceHost host )
    {
        if ( host == null )
        {
            throw new IllegalArgumentException( "Resource host could not be null." );
        }
        resourceHosts.add( host );
    }


    //    @Override
    //    public void onResponse( final Response response )
    //    {
    //        if ( response == null || response.getType() == null )
    //        {
    //            return;
    //        }
    //
    //        if ( response.getType().equals( ResponseType.REGISTRATION_REQUEST ) || response.getType().equals(
    //                ResponseType.HEARTBEAT_RESPONSE ) )
    //        {
    //            if ( response.getHostname().equals( "management" ) )
    //            {
    //                if ( managementHost == null )
    //                {
    //                    Agent agent = PeerUtils.buildAgent( response );
    //                    managementHost =
    //                            new ManagementHostEntity( agent, getId().toString() );//new ManagementHostImpl(
    // agent, getId() );
    //                    //                    managementHost.setParentAgent( NullAgent.getInstance() );
    //                    try
    //                    {
    //                        managementHost.init();
    //                        //                        peerDAO.saveInfo( SOURCE_MANAGEMENT_HOST, managementHost.getId()
    //                        // .toString(), managementHost );
    //
    //                        managementHostDataService.persist( ( ManagementHostEntity ) managementHost );
    //                    }
    //                    catch ( SubutaiInitException e )
    //                    {
    //                        LOG.error( e.toString() );
    //                    }
    //                }
    //                managementHost.updateHeartbeat();
    //                return;
    //            }
    //
    //            if ( response.getHostname().startsWith( "py" ) )
    //            {
    //                ResourceHost host;
    //                try
    //                {
    //                    host = getResourceHostByName( response.getHostname() );
    //                }
    //                catch ( PeerException e )
    //                {
    //                    host = new ResourceHost( PeerUtils.buildAgent( response ), getId() );
    //                    //                    host.setParentAgent( NullAgent.getInstance() );
    //                    addResourceHost( host );
    //                    peerDAO.saveInfo( SOURCE_RESOURCE_HOST, host.getId().toString(), host );
    //                }
    //                host.updateHeartbeat();
    //                return;
    //            }
    //
    //            try
    //            {
    //                SubutaiHost host = ( SubutaiHost ) bindHost( response.getUuid() );
    //                host.updateHeartbeat();
    //            }
    //            catch ( PeerException p )
    //            {
    //                LOG.warn( p.toString() );
    //            }
    //        }
    //    }


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
        Preconditions.checkNotNull( hashCode() );

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
    public void clean()
    {
        if ( managementHost != null && managementHost.getId() != null )
        {
            peerDAO.deleteInfo( SOURCE_MANAGEMENT_HOST, managementHost.getId().toString() );
            managementHost = null;
        }

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            peerDAO.deleteInfo( SOURCE_RESOURCE_HOST, resourceHost.getId().toString() );
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
    public <T, V> V sendRequest( final T request, final String recipient, final int timeout,
                                 final Class<V> responseType ) throws PeerException
    {
        Preconditions.checkNotNull( responseType, "Invalid response type" );

        return sendRequestInternal( request, recipient, timeout, responseType );
    }


    @Override
    public <T> void sendRequest( final T request, final String recipient, final int timeout ) throws PeerException
    {
        sendRequestInternal( request, recipient, timeout, null );
    }


    private <T, V> V sendRequestInternal( final T request, final String recipient, final int timeout,
                                          final Class<V> responseType ) throws PeerException
    {
        Preconditions.checkNotNull( request, "Invalid request" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipient ), "Invalid recipient" );
        Preconditions.checkArgument( timeout > 0, "Timeout must be greater than 0" );


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
        LOG.info( String.format( "Received heartbeat: %s", resourceHostInfo ) );
        if ( resourceHostInfo.getHostname().equals( "management" ) )
        {
            if ( managementHost == null )
            {
                managementHost = new ManagementHostEntity( getId().toString(), resourceHostInfo );
                try
                {
                    managementHost.init();
                    managementHostDataService.persist( ( ManagementHostEntity ) managementHost );
                }
                catch ( SubutaiInitException e )
                {
                    LOG.error( e.toString() );
                }
            }
            managementHost.updateHeartbeat();
            //            peerDAO.saveInfo( SOURCE_MANAGEMENT_HOST, managementHost.getId().toString(), managementHost );
            return;
        }
        else
        {
            ResourceHost host;
            try
            {
                host = getResourceHostByName( resourceHostInfo.getHostname() );
                if ( resourceHostInfo.getContainers() != null )
                {
                    boolean newContainer = false;
                    for ( ContainerHostInfo containerHostInfo : resourceHostInfo.getContainers() )
                    {
                        try
                        {
                            bindHost( containerHostInfo.getId() );
                        }
                        catch ( HostNotFoundException hnfe )
                        {
                            Host containerHost = new ContainerHostEntity( getId().toString(), containerHostInfo );
                            host.addContainerHost( ( ContainerHostEntity ) containerHost );
                            newContainer = true;
                        }
                    }
                    if ( newContainer )
                    {
                        resourceHostDataService.update( ( ResourceHostEntity ) host );
                    }
                }
                host.onHeartbeat( resourceHostInfo );
            }
            catch ( PeerException e )
            {
                host = new ResourceHostEntity( getId().toString(), resourceHostInfo );
                resourceHostDataService.persist( ( ResourceHostEntity ) host );
                addResourceHost( host );
            }
            host.updateHeartbeat();
            //            peerDAO.saveInfo( SOURCE_RESOURCE_HOST, host.getId().toString(), host );
            return;
        }
    }
}

