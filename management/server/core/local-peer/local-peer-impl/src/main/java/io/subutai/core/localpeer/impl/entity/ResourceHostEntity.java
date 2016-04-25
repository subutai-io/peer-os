package io.subutai.core.localpeer.impl.entity;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import io.subutai.common.environment.RhP2pIp;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.InstanceType;
import io.subutai.common.host.NullHostInterface;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.network.JournalCtlLevel;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.network.P2pLogs;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.Disposable;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.protocol.Tunnels;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.settings.Common;
import io.subutai.common.util.P2PUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.localpeer.impl.ResourceHostCommands;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.registration.api.RegistrationManager;


/**
 * Resource host implementation.
 */
@Entity
@Table( name = "r_host" )
@Access( AccessType.FIELD )
public class ResourceHostEntity extends AbstractSubutaiHost implements ResourceHost, Disposable
{
    private static final Logger LOG = LoggerFactory.getLogger( ResourceHostEntity.class );
    private static final Pattern LXC_STATE_PATTERN = Pattern.compile( "(RUNNING|STOPPED|FROZEN)" );
    private static final String PRECONDITION_CONTAINER_IS_NULL_MSG = "Container host is null";
    private static final String CONTAINER_EXCEPTION_MSG_FORMAT = "Container with name %s does not exist";
    private static final Pattern CLONE_OUTPUT_PATTERN = Pattern.compile( "with ID (.*) successfully cloned" );


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
    protected CommandUtil commandUtil = new CommandUtil();

    @Transient
    protected ResourceHostCommands resourceHostCommands = new ResourceHostCommands();


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
    }


    public ResourceHostEntity( final String peerId, final ResourceHostInfo resourceHostInfo )
    {
        super( peerId, resourceHostInfo );

        this.instanceType = resourceHostInfo.getInstanceType();

        setSavedHostInterfaces( resourceHostInfo.getHostInterfaces() );

        init();
    }


    @Override
    public Set<HostInterface> getSavedHostInterfaces()
    {
        return netInterfaces;
    }


    protected void setSavedHostInterfaces( HostInterfaces hostInterfaces )
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
            return getHostRegistry().getResourceHostInfoById( getId() ).getContainers();
        }
        catch ( HostDisconnectedException e )
        {
            LOG.warn( "Error in getContainers", e );
        }

        return Sets.newHashSet();
    }


    public void dispose()
    {
    }


    //this method must be executed sequentially since other parallel executions can setup the same tunnel
    public synchronized void setupTunnels( P2pIps p2pIps, NetworkResource networkResource ) throws ResourceHostException
    {
        Preconditions.checkNotNull( p2pIps, "Invalid peer ips set" );
        Preconditions.checkNotNull( networkResource, "Invalid networkResource" );


        Tunnels tunnels = getTunnels();

        //setup tunnel to each local and remote RH
        for ( RhP2pIp rhP2pIp : p2pIps.getP2pIps() )
        {
            //skip self
            if ( getId().equalsIgnoreCase( rhP2pIp.getRhId() ) )
            {
                continue;
            }

            //skip if own IP
            boolean ownIp = !( getHostInterfaces().findByIp( rhP2pIp.getP2pIp() ) instanceof NullHostInterface );
            if ( ownIp )
            {
                continue;
            }

            //check p2p connections in case heartbeat hasn't arrived yet with new p2p interface
            P2PConnections p2PConnections = getP2PConnections();
            //skip if exists
            if ( p2PConnections.findByIp( rhP2pIp.getP2pIp() ) != null )
            {
                continue;
            }

            //see if tunnel exists
            Tunnel tunnel = tunnels.findByIp( rhP2pIp.getP2pIp() );

            //create new tunnel
            if ( tunnel == null )
            {
                String tunnelName = P2PUtil.generateTunnelName( tunnels );

                if ( tunnelName == null )
                {
                    throw new ResourceHostException( "Free tunnel name not found" );
                }

                Tunnel newTunnel = new Tunnel( tunnelName, rhP2pIp.getP2pIp(), networkResource.getVlan(),
                        networkResource.getVni() );

                createTunnel( newTunnel );

                //add to avoid duplication in the next iteration
                tunnels.addTunnel( newTunnel );
            }
        }
    }


    protected NetworkManager getNetworkManager()
    {
        return ServiceLocator.getServiceNoCache( NetworkManager.class );
    }


    protected RegistrationManager getRegistrationManager()
    {
        return ServiceLocator.getServiceNoCache( RegistrationManager.class );
    }


    protected QuotaManager getQuotaManager()
    {
        return ServiceLocator.getServiceNoCache( QuotaManager.class );
    }


    protected HostRegistry getHostRegistry()
    {
        return ServiceLocator.getServiceNoCache( HostRegistry.class );
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


        CommandResult result;
        try
        {
            result = commandUtil
                    .execute( resourceHostCommands.getListContainerInfoCommand( containerHost.getHostname() ), this );
        }
        catch ( CommandException e )
        {
            LOG.error( e.getMessage(), e );
            throw new ResourceHostException(
                    String.format( "Error fetching container %s state: %s", containerHost.getHostname(),
                            e.getMessage() ), e );
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
            return containersHosts == null ? Sets.<ContainerHost>newHashSet() :
                   Sets.newConcurrentHashSet( containersHosts );
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

        try
        {
            commandUtil.execute( resourceHostCommands.getStartContainerCommand( containerHost.getHostname() ), this );
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException(
                    String.format( "Error on starting container %s: %s", containerHost.getHostname(), e.getMessage() ),
                    e );
        }

        waitContainerStart( containerHost );
    }


    private void waitContainerStart( final ContainerHost containerHost ) throws ResourceHostException
    {
        Preconditions.checkNotNull( containerHost, PRECONDITION_CONTAINER_IS_NULL_MSG );

        //wait container connection
        long ts = System.currentTimeMillis();
        while ( System.currentTimeMillis() - ts < Common.WAIT_CONTAINER_CONNECTION_SEC * 1000 && !containerHost
                .isConnected() )
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

        try
        {
            commandUtil.execute( resourceHostCommands.getStopContainerCommand( containerHost.getHostname() ), this );
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException(
                    String.format( "Error stopping container %s: %s", containerHost.getHostname(), e.getMessage() ),
                    e );
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

        try
        {
            commandUtil.execute( resourceHostCommands.getDestroyContainerCommand( containerHost.getHostname() ), this );
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException(
                    String.format( "Error destroying container %s: %s", containerHost.getHostname(), e.getMessage() ),
                    e );
        }

        removeContainerHost( containerHost );
    }


    @Override
    public void setContainerQuota( final ContainerHost containerHost, final ContainerSize containerSize )
            throws ResourceHostException
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

        ContainerQuota quota = getQuotaManager().getDefaultContainerQuota( containerSize );

        try
        {
            commandUtil.execute( resourceHostCommands.getSetQuotaCommand( containerHost.getHostname(), quota ), this );
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( String.format( "Error setting quota %s to container %s: %s", containerSize,
                    containerHost.getHostname(), e.getMessage() ), e );
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
            ( ( ContainerHostEntity ) containerHost ).setParent( null );
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
            commandUtil.execute( resourceHostCommands.getCleanupEnvironmentCommand( vlan ), this );
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException(
                    String.format( "Could not cleanup resource host %s: %s", hostname, e.getMessage() ) );
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
                CommandResult commandResult =
                        commandUtil.execute( resourceHostCommands.getFetchCpuCoresNumberCommand(), this );

                numberOfCpuCores = Integer.parseInt( commandResult.getStdOut().trim() );
            }
            catch ( Exception e )
            {
                throw new ResourceHostException( String.format( "Error fetching # of cpu cores: %s", e.getMessage() ),
                        e );
            }
        }

        return numberOfCpuCores;
    }


    @Override
    public P2PConnections getP2PConnections() throws ResourceHostException
    {
        try
        {
            return getNetworkManager().getP2PConnections( this );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException( String.format( "Failed to get P2P connections: %s", e.getMessage() ), e );
        }
    }


    @Override
    public void joinP2PSwarm( final String p2pIp, final String interfaceName, final String p2pHash,
                              final String secretKey, final long secretKeyTtlSec ) throws ResourceHostException
    {
        try
        {
            if ( getP2PConnections().findByHash( p2pHash ) != null )
            {
                getNetworkManager().resetSwarmSecretKey( this, p2pHash, secretKey, secretKeyTtlSec );
            }
            else
            {
                getNetworkManager().joinP2PSwarm( this, interfaceName, p2pIp, p2pHash, secretKey, secretKeyTtlSec );
            }
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException( String.format( "Failed to join P2P swarm: %s", e.getMessage() ), e );
        }
    }


    @Override
    public void resetSwarmSecretKey( final String p2pHash, final String newSecretKey, final long ttlSeconds )
            throws ResourceHostException
    {
        try
        {
            if ( getP2PConnections().findByHash( p2pHash ) != null )
            {
                getNetworkManager().resetSwarmSecretKey( this, p2pHash, newSecretKey, ttlSeconds );
            }
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException(
                    String.format( "Failed to reset P2P connection secret key: %s", e.getMessage() ), e );
        }
    }


    @Override
    public Tunnels getTunnels() throws ResourceHostException
    {
        try
        {
            return getNetworkManager().getTunnels( this );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException( String.format( "Failed to get tunnels: %s", e.getMessage() ), e );
        }
    }


    @Override
    public void createTunnel( final Tunnel tunnel ) throws ResourceHostException
    {
        try
        {
            getNetworkManager().createTunnel( this, tunnel.getTunnelName(), tunnel.getTunnelIp(), tunnel.getVlan(),
                    tunnel.getVni() );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException( String.format( "Failed to create tunnel: %s", e.getMessage() ), e );
        }
    }


    @Override
    public void importTemplate( final String templateName ) throws ResourceHostException
    {
        try
        {
            commandUtil.execute( resourceHostCommands.getImportTemplateCommand( templateName ), this );
        }
        catch ( Exception e )
        {
            throw new ResourceHostException(
                    String.format( "Error importing template %s: %s", templateName, e.getMessage() ), e );
        }
    }


    @Override
    public String cloneContainer( final String templateName, final String hostname, final String ip, final int vlan,
                                  final String environmentId ) throws ResourceHostException
    {
        try
        {
            //generate registration token for container for 30 min
            String token = getRegistrationManager().generateContainerTTLToken( 30 * 60 * 1000L ).getToken();

            CommandResult result = commandUtil.execute( resourceHostCommands
                    .getCloneContainerCommand( templateName, hostname, ip, vlan, environmentId, token ), this );

            //parse ID from output

            StringTokenizer st = new StringTokenizer( result.getStdOut(), System.lineSeparator() );

            String containerId = null;

            while ( st.hasMoreTokens() )
            {

                final String nextToken = st.nextToken();

                Matcher m = CLONE_OUTPUT_PATTERN.matcher( nextToken );

                if ( m.find() && m.groupCount() == 1 )
                {
                    containerId = m.group( 1 );
                    break;
                }
            }


            if ( Strings.isNullOrEmpty( containerId ) )
            {
                LOG.error( "Container ID not found in output of subutai clone command" );

                throw new CommandException( "Container ID not found in output of subutai clone command" );
            }

            return containerId;
        }
        catch ( Exception e )
        {
            throw new ResourceHostException(
                    String.format( "Error cloning container %s: %s", hostname, e.getMessage() ), e );
        }
    }


    @Override
    public boolean updateHostInfo( final HostInfo hostInfo )
    {
        super.updateHostInfo( hostInfo );

        setSavedHostInterfaces( hostInfo.getHostInterfaces() );

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
                if ( Common.MANAGEMENT_HOSTNAME.equals( info.getHostname() ) )
                {
                    containerHost = new ContainerHostEntity( peerId, info.getId(), info.getHostname(), info.getArch(),
                            info.getHostInterfaces(), info.getContainerName(), Common.MANAGEMENT_HOSTNAME,
                            info.getArch().name(), Common.MANAGEMENT_HOSTNAME, null, null, ContainerSize.SMALL );

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
    public String getRhVersion() throws ResourceHostException
    {
        try
        {
            return commandUtil.execute( resourceHostCommands.getGetRhVersionCommand(), this ).getStdOut();
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( String.format( "Error obtaining RH version: %s", e.getMessage() ), e );
        }
    }


    @Override
    public String getP2pVersion() throws ResourceHostException
    {
        try
        {
            //todo use "subutai" binding when implemented
            return commandUtil.execute( new RequestBuilder( "/apps/subutai/current/bin/p2p version" ), this )
                              .getStdOut();
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( String.format( "Error obtaining P2P version: %s", e.getMessage() ), e );
        }
    }


    public P2pLogs getP2pLogs( JournalCtlLevel logLevel, Date from, Date till ) throws ResourceHostException
    {
        P2pLogs p2pLogs = new P2pLogs();

        try
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

            CommandResult result = execute( new RequestBuilder(
                    String.format( "journalctl -u *p2p* --since \"%s\" --until " + "\"%s\"",
                            simpleDateFormat.format( from ), simpleDateFormat.format( till ) ) ) );

            StringTokenizer st = new StringTokenizer( result.getStdOut(), System.lineSeparator() );

            while ( st.hasMoreTokens() )
            {
                String logLine = st.nextToken();

                if ( logLevel == JournalCtlLevel.ALL && !Strings.isNullOrEmpty( logLine ) )
                {
                    p2pLogs.addLog( logLine );
                }
                else if ( logLine.contains( String.format( "[%s]", logLevel.name() ) ) )
                {
                    p2pLogs.addLog( logLine );
                }
            }
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( String.format( "Error obtaining P2P logs: %s", e.getMessage() ), e );
        }

        return p2pLogs;
    }


    @Override
    public boolean isConnected()
    {
        return getPeer().isConnected( new HostId( getId() ) );
    }


    @Override
    public String getLinkId()
    {
        return String.format( "%s|%s", getClassPath(), getUniqueIdentifier() );
    }


    @Override
    public String getUniqueIdentifier()
    {
        return getId();
    }


    @Override
    public String getClassPath()
    {
        return this.getClass().getSimpleName();
    }


    @Override
    public String getContext()
    {
        return PermissionObject.ResourceManagement.getName();
    }


    @Override
    public String getKeyId()
    {
        return getId();
    }
}
