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
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.executor.api.CommandExecutor;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostState;
import org.safehaus.subutai.core.hostregistry.api.HostListener;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;
import org.safehaus.subutai.core.lxc.quota.api.QuotaException;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.peer.api.CloneParam;
import org.safehaus.subutai.core.peer.api.CommandUtil;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.HostEvent;
import org.safehaus.subutai.core.peer.api.HostEventListener;
import org.safehaus.subutai.core.peer.api.HostInfoModel;
import org.safehaus.subutai.core.peer.api.HostKey;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.HostTask;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerInfo;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.api.ResourceHostException;
import org.safehaus.subutai.core.peer.api.task.Task;
import org.safehaus.subutai.core.peer.api.task.clone.CloneTask;
import org.safehaus.subutai.core.peer.impl.dao.ContainerHostDataService;
import org.safehaus.subutai.core.peer.impl.dao.ManagementHostDataService;
import org.safehaus.subutai.core.peer.impl.dao.PeerDAO;
import org.safehaus.subutai.core.peer.impl.dao.ResourceHostDataService;
import org.safehaus.subutai.core.peer.impl.entity.ContainerHostEntity;
import org.safehaus.subutai.core.peer.impl.entity.ManagementHostEntity;
import org.safehaus.subutai.core.peer.impl.entity.ResourceHostEntity;
import org.safehaus.subutai.core.peer.impl.task.CloneTaskImpl;
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
public class LocalPeerImpl implements LocalPeer, HostListener, HostEventListener
{
    private static final Logger LOG = LoggerFactory.getLogger( LocalPeerImpl.class );
    private static final String TEMPLATE_DOWNLOAD_DIR = "/downloaded-subutai-templates";

    private static final long HOST_INACTIVE_TIME = 5 * 1000 * 60; // 5 min
    private static final int MAX_LXC_NAME = 15;
    private PeerManager peerManager;
    private TemplateRegistry templateRegistry;
    private PeerDAO peerDAO;
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
    private HostRegistry hostRegistry;
    private Set<RequestListener> requestListeners;
    private List<HostTask> tasks = Lists.newCopyOnWriteArrayList();
    private CommandUtil commandUtil;
    private Commands commands;


    public LocalPeerImpl( PeerManager peerManager, TemplateRegistry templateRegistry, PeerDAO peerDao,
                          QuotaManager quotaManager, StrategyManager strategyManager,
                          Set<RequestListener> requestListeners, CommandExecutor commandExecutor,
                          HostRegistry hostRegistry, Monitor monitor )

    {
        this.strategyManager = strategyManager;
        this.peerManager = peerManager;
        this.templateRegistry = templateRegistry;
        this.peerDAO = peerDao;
        this.quotaManager = quotaManager;
        this.monitor = monitor;
        this.requestListeners = requestListeners;
        this.commandExecutor = commandExecutor;
        this.hostRegistry = hostRegistry;
        this.commandUtil = new CommandUtil();
        this.commands = new Commands();
    }


    @Override
    public void init()
    {
        managementHostDataService = new ManagementHostDataService( peerManager.getEntityManagerFactory() );
        Collection allManagementHostEntity = managementHostDataService.getAll();
        if ( allManagementHostEntity != null && allManagementHostEntity.size() > 0 )
        {
            managementHost = ( ManagementHost ) allManagementHostEntity.iterator().next();
            managementHost.addListener( this );
            managementHost.setPeer( this );
            managementHost.init();
        }

        resourceHostDataService = new ResourceHostDataService( peerManager.getEntityManagerFactory() );
        resourceHosts = Sets.newHashSet();
        resourceHosts.addAll( resourceHostDataService.getAll() );

        containerHostDataService = new ContainerHostDataService( peerManager.getEntityManagerFactory() );

        for ( ResourceHost resourceHost : resourceHosts )
        {
            resourceHost.addListener( this );
            resourceHost.setPeer( this );
            for ( ContainerHost containerHost : ( resourceHost ).getContainerHosts() )
            {
                containerHost.setPeer( this );
                containerHost.setDataService( containerHostDataService );
            }
        }


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


    public void submit( Task task )
    {
        checkSecurity( task );
        accept( task );
    }


    private void runTasks( final Task task )
    {

    }


    private void accept( final Task task )
    {

    }


    private void checkSecurity( Task task )
    {

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
    public List<HostTask> getTasks()
    {
        return tasks;
    }


    @Override
    public ContainerHost createContainer( final String hostName, final String templateName, final String cloneName,
                                          final UUID environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( hostName, "Host name is null." );
        Preconditions.checkNotNull( environmentId, "Environment ID is null." );
        Preconditions.checkNotNull( templateName, "Template list is null." );

        CloneParam cloneParam = new CloneParam( cloneName, Lists.newArrayList( getTemplate( templateName ) ) );
        ResourceHost resourceHost = getResourceHostByName( hostName );


        UUID taskGroupId = UUID.randomUUID();
        HostCloneTask hostCloneTask = new HostCloneTask( taskGroupId, resourceHost, cloneParam );
        tasks.add( hostCloneTask );
        hostCloneTask.start();

        try
        {
            return waitCloneTasks( Lists.newArrayList( hostCloneTask ) ).iterator().next();
        }
        catch ( Exception e )
        {
            LOG.error( "Clone fail", e );
            throw new PeerException( "Clone fail.", e.toString() );
        }
    }


    private Set<ContainerHost> waitCloneTasks( final List<HostCloneTask> hostCloneTasks ) throws Exception
    {
        int quantity = hostCloneTasks.size();
        long threshold = System.currentTimeMillis() + 180 * quantity * 1000;
        DateFormat formatter = new SimpleDateFormat( "HH:mm:ss" );
        formatter.setTimeZone( TimeZone.getTimeZone( "GMT" ) );

        int i = 0;
        while ( i < quantity && threshold - System.currentTimeMillis() > 0 )
        {
            i = 0;
            for ( HostCloneTask hostCloneTask : hostCloneTasks )
            {
                LOG.info( String.format( "Clone task %s  %s:%s", hostCloneTask.getPhase(),
                        hostCloneTask.getHost().getHostname(), hostCloneTask.getParameter().getHostname() ) );
                if ( hostCloneTask.getPhase() == HostTask.Phase.DONE )
                {
                    if ( hostCloneTask.getResult().isOk() )
                    {
                        i++;
                    }
                    else
                    {
                        LOG.error( "Container clone error.", hostCloneTask.getException() );
                        throw hostCloneTask.getException();
                    }
                }
            }

            try
            {
                Thread.sleep( 5000 );
            }
            catch ( InterruptedException ignore )
            {
            }
            LOG.info( String.format( "Waiting clone tasks. Timeout: %s ",
                    formatter.format( new Date( threshold - System.currentTimeMillis() ) ) ) );
        }
        Set<ContainerHost> result = new HashSet<>();
        for ( HostCloneTask hostCloneTask : hostCloneTasks )
        {
            result.add( hostCloneTask.getResult().getValue() );
        }
        return result;
    }


    public boolean schedule( Task task )
    {
        CloneTaskImpl cloneTask = new CloneTaskImpl( ( CloneTask ) task );
        cloneTask.run();
        return true;
    }


    /**
     * Imports remote templates without registering them with template registry.
     *
     * After this call all imported templates can be cloned
     *
     * @param sourcePeer - peer from which to import templates
     * @param templates - templates to import
     * @param templateDownloadToken - template download token
     * @param resourceHosts - resource hosts on which to import template
     */
    protected void importTemplates( Peer sourcePeer, Set<Template> templates, String templateDownloadToken,
                                    Set<ResourceHost> resourceHosts ) throws PeerException
    {
        Preconditions.checkNotNull( sourcePeer );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( templates ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateDownloadToken ) );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( resourceHosts ) );

        //import only remote templates, otherwise no-op
        if ( !sourcePeer.isLocal() )
        {
            //import templates one by one
            for ( Template template : templates )
            {
                //import each template's ancestry lineage
                importTemplateLineage( sourcePeer, template, templateDownloadToken, resourceHosts );
            }
        }
    }


    /**
     * TODO make sure resource hosts can access management host via SSH without password
     *
     * Imports a template's whole ancestry lineage including the template itself from remote peer without registering
     * with template registry.
     *
     * After this call an imported template can be cloned
     *
     * @param sourcePeer - peer from which to import ancestry lineage
     * @param template - template whose ancestry lineage to import
     */
    private void importTemplateLineage( Peer sourcePeer, Template template, String templateDownloadToken,
                                        Set<ResourceHost> resourceHosts ) throws PeerException
    {
        //construct template lineage
        List<Template> templateLineage = Lists.newArrayList();

        Template parentTemplate = template;
        while ( parentTemplate != null && !Common.MASTER_TEMPLATE_NAME
                .equalsIgnoreCase( parentTemplate.getTemplateName() ) )
        {
            //add parent template to the beginning of collection to maintain correct order of import
            templateLineage.add( 0, parentTemplate );

            //obtain parent template metadata from source peer
            parentTemplate = sourcePeer.getTemplate( parentTemplate.getParentTemplateName() );
        }


        //downloaded templates if needed
        for ( Template remoteTemplate : templateLineage )
        {
            try
            {
                //check if template is already downloaded
                CommandResult result = managementHost.execute(
                        commands.getCheckTemplateDownloadedCommand( TEMPLATE_DOWNLOAD_DIR,
                                remoteTemplate.getFileName() ) );

                //template is not downloaded -> download it
                if ( result.getExitCode() == 2 )
                {
                    //download target template
                    commandUtil.execute( commands.getDownloadTemplateCommand( sourcePeer.getPeerInfo().getIp(),
                            sourcePeer.getPeerInfo().getPort(), remoteTemplate.getTemplateName(), templateDownloadToken,
                            TEMPLATE_DOWNLOAD_DIR ), managementHost );
                }
            }
            catch ( CommandException e )
            {
                throw new PeerException( e );
            }
        }

        //import templates on resource hosts
        for ( ResourceHost resourceHost : resourceHosts )
        {
            for ( Template remoteTemplate : templateLineage )
            {
                try
                {
                    CommandResult result = resourceHost
                            .execute( commands.getCheckTemplateImportedCommand( remoteTemplate.getTemplateName() ) );

                    //template is not imported -> import it
                    if ( result.getExitCode() == 1 )
                    {
                        //copy template from management host
                        commandUtil.execute( commands.getCopyTemplateFromManagementHostCommand( TEMPLATE_DOWNLOAD_DIR,
                                remoteTemplate.getFileName() ), resourceHost );

                        //import template
                        commandUtil.execute( commands.getImportTemplateCommand( remoteTemplate.getTemplateName() ),
                                resourceHost );
                    }
                }
                catch ( CommandException e )
                {
                    throw new PeerException( e );
                }
            }
        }
    }


    private String getTempDirPath()
    {
        return System.getProperty( "java.io.tmpdir" );
    }


    @Override
    public Set<HostInfoModel> scheduleCloneContainers( final UUID creatorPeerId, final List<Template> templates,
                                                       final int quantity, final String strategyId,
                                                       final List<Criteria> criteria ) throws PeerException
    {
        Preconditions.checkNotNull( creatorPeerId, "Creator peer ID is null." );
        Preconditions.checkNotNull( templates, "Template list is null." );
        Preconditions.checkState( templates.size() > 0, "Template list is empty" );

        UUID parentTaskId = UUID.randomUUID();
        LOG.info( String.format( "=============> Order received: %d %s", quantity, creatorPeerId.toString() ) );

        Set<HostInfoModel> result = new HashSet<>();
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

            List<HostCloneTask> hostCloneTasks = new ArrayList<>();
            Map<ResourceHost, List<CloneParam>> orders = new HashMap<>();

            for ( final Map.Entry<ResourceHost, Set<String>> e : cloneNames.entrySet() )
            {
                ResourceHost rh = e.getKey();
                Set<String> clones = e.getValue();
                ResourceHost resourceHost = getResourceHostByName( rh.getHostname() );
                List<CloneParam> cloneParams = new ArrayList<>();
                for ( String cloneName : clones )
                {
                    CloneParam cloneParam = new CloneParam( cloneName, templates );
                    cloneParams.add( cloneParam );
                    HostCloneTask hostCloneTask = new HostCloneTask( parentTaskId, resourceHost, cloneParam );
                    hostCloneTask.start();
                    hostCloneTasks.add( hostCloneTask );
                }
                orders.put( rh, cloneParams );
            }
            tasks.addAll( hostCloneTasks );

            Set<ContainerHost> containerHosts = waitCloneTasks( hostCloneTasks );
            for ( ContainerHost containerHost : containerHosts )
            {
                containerHost.setPeer( this );
                containerHost.setDataService( containerHostDataService );
                result.add( new HostInfoModel( containerHost ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Clone fail.", e );
            throw new PeerException( e.toString() );
        }
        return result;
    }


    @Override
    public ContainerHost getContainerHostImpl( final HostKey hostKey )
    {
        Host host = null;

        if ( getId().toString().equals( hostKey.getCreatorId() ) )
        {
            try
            {
                host = bindHost( hostKey.getHostId() );
            }
            catch ( HostNotFoundException ignore )
            {

            }
        }
        if ( host == null )
        {
            //TODO: implement remote ContainerHostImpl if needs
            host = new ContainerHostEntity( hostKey );
        }
        return ( ContainerHost ) host;
    }


    @Override
    public void onHostEvent( final HostEvent hostEvent )
    {
        LOG.info( String.format( "HostEvent received: %s %s", hostEvent.getType(), hostEvent.getObject() ) );
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
            ResourceHost resourceHost =
                    entity.getParent(); //getResourceHostByName( containerHost.getAgent().getParentHostName() );
            resourceHost.destroyContainerHost( containerHost );
            containerHostDataService.remove( containerHost.getHostId() );
            entity.getParent().removeContainerHost( entity );
            //            peerDAO.saveInfo( SOURCE_RESOURCE_HOST, resourceHost.getId().toString(), resourceHost );
        }
        catch ( ResourceHostException e )
        {
            String errMsg = String.format( "Could not destroy container [%s]", containerHost.getHostname() );
            LOG.error( errMsg, e );
            throw new PeerException( errMsg, e.toString() );
        }
    }


    @Override
    public boolean isConnected( final Host host )
    {
        try
        {
            Host h = bindHost( host.getId() );

            if ( h instanceof ContainerHost )
            {
                return ContainerHostState.RUNNING.equals( ( ( ContainerHost ) h ).getState() );
            }

            return !isTimedOut( h.getLastHeartbeat(), HOST_INACTIVE_TIME );
        }
        catch ( PeerException e )
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
    public ProcessResourceUsage getProcessResourceUsage( final ContainerHost host, final int processPid )
            throws PeerException
    {
        try
        {
            Host c = bindHost( host.getHostId() );
            return monitor.getProcessResourceUsage( ( ContainerHost ) c, processPid );
        }
        catch ( MonitorException e )
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


    @Override
    public void clean()
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
            }
            resourceHost.getContainerHosts().clear();
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
        LOG.info( String.format( "Received heartbeat: %s", resourceHostInfo ) );
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
                managementHost.addListener( this );
                managementHost.setPeer( this );
            }
            managementHost.updateHostInfo( resourceHostInfo );
        }
        else
        {
            ResourceHost host;
            try
            {
                host = getResourceHostByName( resourceHostInfo.getHostname() );
                if ( !resourceHostInfo.getContainers().isEmpty() )
                {
                    for ( ContainerHostInfo containerHostInfo : resourceHostInfo.getContainers() )
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
                            ( ( ContainerHostEntity ) containerHost ).setDataService( containerHostDataService );
                            containerHost.setPeer( this );
                            host.addContainerHost( ( ContainerHostEntity ) containerHost );
                            containerHostDataService.persist( ( ContainerHostEntity ) containerHost );
                        }
                        containerHost.updateHostInfo( containerHostInfo );
                    }
                }
            }
            catch ( HostNotFoundException e )
            {
                host = new ResourceHostEntity( getId().toString(), resourceHostInfo );
                host.init();
                resourceHostDataService.persist( ( ResourceHostEntity ) host );
                addResourceHost( host );
                host.addListener( this );
                host.setPeer( this );
            }
            host.updateHostInfo( resourceHostInfo );
        }
    }
}

