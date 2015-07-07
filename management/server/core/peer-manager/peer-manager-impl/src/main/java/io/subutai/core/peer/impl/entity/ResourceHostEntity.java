package io.subutai.core.peer.impl.entity;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.host.HostInfo;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.protocol.Disposable;
import io.subutai.common.protocol.Template;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.peer.api.ContainerState;
import io.subutai.core.peer.api.HostNotFoundException;
import io.subutai.core.peer.api.ResourceHost;
import io.subutai.core.peer.api.ResourceHostException;
import io.subutai.core.peer.impl.container.CreateContainerTask;
import io.subutai.core.peer.impl.container.DestroyContainerTask;

import io.subutai.core.registry.api.TemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * Resource host implementation.
 */
@Entity
@Table( name = "resource_host" )
@Access( AccessType.FIELD )
public class ResourceHostEntity extends AbstractSubutaiHost implements ResourceHost, Disposable
{
    private static final int CONNECT_TIMEOUT = 300;

    private static final Logger LOG = LoggerFactory.getLogger( ResourceHostEntity.class );
    private static final Pattern LXC_STATE_PATTERN = Pattern.compile( "State:(\\s*)(.*)" );
    private static final String PRECONDITION_CONTAINER_IS_NULL_MSG = "Container host is null";
    private static final String CONTAINER_EXCEPTION_MSG_FORMAT = "Container with name %s does not exist";

    @OneToMany( mappedBy = "parent", fetch = FetchType.EAGER,
            targetEntity = ContainerHostEntity.class )
    final Set<ContainerHost> containersHosts = Sets.newHashSet();

    @Transient
    protected ExecutorService singleThreadExecutorService = Executors.newSingleThreadExecutor();
    @Transient
    private Monitor monitor;
    @Transient
    protected CommandUtil commandUtil = new CommandUtil();
    @Transient
    protected TemplateRegistry registry;
    @Transient
    protected HostRegistry hostRegistry;


    protected ResourceHostEntity()
    {
    }


    public ResourceHostEntity( final String peerId, final HostInfo resourceHostInfo )
    {
        super( peerId, resourceHostInfo );
    }


    public <T> Future<T> queueSequentialTask( Callable<T> callable )
    {
        return singleThreadExecutorService.submit( callable );
    }


    public void dispose()
    {
        singleThreadExecutorService.shutdown();
    }


    @Override
    public ContainerState getContainerHostState( final ContainerHost containerHost ) throws ResourceHostException
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
                new RequestBuilder( String.format( "/usr/bin/lxc-info -n %s", containerHost.getHostname() ) )
                        .withTimeout( 30 );
        CommandResult result;
        try
        {
            result = commandUtil.execute( requestBuilder, this );
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( "Error fetching container state", e );
        }

        String stdOut = result.getStdOut();

        Matcher m = LXC_STATE_PATTERN.matcher( stdOut );
        if ( m.find() )
        {
            return ContainerState.valueOf( m.group( 2 ) );
        }
        else
        {
            return ContainerState.UNKNOWN;
        }
    }


    @Override
    public ResourceHostMetric getHostMetric() throws ResourceHostException
    {
        try
        {
            return monitor.getResourceHostMetric( this );
        }
        catch ( MonitorException e )
        {
            throw new ResourceHostException( "Error obtaining host metric", e );
        }
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
                new RequestBuilder( String.format( "/usr/bin/lxc-start -n %s -d", containerHost.getHostname() ) )
                        .withTimeout( 1 ).daemon();
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

        if ( !ContainerState.RUNNING.equals( getContainerHostState( containerHost ) ) )
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
                new RequestBuilder( String.format( "/usr/bin/lxc-stop -n %s", containerHost.getHostname() ) )
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
            }
        }
    }


    public ContainerHost getContainerHostById( final UUID id ) throws HostNotFoundException
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
    public ContainerHost createContainer( final String templateName, final String hostname, final String ip,
                                          final int vlan, final String gateway, final int timeout )
            throws ResourceHostException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid hostname" );
        Preconditions.checkArgument( timeout > 0, "Invalid timeout" );

        Template template = registry.getTemplate( templateName );
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
            LOG.warn( "Error getting container host by name: " + hostname, e );
            //ignore
        }

        Future<ContainerHost> containerHostFuture =
                queueSequentialTask( new CreateContainerTask( this, template, hostname, ip, vlan, gateway, timeout ) );

        try
        {
            return containerHostFuture.get();
        }
        catch ( ExecutionException | InterruptedException e )
        {
            throw new ResourceHostException( "Error creating container", e );
        }
    }


    @Override
    public ContainerHost createContainer( final String templateName, final String hostname, final int timeout )
            throws ResourceHostException
    {
        return createContainer( templateName, hostname, null, 0, null, timeout );
    }


    public void setRegistry( final TemplateRegistry registry )
    {
        this.registry = registry;
    }


    public void setHostRegistry( final HostRegistry hostRegistry )
    {
        this.hostRegistry = hostRegistry;
    }


    public void setMonitor( final Monitor monitor )
    {
        this.monitor = monitor;
    }


    public void addContainerHost( ContainerHost host )
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        ( ( ContainerHostEntity ) host ).setParent( this );

        synchronized ( containersHosts )
        {
            //replace host
            containersHosts.remove( host );
            containersHosts.add( host );
        }
    }
}
