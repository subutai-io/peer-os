package org.safehaus.subutai.core.peer.impl.entity;


import java.util.List;
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

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.metric.ResourceHostMetric;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.peer.api.ContainerState;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.api.ResourceHostException;
import org.safehaus.subutai.core.peer.impl.container.CreateContainerTask;
import org.safehaus.subutai.core.peer.impl.container.DestroyContainerTask;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * Resource host implementation.
 */
@Entity
@Table( name = "resource_host" )
@Access( AccessType.FIELD )
public class ResourceHostEntity extends AbstractSubutaiHost implements ResourceHost
{
    private static final int CONNECT_TIMEOUT = 300;

    protected static final Logger LOG = LoggerFactory.getLogger( ResourceHostEntity.class );
    private static final Pattern LXC_STATE_PATTERN = Pattern.compile( "State:(\\s*)(.*)" );
    private static final int TEMPLATE_IMPORT_TIMEOUT_SEC = 10 * 60 * 60;

    @OneToMany( mappedBy = "parent", fetch = FetchType.EAGER,
            targetEntity = ContainerHostEntity.class )
    Set<ContainerHost> containersHosts = Sets.newHashSet();

    @Transient
    private ExecutorService singleThreadExecutorService = Executors.newSingleThreadExecutor();
    @Transient
    private Monitor monitor;
    @Transient
    CommandUtil commandUtil = new CommandUtil();
    @Transient
    TemplateRegistry registry;
    @Transient
    HostRegistry hostRegistry;


    public ResourceHostEntity()
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


    @Override
    public ContainerState getContainerHostState( final ContainerHost containerHost ) throws ResourceHostException
    {
        Preconditions.checkNotNull( containerHost, "Container host is null" );

        try
        {
            getContainerHostById( containerHost.getId() );
        }
        catch ( HostNotFoundException e )
        {
            throw new ResourceHostException(
                    String.format( "Container with name %s does not exist", containerHost.getHostname() ) );
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
        Preconditions.checkNotNull( containerHost, "Container host is null" );

        try
        {
            getContainerHostById( containerHost.getId() );
        }
        catch ( HostNotFoundException e )
        {
            throw new ResourceHostException(
                    String.format( "Container with name %s does not exist", containerHost.getHostname() ) );
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
        Preconditions.checkNotNull( containerHost, "Container host is null" );

        try
        {
            getContainerHostById( containerHost.getId() );
        }
        catch ( HostNotFoundException e )
        {
            throw new ResourceHostException(
                    String.format( "Container with name %s does not exist", containerHost.getHostname() ) );
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

        Preconditions.checkNotNull( containerHost, "Container host is null" );

        try
        {
            getContainerHostById( containerHost.getId() );
        }
        catch ( HostNotFoundException e )
        {
            throw new ResourceHostException(
                    String.format( "Container with name %s does not exist", containerHost.getHostname() ) );
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
        Preconditions.checkNotNull( containerHost, "Container host is null" );

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

        if ( registry.getTemplate( templateName ) == null )
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
        }

        Future<ContainerHost> containerHostFuture = queueSequentialTask(
                new CreateContainerTask( this, templateName, hostname, ip, vlan, gateway, timeout ) );

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


    @Override
    public void prepareTemplates( List<Template> templates ) throws ResourceHostException
    {

        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( templates ), "Invalid template set" );

        LOG.debug( String.format( "Preparing templates on %s...", hostname ) );

        for ( Template p : templates )
        {
            prepareTemplate( p );
        }
        LOG.debug( "Template successfully prepared." );
    }


    @Override
    public void prepareTemplate( final Template template ) throws ResourceHostException
    {
        Preconditions.checkNotNull( template, "Invalid template" );

        if ( templateExists( template ) )
        {
            return;
        }
        importTemplate( template );
        if ( templateExists( template ) )
        {
            return;
        }
        // trying add repository
        updateRepository( template );
        importTemplate( template );
        if ( !templateExists( template ) )
        {
            LOG.debug( String.format( "Could not prepare template %s on %s.", template.getTemplateName(), hostname ) );
            throw new ResourceHostException(
                    String.format( "Could not prepare template %s on %s", template.getTemplateName(), hostname ) );
        }
    }


    @Override
    public boolean templateExists( final Template template ) throws ResourceHostException
    {
        Preconditions.checkNotNull( template, "Invalid template" );

        try
        {
            CommandResult commandresult = execute( new RequestBuilder( "subutai list -t" )
                    .withCmdArgs( Lists.newArrayList( template.getTemplateName() ) ) );
            if ( commandresult.hasSucceeded() )
            {
                LOG.debug( String.format( "Template %s exists on %s.", template.getTemplateName(), hostname ) );
                return true;
            }
            else
            {
                LOG.warn( String.format( "Template %s does not exists on %s.", template.getTemplateName(), hostname ) );
                return false;
            }
        }
        catch ( CommandException ce )
        {
            LOG.error( "Command exception.", ce );
            throw new ResourceHostException( "General command exception on checking container existence.", ce );
        }
    }


    @Override
    public void importTemplate( final Template template ) throws ResourceHostException
    {
        Preconditions.checkNotNull( template, "Invalid template" );

        final ResourceHost THIS = this;

        Future<Boolean> future = queueSequentialTask( new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                try
                {
                    commandUtil.execute(
                            new RequestBuilder( "subutai import" ).withTimeout( TEMPLATE_IMPORT_TIMEOUT_SEC )
                                                                  .withCmdArgs( Lists.newArrayList(
                                                                          template.getTemplateName() ) ), THIS );

                    return true;
                }
                catch ( CommandException ce )
                {
                    LOG.error( "Template import failed", ce );
                    throw new ResourceHostException( "Template import failed", ce );
                }
            }
        } );

        try
        {
            future.get();
        }
        catch ( InterruptedException e )
        {
            throw new ResourceHostException( e );
        }
        catch ( ExecutionException e )
        {
            throw new ResourceHostException( "Error importing template", e.getCause() );
        }
    }


    @Override
    public void updateRepository( final Template template ) throws ResourceHostException
    {
        Preconditions.checkNotNull( template, "Invalid template" );

        if ( template.isRemote() )
        {
            Future<Boolean> future = queueSequentialTask( new Callable<Boolean>()
            {
                @Override
                public Boolean call() throws Exception
                {

                    try
                    {
                        LOG.debug( String.format( "Adding remote repository %s to %s...", template.getPeerId(),
                                hostname ) );
                        CommandResult commandResult = execute( new RequestBuilder( String.format(
                                "echo \"deb http://gw.intra.lan:9999/%1$s trusty main\" > /etc/apt/sources.list"
                                        + ".d/%1$s.list ", template.getPeerId().toString() ) ) );
                        if ( !commandResult.hasSucceeded() )
                        {
                            LOG.warn( String.format( "Could not add repository %s to %s.", template.getPeerId(),
                                    hostname ), commandResult );
                        }
                        LOG.debug( String.format( "Updating repository index on %s...", hostname ) );
                        commandResult = execute( new RequestBuilder( "apt-get update" ).withTimeout( 300 ) );
                        if ( !commandResult.hasSucceeded() )
                        {
                            LOG.warn( String.format( "Could not update repository %s on %s.", template.getPeerId(),
                                    hostname ), commandResult );
                        }
                        return true;
                    }
                    catch ( CommandException ce )
                    {
                        LOG.error( "Command exception.", ce );
                        throw new ResourceHostException( "General command exception on updating repository.", ce );
                    }
                }
            } );

            try
            {
                future.get();
            }
            catch ( InterruptedException e )
            {
                throw new ResourceHostException( e );
            }
            catch ( ExecutionException e )
            {
                throw new ResourceHostException( "Error updating repository", e.getCause() );
            }
        }
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
