package io.subutai.core.localpeer.impl.entity;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.InstanceType;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.Disposable;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.settings.Common;
import io.subutai.common.util.NumUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.localpeer.impl.container.CreateContainerTask;
import io.subutai.core.localpeer.impl.container.DestroyContainerTask;
import io.subutai.core.localpeer.impl.tasks.SetupTunnelsTask;
import io.subutai.core.network.api.NetworkManager;


/**
 * Resource host implementation.
 */
@Entity
@Table( name = "r_host" )
@Access( AccessType.FIELD )
public class ResourceHostEntity extends AbstractSubutaiHost implements ResourceHost, Disposable
{
    private static final int CONNECT_TIMEOUT = 300;

    private static final Logger LOG = LoggerFactory.getLogger( ResourceHostEntity.class );
    private static final Pattern LXC_STATE_PATTERN = Pattern.compile( "(RUNNING|STOPPED|FROZEN)" );
    private static final String PRECONDITION_CONTAINER_IS_NULL_MSG = "Container host is null";
    private static final String CONTAINER_EXCEPTION_MSG_FORMAT = "Container with name %s does not exist";

    @OneToMany( mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER,
            targetEntity = ContainerHostEntity.class, orphanRemoval = true )
    private Set<ContainerHost> containersHosts = Sets.newHashSet();

    @Column( name = "instance" )
    @Enumerated( EnumType.STRING )
    private InstanceType instanceType;

    @OneToMany( mappedBy = "host", fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity =
            HostInterfaceEntity.class, orphanRemoval = true )
    @JsonIgnore
    protected Set<HostInterface> netInterfaces = new HashSet<>();

    @Transient
    protected ExecutorService singleThreadExecutorService;


    @Transient
    protected CommandUtil commandUtil = new CommandUtil();

    @Transient
    protected TemplateManager registry;

    @Transient
    protected HostRegistry hostRegistry;

    @Transient
    protected int numberOfCpuCores = -1;


    protected ResourceHostEntity()
    {
        init();
    }


    @Override
    public void init()
    {
        super.init();

        if ( singleThreadExecutorService == null )
        {
            singleThreadExecutorService = Executors.newSingleThreadExecutor();
        }
    }


    public ResourceHostEntity( final String peerId, final ResourceHostInfo resourceHostInfo )
    {
        super( peerId, resourceHostInfo );

        this.instanceType = resourceHostInfo.getInstanceType();

        setNetInterfaces( resourceHostInfo.getHostInterfaces() );

        init();
    }


    @Override
    public Set<HostInterface> getNetInterfaces()
    {
        return netInterfaces;
    }


    public void setNetInterfaces( HostInterfaces hostInterfaces )
    {
        Preconditions.checkNotNull( hostInterfaces );

        this.netInterfaces.clear();
        for ( HostInterface iface : hostInterfaces.getAll() )
        {
            HostInterfaceEntity netInterface = new HostInterfaceEntity( iface );
            netInterface.setHost( this );
            this.netInterfaces.add( netInterface );
        }
    }


    @Override
    public InstanceType getInstanceType()
    {
        return instanceType;
    }


    @Override
    public Set<ContainerHostInfo> getContainers()
    {
        try
        {
            return hostRegistry.getResourceHostInfoById( getId() ).getContainers();
        }
        catch ( HostDisconnectedException e )
        {
            LOG.warn( "Error in getContainers", e );
        }

        return Sets.newHashSet();
    }


    public <T> Future<T> queueSequentialTask( Callable<T> callable )
    {
        return singleThreadExecutorService.submit( callable );
    }


    public void dispose()
    {
        singleThreadExecutorService.shutdown();
    }


    public void setHostname( String hostname )
    {
        Preconditions.checkNotNull( hostname );
        this.hostname = hostname;
    }


    public void setupTunnels( P2pIps p2pIps, NetworkResource networkResource ) throws ResourceHostException
    {
        Preconditions.checkNotNull( p2pIps, "Invalid peer ips set" );
        Preconditions.checkNotNull( networkResource, "Invalid networkResource" );

        //need to execute sequentially since other parallel executions can setup the same tunnel
        Future<Boolean> future =
                queueSequentialTask( new SetupTunnelsTask( getNetworkManager(), this, p2pIps, networkResource ) );

        try
        {
            future.get();
        }
        catch ( Exception e )
        {
            throw new ResourceHostException( "Error setting up tunnels", e.getCause() );
        }
    }


    protected NetworkManager getNetworkManager() throws ResourceHostException
    {
        try
        {
            return ServiceLocator.getServiceNoCache( NetworkManager.class );
        }
        catch ( NamingException e )
        {
            throw new ResourceHostException( e );
        }
    }


    @Override
    public ContainerHostState getContainerHostState( final ContainerHost containerHost ) throws ResourceHostException
    {
        Preconditions.checkNotNull( containerHost, PRECONDITION_CONTAINER_IS_NULL_MSG );

        try
        {
            getContainerHostById( containerHost.getId() );
        }
        catch ( HostNotFoundException e )
        {
            throw new ResourceHostException(
                    String.format( CONTAINER_EXCEPTION_MSG_FORMAT, containerHost.getHostname() ), e );
        }


        RequestBuilder requestBuilder =
                new RequestBuilder( String.format( "subutai list -i %s", containerHost.getHostname() ) );
        CommandResult result;
        try
        {
            result = commandUtil.execute( requestBuilder, this );
        }
        catch ( CommandException e )
        {
            LOG.error( e.getMessage(), e );
            throw new ResourceHostException( "Error fetching container state", e );
        }

        String stdOut = result.getStdOut();

        String[] outputLines = stdOut.split( System.lineSeparator() );
        if ( outputLines.length == 3 )
        {
            Matcher m = LXC_STATE_PATTERN.matcher( outputLines[2] );
            if ( m.find() )
            {
                return ContainerHostState.valueOf( m.group( 1 ) );
            }
        }

        return ContainerHostState.UNKNOWN;
    }


    public Set<ContainerHost> getContainerHosts()
    {
        synchronized ( containersHosts )
        {
            return Sets.newConcurrentHashSet( containersHosts );
        }
    }


    public void startContainerHost( final ContainerHost containerHost ) throws ResourceHostException
    {
        Preconditions.checkNotNull( containerHost, PRECONDITION_CONTAINER_IS_NULL_MSG );

        try
        {
            getContainerHostById( containerHost.getId() );
        }
        catch ( HostNotFoundException e )
        {
            throw new ResourceHostException(
                    String.format( CONTAINER_EXCEPTION_MSG_FORMAT, containerHost.getHostname() ), e );
        }

        RequestBuilder requestBuilder =
                new RequestBuilder( String.format( "subutai start %s", containerHost.getHostname() ) ).withTimeout( 1 )
                                                                                                      .daemon();
        try
        {
            commandUtil.execute( requestBuilder, this );
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( "Error on starting container", e );
        }

        poolContainerAvailability( containerHost );
    }


    private void poolContainerAvailability( final ContainerHost containerHost ) throws ResourceHostException
    {
        //wait container connection
        long ts = System.currentTimeMillis();
        while ( System.currentTimeMillis() - ts < CONNECT_TIMEOUT * 1000 && !containerHost.isConnected() )
        {
            try
            {
                Thread.sleep( 100 );
            }
            catch ( InterruptedException e )
            {
                throw new ResourceHostException( e );
            }
        }

        if ( !ContainerHostState.RUNNING.equals( getContainerHostState( containerHost ) ) )
        {
            throw new ResourceHostException(
                    String.format( "Error starting container %s", containerHost.getHostname() ) );
        }
    }


    public void stopContainerHost( final ContainerHost containerHost ) throws ResourceHostException
    {
        Preconditions.checkNotNull( containerHost, PRECONDITION_CONTAINER_IS_NULL_MSG );

        try
        {
            getContainerHostById( containerHost.getId() );
        }
        catch ( HostNotFoundException e )
        {
            throw new ResourceHostException(
                    String.format( CONTAINER_EXCEPTION_MSG_FORMAT, containerHost.getHostname() ), e );
        }

        RequestBuilder requestBuilder =
                new RequestBuilder( String.format( "subutai stop %s", containerHost.getHostname() ) )
                        .withTimeout( 120 );
        try
        {
            commandUtil.execute( requestBuilder, this );
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( "Error stopping container", e );
        }
    }


    @Override
    public void destroyContainerHost( final ContainerHost containerHost ) throws ResourceHostException
    {

        Preconditions.checkNotNull( containerHost, PRECONDITION_CONTAINER_IS_NULL_MSG );

        try
        {
            getContainerHostById( containerHost.getId() );
        }
        catch ( HostNotFoundException e )
        {
            throw new ResourceHostException(
                    String.format( CONTAINER_EXCEPTION_MSG_FORMAT, containerHost.getHostname() ), e );
        }

        Future future = queueSequentialTask( new DestroyContainerTask( this, containerHost.getHostname() ) );

        try
        {
            future.get();
        }
        catch ( ExecutionException | InterruptedException e )
        {
            throw new ResourceHostException( "Error destroying container", e );
        }

        removeContainerHost( containerHost );
    }


    @Override
    public ContainerHost getContainerHostByName( final String hostname ) throws HostNotFoundException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid hostname" );

        for ( ContainerHost containerHost : getContainerHosts() )
        {

            if ( containerHost.getHostname().equalsIgnoreCase( hostname ) )
            {
                return containerHost;
            }
        }

        throw new HostNotFoundException( String.format( "Container host not found by hostname %s", hostname ) );
    }


    public void removeContainerHost( final ContainerHost containerHost )
    {
        Preconditions.checkNotNull( containerHost, PRECONDITION_CONTAINER_IS_NULL_MSG );

        if ( getContainerHosts().contains( containerHost ) )
        {
            synchronized ( containersHosts )
            {
                containersHosts.remove( containerHost );
                ( ( ContainerHostEntity ) containerHost ).setParent( null );
            }
        }
    }


    public ContainerHost getContainerHostById( final String id ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( id, "Invalid container id" );

        for ( ContainerHost containerHost : getContainerHosts() )
        {
            if ( containerHost.getId().equals( id ) )
            {
                return containerHost;
            }
        }

        throw new HostNotFoundException( String.format( "Container host not found by id %s", id ) );
    }


    @Override
    public Set<ContainerHost> getContainerHostsByEnvironmentId( final String environmentId )
    {
        Set<ContainerHost> result = new HashSet<>();
        for ( ContainerHost containerHost : getContainerHosts() )
        {
            if ( environmentId.equals( containerHost.getEnvironmentId().getId() ) )
            {
                result.add( containerHost );
            }
        }
        return result;
    }


    @Override
    public Set<ContainerHost> getContainerHostsByOwnerId( final String ownerId )
    {
        Set<ContainerHost> result = new HashSet<>();
        for ( ContainerHost containerHost : getContainerHosts() )
        {
            if ( ownerId.equals( containerHost.getOwnerId() ) )
            {
                result.add( containerHost );
            }
        }
        return result;
    }


    @Override
    public Set<ContainerHost> getContainerHostsByPeerId( final String peerId )
    {
        Set<ContainerHost> result = new HashSet<>();
        for ( ContainerHost containerHost : getContainerHosts() )
        {
            if ( peerId.equals( containerHost.getInitiatorPeerId() ) )
            {
                result.add( containerHost );
            }
        }
        return result;
    }


    @Override
    public ContainerHostInfo createContainer( final String templateName, final String hostname,
                                              final ContainerQuota quota, final String ip, final int vlan,
                                              final int timeout, final String environmentId )
            throws ResourceHostException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid hostname" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ), "Invalid ip" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions.checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ),
                "Invalid vlan id" );
        Preconditions.checkArgument( timeout > 0, "Invalid timeout" );

        TemplateKurjun template = registry.getTemplate( templateName );
        if ( template == null )
        {
            throw new ResourceHostException( String.format( "Template %s is not registered", templateName ) );
        }

        try
        {
            getContainerHostByName( hostname );
            throw new ResourceHostException( String.format( "Container with name %s already exists", hostname ) );
        }
        catch ( HostNotFoundException e )
        {
            //ignore
            LOG.info( String.format( "Container host '%s' does not exists, creating new one.", hostname ) );
        }

        Future<ContainerHostInfo> containerHostFuture = queueSequentialTask(
                new CreateContainerTask( hostRegistry, this, template, hostname, quota, ip, vlan, timeout,
                        environmentId ) );

        try
        {
            final ContainerHostInfo result = containerHostFuture.get();
            return result;
        }
        catch ( ExecutionException | InterruptedException e )
        {
            throw new ResourceHostException( "Error creating container", e );
        }
    }


    public void setRegistry( final TemplateManager registry )
    {
        this.registry = registry;
    }


    public void setHostRegistry( final HostRegistry hostRegistry )
    {
        this.hostRegistry = hostRegistry;
    }


    @Override
    public void addContainerHost( ContainerHost host )
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) host;
        containerHostEntity.setParent( this );

        synchronized ( containersHosts )
        {
            containersHosts.add( host );
        }
    }


    @Override
    public void cleanup( final EnvironmentId environmentId, final int vlan ) throws ResourceHostException
    {
        try
        {
            commandUtil.execute( new RequestBuilder( String.format( "subutai cleanup %d", vlan ) ), this );
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( String.format( "Could not cleanup resource host '%s'.", hostname ) );
        }

        Set<ContainerHost> containerHosts = getContainerHostsByEnvironmentId( environmentId.getId() );
        if ( containerHosts.size() > 0 )
        {
            for ( ContainerHost containerHost : containerHosts )
            {
                removeContainerHost( containerHost );
            }
        }
    }


    @Override
    public int getNumberOfCpuCores() throws ResourceHostException
    {
        if ( numberOfCpuCores == -1 )
        {
            try
            {
                CommandResult commandResult = commandUtil.execute( new RequestBuilder( "nproc" ), this );

                numberOfCpuCores = Integer.parseInt( commandResult.getStdOut().trim() );
            }
            catch ( Exception e )
            {
                throw new ResourceHostException( "Error fetching # of cpu cores", e );
            }
        }

        return numberOfCpuCores;
    }


    @Override
    public boolean updateHostInfo( final HostInfo hostInfo )
    {
        super.updateHostInfo( hostInfo );

        setNetInterfaces( hostInfo.getHostInterfaces() );

        ResourceHostInfo resourceHostInfo = ( ResourceHostInfo ) hostInfo;

        for ( ContainerHostInfo info : resourceHostInfo.getContainers() )
        {
            ContainerHostEntity containerHost;
            try
            {
                containerHost = ( ContainerHostEntity ) getContainerHostByName( info.getHostname() );
                containerHost.updateHostInfo( info );
            }
            catch ( HostNotFoundException e )
            {
                if ( "management".equals( info.getHostname() ) )
                {
                    containerHost = new ContainerHostEntity( peerId, info.getId(), info.getHostname(), info.getArch(),
                            info.getHostInterfaces(), info.getHostname(), "management", info.getArch().name(),
                            "management", null, null, ContainerSize.SMALL, info.getState() );
                    addContainerHost( containerHost );
                }
                else
                {
                    LOG.warn( String.format( "Found not registered container host: %s %s", info.getId(),
                            info.getHostname() ) );
                }
            }
        }

        //remove containers that are missing in heartbeat
        for ( ContainerHost containerHost : getContainerHosts() )
        {
            boolean found = false;
            for ( ContainerHostInfo info : resourceHostInfo.getContainers() )
            {
                if ( info.getId().equals( containerHost.getId() ) )
                {
                    found = true;
                    break;
                }
            }
            if ( !found )
            {
                removeContainerHost( containerHost );
            }
        }

        return true;
    }


    @Override
    public boolean isConnected()
    {
        return getPeer().isConnected( new HostId( getId() ) );
    }
}
