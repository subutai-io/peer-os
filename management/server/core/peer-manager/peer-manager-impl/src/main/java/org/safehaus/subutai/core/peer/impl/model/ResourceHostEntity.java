package org.safehaus.subutai.core.peer.impl.model;


import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandStatus;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.ContainerState;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.HostTask;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.api.ResourceHostException;
import org.safehaus.subutai.core.strategy.api.ServerMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Resource host implementation.
 */
@Entity
@Table( name = "resource_host" )
@Access( AccessType.FIELD )
public class ResourceHostEntity extends AbstractSubutaiHost implements ResourceHost
{
    @javax.persistence.Transient
    transient protected static final Logger LOG = LoggerFactory.getLogger( ResourceHostEntity.class );
    @javax.persistence.Transient
    transient private static final Pattern LXC_STATE_PATTERN = Pattern.compile( "State:(\\s*)(.*)" );
    @javax.persistence.Transient
    transient private static final Pattern LOAD_AVERAGE_PATTERN = Pattern.compile( "load average: (.*)" );
    @javax.persistence.Transient
    transient private static final long WAIT_BEFORE_CHECK_STATUS_TIMEOUT_MS = 10000;
    @javax.persistence.Transient
    transient private static final int HOST_EXPIRATION = 60;
    @javax.persistence.Transient
    transient private ExecutorService singleThreadExecutorService;

    @javax.persistence.Transient
    transient private ExecutorService cachedThredPoolService;

    @javax.persistence.Transient
    transient private Queue<HostTask> taskQueue = new LinkedList<>();

    //    @javax.persistence.Transient
    //    protected Cache<UUID, ContainerHostInfo> hostCache;

    @OneToMany( mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true,
            targetEntity = ContainerHostEntity.class )
    Set<ContainerHost> containersHosts = new HashSet();


    private ResourceHostEntity()
    {
    }


    public ResourceHostEntity( final String peerId, final HostInfo resourceHostInfo )
    {
        super( peerId, resourceHostInfo );
    }


    private ExecutorService getSingleThreadExecutorService()
    {
        if ( singleThreadExecutorService == null )
        {
            singleThreadExecutorService = Executors.newSingleThreadExecutor();
            LOG.debug( String.format( "New single thread executor created for %s", hostname ) );
        }

        return singleThreadExecutorService;
    }


    private ExecutorService getCachedThreadExecutorService()
    {
        if ( cachedThredPoolService == null )
        {
            cachedThredPoolService = Executors.newCachedThreadPool();
        }

        return cachedThredPoolService;
    }


    @Override
    public synchronized void queue( final HostTask hostTask )
    {
        LOG.debug( String.format( "New sequential task %s queued.", hostTask.getId() ) );
        ExecutorService executorService = getSingleThreadExecutorService();
        //        LOG.debug( executorService.toString() );
        executorService.submit( hostTask );
    }


    public synchronized void run( final HostTask hostTask )
    {
        LOG.info( String.format( "New immediate task %s added.", hostTask.getId() ) );
        getCachedThreadExecutorService().submit( hostTask );
    }
    //
    //    private Cache<UUID, ContainerHostInfo> getHostCache()
    //    {
    //        if ( hostCache == null )
    //        {
    //            hostCache = CacheBuilder.newBuilder().
    //                    expireAfterWrite( HOST_EXPIRATION, TimeUnit.SECONDS ).
    //                                            build();
    //        }
    //        return hostCache;
    //    }


    public boolean startContainerHost( final ContainerHost container ) throws ResourceHostException
    {

        RequestBuilder requestBuilder =
                new RequestBuilder( String.format( "/usr/bin/lxc-start -n %s -d", container.getHostname() ) )
                        .withTimeout( 180 );
        try
        {
            execute( requestBuilder );
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( "Error on starting container.", e.getMessage() );
        }
        try
        {
            Thread.sleep( WAIT_BEFORE_CHECK_STATUS_TIMEOUT_MS );
        }
        catch ( InterruptedException ignore )
        {
        }

        return ContainerState.RUNNING.equals( getContainerHostState( container ) );
    }


    private ContainerState getContainerHostState( final ContainerHost container ) throws ResourceHostException
    {
        RequestBuilder requestBuilder =
                new RequestBuilder( String.format( "/usr/bin/lxc-info -n %s", container.getHostname() ) )
                        .withTimeout( 30 );
        CommandResult result = null;
        try
        {
            result = execute( requestBuilder );
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( "Error on fetching container state.", e.getMessage() );
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


    public ServerMetric getMetric() throws ResourceHostException
    {
        RequestBuilder requestBuilder =
                new RequestBuilder( "free -m | grep buffers/cache ; df /lxc-data | grep /lxc-data ; uptime ; nproc" )
                        .withTimeout( 30 );
        try
        {
            CommandResult result = execute( requestBuilder );
            ServerMetric serverMetric = null;
            if ( result.hasCompleted() )
            {
                String[] metrics = result.getStdOut().split( "\n" );
                serverMetric = gatherMetrics( metrics );
                //                serverMetric.setAverageMetrics( gatherAvgMetrics() );
            }
            return serverMetric;
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( "Unable retrieve host metric", e.toString() );
        }
    }


    public Set<ContainerHost> getContainerHosts()
    {
        return containersHosts;
    }


    /**
     * Gather metrics from elastic search for a one week period
     */
    //    private Map<MetricType, Double> gatherAvgMetrics()
    //    {
    //        //TODO: Implement me
    //        return new EnumMap<>( MetricType.class );
    //    }


    /**
     * Gather metrics from linux commands outputs.
     */
    private ServerMetric gatherMetrics( String[] metrics )
    {
        int freeRamMb = 0;
        int freeHddMb = 0;
        int numOfProc = 0;
        double loadAvg = 0;
        double cpuLoadPercent = 100;
        // parsing only 4 metrics
        if ( metrics.length != 4 )
        {
            return null;
        }
        boolean parseOk = true;
        for ( int line = 0; parseOk && line < metrics.length; line++ )
        {
            String metric = metrics[line];
            switch ( line )
            {
                case 0:
                    //-/+ buffers/cache:       1829       5810
                    String[] ramMetric = metric.split( "\\s+" );
                    String freeRamMbStr = ramMetric[ramMetric.length - 1];
                    try
                    {
                        freeRamMb = Integer.parseInt( freeRamMbStr );
                    }
                    catch ( Exception e )
                    {
                        parseOk = false;
                    }
                    break;
                case 1:
                    //lxc-data       143264768 608768 142656000   1% /lxc-data
                    String[] hddMetric = metric.split( "\\s+" );
                    if ( hddMetric.length == 6 )
                    {
                        String hddMetricKbStr = hddMetric[3];
                        try
                        {
                            freeHddMb = Integer.parseInt( hddMetricKbStr ) / 1024;
                        }
                        catch ( Exception e )
                        {
                            parseOk = false;
                        }
                    }
                    else
                    {
                        parseOk = false;
                    }
                    break;
                case 2:
                    // 09:17:38 up 4 days, 23:06,  0 users,  load average: 2.18, 3.06, 2.12
                    Matcher m = LOAD_AVERAGE_PATTERN.matcher( metric );
                    if ( m.find() )
                    {
                        String[] loads = m.group( 1 ).split( "," );
                        try
                        {
                            loadAvg = ( Double.parseDouble( loads[0] ) + Double.parseDouble( loads[1] ) + Double
                                    .parseDouble( loads[2] ) ) / 3;
                        }
                        catch ( Exception e )
                        {
                            parseOk = false;
                        }
                    }
                    else
                    {
                        parseOk = false;
                    }
                    break;
                case 3:
                    try
                    {
                        numOfProc = Integer.parseInt( metric );
                        if ( numOfProc > 0 )
                        {
                            cpuLoadPercent = ( loadAvg / numOfProc ) * 100;
                        }
                        else
                        {
                            break;
                        }
                    }
                    catch ( Exception e )
                    {
                        parseOk = false;
                    }
                    break;
            }
        }
        if ( parseOk )
        {
            return new ServerMetric( getHostname(), freeHddMb, freeRamMb, ( int ) cpuLoadPercent, numOfProc );
        }
        else
        {
            return null;
        }
    }


    public boolean stopContainerHost( final ContainerHost container ) throws ResourceHostException
    {
        RequestBuilder requestBuilder =
                new RequestBuilder( String.format( "/usr/bin/lxc-stop -n %s", container.getHostname() ) )
                        .withTimeout( 180 );
        try
        {
            execute( requestBuilder );
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( "Error on stopping container.", e.getMessage() );
        }

        try
        {
            Thread.sleep( WAIT_BEFORE_CHECK_STATUS_TIMEOUT_MS );
        }
        catch ( InterruptedException ignore )
        {
        }

        return ContainerState.STOPPED.equals( getContainerHostState( container ) );
    }


    public void destroyContainerHost( final ContainerHost containerHost ) throws ResourceHostException
    {
        run( Command.DESTROY, containerHost.getHostname() );
    }


    public ContainerHost getContainerHostByName( final String hostname )
    {
        ContainerHost result = null;
        Iterator iterator = containersHosts.iterator();

        while ( result == null && iterator.hasNext() )
        {
            ContainerHost host = ( ContainerHost ) iterator.next();

            if ( host.getHostname().equals( hostname ) )
            {
                result = host;
            }
        }
        return result;
    }


    public Set<ContainerHost> getContainerHostsByEnvironmentId( final UUID environmentId )
    {
        Set<ContainerHost> result = new HashSet<>();
        for ( ContainerHost containerHost : getContainerHosts() )
        {
            if ( containerHost.getEnvironmentId() == null )
            {
                continue;
            }
            if ( containerHost.getEnvironmentId().equals( environmentId ) )
            {
                result.add( containerHost );
            }
        }
        return result;
    }


    public void removeContainerHost( final Host result )
    {
        if ( containersHosts.contains( result ) )
        {
            containersHosts.remove( result );
        }
    }


    public ContainerHost getContainerHostById( final String id )
    {
        ContainerHost result = null;
        Iterator iterator = containersHosts.iterator();

        while ( result == null && iterator.hasNext() )
        {
            ContainerHost host = ( ContainerHost ) iterator.next();

            if ( host.getHostId().equals( id ) )
            {
                result = host;
            }
        }
        return result;
    }


    @Override
    public void cloneContainer( final String templateName, final String cloneName ) throws ResourceHostException
    {
        LOG.debug( String.format( "Cloning container %s on %s from template %s", cloneName, hostname, templateName ) );
        run( Command.CLONE, templateName, cloneName );
    }


    //    private void create( final CloneTask cloneTask )
    //    {
    //        try
    //        {
    //            prepareTemplates( cloneTask.getParameter().getTemplates() );
    //            run( Command.CLONE, cloneTask.getParameter().getTemplateName(), cloneTask.getParameter()
    // .getHostname() );
    //            ContainerHost host = getContainerHostByName( cloneTask.getParameter().getHostname() );
    //            if ( host != null )
    //            {
    //                cloneTask.success();
    //            }
    //            else
    //            {
    //                cloneTask.fail( new ResourceHostException(
    //                        String.format( "Container %s on % is not cloned in estimated time.", this.hostname,
    //                                cloneTask.getParameter().getHostname() ), "" ) );
    //            }
    //        }
    //        catch ( Exception e )
    //        {
    //            cloneTask.fail( new ResourceHostException(
    //                    String.format( "Error on cloning container %s on %s.", this.hostname,
    //                            cloneTask.getParameter().getHostname() ), e.toString() ) );
    //        }
    //        fireEvent( new HostEvent( this, HostEvent.EventType.TASK_FINISHED, cloneTask ) );
    //    }
    //
    //
    //    @Override
    //    public void createContainer( final ContainerCreateOrder containerCreateOrder )
    //
    //    {
    //        Preconditions.checkNotNull( containerCreateOrder, "Container create order is null." );
    //        LOG.info( "Scheduled new container clone order for %s", containerCreateOrder.getHostname() );
    //        final CloneTask task = new CloneTask( containerCreateOrder );
    //        getSingleThreadExecutorService().execute( new Runnable()
    //        {
    //            @Override
    //            public void run()
    //            {
    //                create( task );
    //            }
    //        } );
    //    }


    //    private ContainerHost waitHeartbeatAndCreateContainerHost( final String containerName, final String
    // templateName,
    //                                                               final String envId, final String creatorPeerId )
    //            throws PeerException
    //    {
    //        HostInfo hostInfo = waitHeartbeat( containerName, 15 );
    //        if ( hostInfo == null )
    //        {
    //            throw new ResourceHostException( "Heartbeat from container not received.", null );
    //        }
    //        ContainerHost containerHost = new ContainerHostEntity( getPeerId(), creatorPeerId, envId, hostInfo );
    //        containerHost.setCreatorPeerId( creatorPeerId );
    //        containerHost.setTemplateName( templateName );
    //        containerHost.setTemplateArch( "amd64" );
    //        containerHost.updateHostInfo();
    //        addContainerHost( containerHost );
    //        return containerHost;
    //    }


    @Override
    public void prepareTemplates( List<Template> templates ) throws ResourceHostException
    {
        LOG.debug( String.format( "Preparing templates on %s...", hostname ) );
        for ( Template p : templates )
        {
            prepareTemplate( p );
        }
        LOG.debug( "Template successfully prepared." );
    }


    @Override
    public void prepareTemplate( final Template p ) throws ResourceHostException
    {
        if ( isTemplateExist( p ) )
        {
            return;
        }
        importTemplate( p );
        if ( isTemplateExist( p ) )
        {
            return;
        }
        // trying add repository
        updateRepository( p );
        importTemplate( p );
        if ( !isTemplateExist( p ) )
        {
            LOG.debug( String.format( "Could not prepare template %s on %s.", p.getTemplateName(), hostname ) );
            throw new ResourceHostException( "Prepare template exception.",
                    String.format( "Could not prepare template %s on %s", p.getTemplateName(),
                            getAgent().getHostname() ) );
        }
    }


    @Override
    public boolean isTemplateExist( final Template template ) throws ResourceHostException
    {
        try
        {
            run( Command.LIST_TEMPLATES, template.getTemplateName() );
            LOG.debug( String.format( "Template %s exists on %s.", template.getTemplateName(), hostname ) );
            return true;
        }
        catch ( ResourceHostException rhe )
        {
            LOG.debug( String.format( "Template %s does not exists on %s.", template.getTemplateName(), hostname ) );
            return false;
        }
    }


    @Override
    public void importTemplate( Template template ) throws ResourceHostException
    {
        LOG.debug( String.format( "Trying to import template %s to %s.", template.getTemplateName(), hostname ) );
        run( Command.IMPORT, template.getTemplateName() );
    }


    @Override
    public void updateRepository( Template template ) throws ResourceHostException
    {
        if ( template.isRemote() )
        {
            LOG.debug( String.format( "Adding remote repository %s to %s...", template.getPeerId(), hostname ) );
            run( Command.ADD_SOURCE, template.getPeerId().toString() );
            run( Command.APT_GET_UPDATE );
        }
    }


    /**
     * Promotes a given clone into a template with given name. This method gives possibility to promote a copy of the
     * clone instead of the clone itself.
     *
     * @param hostName the physical host name
     * @param cloneName name of the clone to be converted
     * @param newName new name for template
     * @param copyit if set <tt>true</tt>, a copy of clone is made first and a copied clone is promoted to template
     *
     * @return <tt>true</tt> if promote successfully completed
     */
    public boolean promote( String hostName, String cloneName, String newName, boolean copyit )
            throws ResourceHostException
    {
        List<String> args = new ArrayList<>();
        if ( newName != null && newName.length() > 0 )
        {
            args.add( "-n " + newName );
        }
        if ( copyit )
        {
            args.add( "-c" );
        }
        args.add( cloneName );
        String[] arr = args.toArray( new String[args.size()] );
        run( Command.PROMOTE, arr );
        return true;
    }


    /**
     * Exports the template in the given server into a deb package.
     *
     * @param templateName the template name to be exported
     *
     * @return path to generated deb package
     */
    public String exportTemplate( String templateName ) throws ResourceHostException
    {
        run( Command.EXPORT, templateName );
        return getExportedPackageFilePath( templateName );
    }


    /**
     * Gets a full Debian package name for a given template. Name does not have <tt>.deb</tt> extension.
     *
     * @param templateName the template name
     */
    public String getDebianPackageName( String templateName ) throws ResourceHostException
    {
        try
        {
            CommandResult commandResult = execute( Command.GET_DEB_PACKAGE_NAME.build( templateName ) );
            if ( commandResult.hasSucceeded() )
            {
                return commandResult.getStdOut();
            }
            else
            {
                return null;
            }
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( "Could not get deb package name.", e.toString() );
        }
    }


    private String getExportedPackageFilePath( String templateName ) throws ResourceHostException
    {
        String result = null;
        try
        {
            CommandResult dirCommandResult = execute( Command.SUBUTAI_TMPDIR.build() );
            if ( dirCommandResult.hasSucceeded() )
            {
                String dir = dirCommandResult.getStdOut();
                CommandResult packageNameCommandResult = execute( Command.GET_PACKAGE_NAME.build( templateName ) );
                if ( packageNameCommandResult.hasSucceeded() )
                {
                    result = Paths.get( dir, packageNameCommandResult.getStdOut() ).toString();
                }
            }
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( "Could not get exported package file path.", e.toString() );
        }

        if ( result == null )
        {
            throw new ResourceHostException( "Could not get exported package file path.", templateName );
        }
        return result;
    }


    /**
     * Gets package name for a given template. Package name is a name used in Apt commands. It is NOT a full Debian
     * package name of a template.
     */
    public String getPackageName( String templateName ) throws ResourceHostException
    {
        try
        {
            CommandResult commandResult = execute( Command.GET_PACKAGE_NAME.build( templateName ) );
            if ( commandResult.hasSucceeded() )
            {
                return commandResult.getStdOut();
            }
            else
            {
                return null;
            }
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( "Could not get package name.", e.toString() );
        }
    }


    protected void run( Command command, String... args ) throws ResourceHostException
    {
        try
        {
            CommandResult commandResult = execute( command.build( args ) );

            if ( !commandResult.getStatus().equals( CommandStatus.SUCCEEDED ) )
            {
                throw new ResourceHostException(
                        String.format( "Command execution failed %s.", String.format( command.script, args ) ),
                        commandResult.getStdErr() );
            }
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException(
                    String.format( "Could not execute script/command %s", String.format( command.script, args ) ),
                    e.toString() );
        }
    }


    public Set<ContainerHost> getContainerHostsByNameList( final Set<String> cloneNames )
    {
        Set<ContainerHost> result = new HashSet<>();
        for ( ContainerHost containerHost : getContainerHosts() )
        {
            if ( cloneNames.contains( containerHost.getHostname() ) )
            {
                result.add( containerHost );
            }
        }
        return result;
    }


    public void addContainerHost( ContainerHost host )
    {
        if ( host == null )
        {
            throw new IllegalArgumentException( "Container host could not be null." );
        }

        ( ( ContainerHostEntity ) host ).setParent( this );
        containersHosts.add( host );
    }


    //    @Override
    //    public void onHeartbeat( final ResourceHostInfo resourceHostInfo )
    //    {
    //        for ( ContainerHostInfo containerHostInfo : resourceHostInfo.getContainers() )
    //        {
    //            getHostCache().put( containerHostInfo.getId(), containerHostInfo );
    //        }
    //    }


    //    private HostInfo waitHeartbeat( String hostname, int timeoutInSeconds )
    //    {
    //        long threshold = System.currentTimeMillis() + timeoutInSeconds * 1000;
    //        HostInfo result = getHeartbeat( hostname );
    //        while ( result == null && System.currentTimeMillis() < threshold )
    //        {
    //            LOG.info( String.format( "Waiting for host: %s... Left seconds: %d", hostname,
    //                    ( threshold - System.currentTimeMillis() ) / 1000 ) );
    //            try
    //            {
    //                Thread.sleep( 2000 );
    //            }
    //            catch ( InterruptedException ignore )
    //            {
    //                break;
    //            }
    //            result = getHeartbeat( hostname );
    //        }
    //        return result;
    //    }


    //    private HostInfo getHeartbeat( final String hostname )
    //    {
    //        for ( HostInfo hostInfo : getHostCache().asMap().values() )
    //        {
    //            if ( hostname.equals( hostInfo.getHostname() ) )
    //            {
    //                return hostInfo;
    //            }
    //        }
    //        return null;
    //    }


    enum Command
    {
        LIST_TEMPLATES( "subutai list -t %s" ),
        CLONE( "subutai clone %s %s", 1, true ),
        DESTROY( "subutai destroy %s" ),
        IMPORT( "subutai import %s" ),
        PROMOTE( "promote %s" ),
        EXPORT( "subutai export %s" ),
        SUBUTAI_TMPDIR( "echo $SUBUTAI_TMPDIR" ),
        GET_PACKAGE_NAME( ". /usr/share/subutai-cli/subutai/lib/deb_ops && get_package_name  %s" ),
        GET_DEB_PACKAGE_NAME(
                ". /etc/subutai/config && . /usr/share/subutai-cli/subutai/lib/deb_ops && get_debian_package_name  "
                        + "%s" ),
        ADD_SOURCE( "echo \"deb http://gw.intra.lan:9999/%1$s trusty main\" > /etc/apt/sources.list.d/%1$s.list " ),
        REMOVE_SOURCE( "rm /etc/apt/sources.list.d/%1$s.list " ),
        APT_GET_UPDATE( "apt-get update", 240 );

        String script;
        boolean daemon = false;
        int timeout = 120;


        Command( String script )
        {
            this.script = script;
        }


        Command( String script, int timeout )
        {
            this.script = script;
            this.timeout = timeout;
        }


        Command( String script, int timeout, boolean daemon )
        {
            this.script = script;
            this.timeout = timeout;
            this.daemon = daemon;
        }


        public RequestBuilder build( String... args )
        {
            String s = String.format( this.script, args );
            RequestBuilder rb = new RequestBuilder( s );
            rb.withTimeout( timeout );
            if ( daemon )
            {
                rb.daemon();
            }
            return rb;
        }
    }
}
