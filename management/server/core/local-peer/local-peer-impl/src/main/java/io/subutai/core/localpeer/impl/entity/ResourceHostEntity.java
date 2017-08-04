package io.subutai.core.localpeer.impl.entity;


import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.environment.RhP2pIp;
import io.subutai.common.environment.RhTemplatesDownloadProgress;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.InstanceType;
import io.subutai.common.host.NullHostInterface;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.network.LogLevel;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.network.P2pLogs;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.Disposable;
import io.subutai.common.protocol.LoadBalancing;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.protocol.Protocol;
import io.subutai.common.protocol.ReservedPorts;
import io.subutai.common.protocol.Template;
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.protocol.Tunnels;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.settings.Common;
import io.subutai.common.util.NumUtil;
import io.subutai.common.util.P2PUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.localpeer.impl.ResourceHostCommands;
import io.subutai.core.localpeer.impl.binding.Commands;
import io.subutai.core.localpeer.impl.command.TemplateDownloadTracker;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.registration.api.HostRegistrationManager;
import io.subutai.hub.share.dto.domain.ReservedPortMapping;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.quota.ContainerSize;


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
    private transient final Cache<String, Map<String, Integer>> envTemplatesDownloadPercent = CacheBuilder.newBuilder().
            expireAfterAccess( Common.TEMPLATE_DOWNLOAD_TIMEOUT_SEC, TimeUnit.HOURS ).build();


    @OneToMany( mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity =
            ContainerHostEntity.class, orphanRemoval = true )
    private Set<ContainerHost> containersHosts = Sets.newHashSet();

    @Column( name = "instance" )
    @Enumerated( EnumType.STRING )
    private InstanceType instanceType;

    @OneToMany( mappedBy = "host", fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity =
            HostInterfaceEntity.class, orphanRemoval = true )
    @JsonIgnore
    private Set<HostInterface> netInterfaces = new HashSet<>();

    @Column( name = "address" )
    private String address;


    @Transient
    protected transient CommandUtil commandUtil = new CommandUtil();

    @Transient
    protected transient ResourceHostCommands resourceHostCommands = new ResourceHostCommands();


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

        this.address = resourceHostInfo.getAddress();

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
    public String getAddress()
    {
        return address;
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


    @Override
    public void dispose()
    {
        // no-op
    }


    //this method must be executed sequentially since other parallel executions can setup the same tunnel
    @Override
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


    public void deleteTunnels( P2pIps p2pIps, NetworkResource networkResource ) throws ResourceHostException
    {
        Tunnels tunnels = getTunnels();

        for ( RhP2pIp rhP2pIp : p2pIps.getP2pIps() )
        {
            Tunnel tunnel = tunnels.findByIp( rhP2pIp.getP2pIp() );

            if ( tunnel != null )
            {
                deleteTunnel( tunnel );
            }
        }
    }


    protected NetworkManager getNetworkManager()
    {
        return ServiceLocator.lookup( NetworkManager.class );
    }


    protected HostRegistrationManager getRegistrationManager()
    {
        return ServiceLocator.lookup( HostRegistrationManager.class );
    }


    protected HostRegistry getHostRegistry()
    {
        return ServiceLocator.lookup( HostRegistry.class );
    }


    protected LocalPeer getLocalPeer()
    {
        return ServiceLocator.lookup( LocalPeer.class );
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
                    .execute( resourceHostCommands.getListContainerInfoCommand( containerHost.getContainerName() ),
                            this );
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


    @Override
    public Set<ContainerHost> getContainerHosts()
    {
        synchronized ( containersHosts )
        {
            return containersHosts == null ? Sets.<ContainerHost>newHashSet() :
                   Sets.newConcurrentHashSet( containersHosts );
        }
    }


    @Override
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
            commandUtil
                    .execute( resourceHostCommands.getStartContainerCommand( containerHost.getContainerName() ), this );
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
            TaskUtil.sleep( 100 );
        }

        if ( !ContainerHostState.RUNNING.equals( getContainerHostState( containerHost ) ) )
        {
            throw new ResourceHostException(
                    String.format( "Error starting container %s", containerHost.getHostname() ) );
        }
    }


    @Override
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
            commandUtil
                    .execute( resourceHostCommands.getStopContainerCommand( containerHost.getContainerName() ), this );
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
            //todo use commandExecutor.execute to avoid exception in case container does not exist
            //todo check if exception is due to not existing container and ignore such exception
            commandUtil.execute( resourceHostCommands.getDestroyContainerCommand( containerHost.getId() ), this );
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
    public void setContainerQuota( final ContainerHost containerHost, final ContainerQuota containerQuota )
            throws ResourceHostException
    {
        Preconditions.checkNotNull( containerHost, PRECONDITION_CONTAINER_IS_NULL_MSG );
        Preconditions.checkNotNull( containerQuota );

        try
        {
            commandUtil
                    .execute( Commands.getSetQuotaCommand( containerHost.getContainerName(), containerQuota ), this );
        }
        catch ( CommandException e )
        {
            LOG.error( e.getMessage(), e );
            throw new ResourceHostException(
                    String.format( "Could not set quota values of %s", containerHost.getId() ) );
        }
    }


    @Override
    public ContainerHost getContainerHostByHostName( final String hostname ) throws HostNotFoundException
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


    @Override
    public ContainerHost getContainerHostByContainerName( final String containerName ) throws HostNotFoundException
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerName ), "Invalid container name" );

        for ( ContainerHost containerHost : getContainerHosts() )
        {

            if ( containerHost.getContainerName().equalsIgnoreCase( containerName ) )
            {
                return containerHost;
            }
        }

        throw new HostNotFoundException(
                String.format( "Container host not found by container name %s", containerName ) );
    }


    @Override
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


    @Override
    public ContainerHost getContainerHostById( final String id ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( id ), "Invalid container id" );

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
    public ContainerHost getContainerHostByIp( final String ip ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ), "Invalid container ip" );

        for ( ContainerHost containerHost : getContainerHosts() )
        {
            if ( containerHost.getIp().equals( ip ) )
            {
                return containerHost;
            }
        }

        throw new HostNotFoundException( String.format( "Container host not found by ip %s", ip ) );
    }


    @Override
    public Set<ContainerHost> getContainerHostsByEnvironmentId( final String environmentId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

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
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ownerId ), "Invalid owner id" );

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
        Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ), "Invalid peer id" );

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
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );
        Preconditions
                .checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ), "Invalid vlan" );
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

        if ( !containerHosts.isEmpty() )
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
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pIp ), "Invalid p2p IP" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( interfaceName ), "Invalid interface name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pHash ), "Invalid p2p hash" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( secretKey ), "Invalid secret" );
        Preconditions.checkArgument( secretKeyTtlSec > 0, "Ttl must be greater than 0" );

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
    public void removeP2PSwarm( String p2pHash ) throws ResourceHostException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pHash ), "Invalid p2p hash" );

        try
        {
            if ( getP2PConnections().findByHash( p2pHash ) != null )
            {
                getNetworkManager().removeP2PSwarm( this, p2pHash );
            }
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException( String.format( "Failed to remove P2P swarm: %s", e.getMessage() ), e );
        }
    }


    @Override
    public void resetSwarmSecretKey( final String p2pHash, final String newSecretKey, final long ttlSeconds )
            throws ResourceHostException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pHash ), "Invalid p2p hash" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newSecretKey ), "Invalid secret" );
        Preconditions.checkArgument( ttlSeconds > 0, "Ttl must be greater than 0" );

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
        Preconditions.checkNotNull( tunnel, "Invalid tunnel" );

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


    public void deleteTunnel( final Tunnel tunnel ) throws ResourceHostException
    {
        Preconditions.checkNotNull( tunnel, "Invalid tunnel" );

        try
        {
            getNetworkManager().deleteTunnel( this, tunnel.getTunnelName() );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException( String.format( "Failed to create tunnel: %s", e.getMessage() ), e );
        }
    }


    @Override
    public void importTemplate( final Template template, final String environmentId, final String kurjunToken )
            throws ResourceHostException
    {
        Preconditions.checkNotNull( template, "Invalid template" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );

        try
        {
            updateTemplateDownloadProgress( environmentId, template.getName(), 0 );

            commandUtil.execute( resourceHostCommands.getImportTemplateCommand( template.getId(), kurjunToken ), this,
                    new TemplateDownloadTracker( this, environmentId ) );
        }
        catch ( Exception e )
        {
            throw new ResourceHostException(
                    String.format( "Error importing template %s: %s", template.getName(), e.getMessage() ), e );
        }
    }


    @Override
    public RhTemplatesDownloadProgress getTemplateDownloadProgress( final String environmentId )
    {
        Map<String, Integer> templateDownloadPercent = envTemplatesDownloadPercent.getIfPresent( environmentId );

        return new RhTemplatesDownloadProgress( getId(),
                templateDownloadPercent == null ? Maps.<String, Integer>newHashMap() : templateDownloadPercent );
    }


    public void updateTemplateDownloadProgress( String environmentId, final String templateName,
                                                final int downloadPercent )
    {
        try
        {
            Map<String, Integer> templateDownloadPercent =

                    envTemplatesDownloadPercent.get( environmentId, new Callable<Map<String, Integer>>()
                    {
                        @Override
                        public Map<String, Integer> call() throws Exception
                        {

                            return Maps.newConcurrentMap();
                        }
                    } );


            templateDownloadPercent.put( templateName, downloadPercent );
        }
        catch ( ExecutionException e )
        {
            LOG.error( "Error updating template download progress", e );
        }
    }


    @Override
    public String cloneContainer( final Template template, final String containerName, final String hostname,
                                  final String ip, final int vlan, final String environmentId )
            throws ResourceHostException
    {
        Preconditions.checkNotNull( template, "Invalid template" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerName ), "Invalid container name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ), "Invalid ip" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ), "Invalid environment id" );
        Preconditions
                .checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ), "Invalid vlan" );


        try
        {
            //generate registration token for container for 30 min
            String containerToken = getRegistrationManager().generateContainerTTLToken( 30 * 60 * 1000L ).getToken();


            CommandResult result = execute( resourceHostCommands
                    .getCloneContainerCommand( template.getId(), containerName, hostname, ip, vlan, environmentId,
                            containerToken ) );

            //If container clone failed with message containing "{container} already exist", assume this result as
            // successful and skip the error. See https://github.com/optdyn/hub/issues/3268
            if ( !result.hasSucceeded() && !result.getStdOut()
                                                  .contains( String.format( "%s already exist", containerName ) ) )
            {
                throw new RuntimeException(
                        String.format( "Failed to clone container: %s, exit code %d", result.getStdErr(),
                                result.getExitCode() ) );
            }

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
                LOG.error( "Container ID not found in the output of subutai clone command" );

                throw new CommandException( "Container ID not found in the output of subutai clone command" );
            }

            return containerId;
        }
        catch ( Exception e )
        {
            throw new ResourceHostException(
                    String.format( "Error cloning container %s: %s", containerName, e.getMessage() ), e );
        }
    }


    @Override
    public void updateHostInfo( final HostInfo hostInfo )
    {
        super.updateHostInfo( hostInfo );

        setSavedHostInterfaces( hostInfo.getHostInterfaces() );

        ResourceHostInfo resourceHostInfo = ( ResourceHostInfo ) hostInfo;

        this.address = resourceHostInfo.getAddress();

        for ( ContainerHostInfo info : resourceHostInfo.getContainers() )
        {
            ContainerHostEntity containerHost;
            try
            {
                containerHost = ( ContainerHostEntity ) getContainerHostById( info.getId() );
                containerHost.updateHostInfo( info );
            }
            catch ( HostNotFoundException e )
            {

                //check that MH container is already registered
                boolean mhAlreadyRegistered = false;

                try
                {
                    LocalPeer localPeer = getLocalPeer();

                    localPeer.getManagementHost();

                    mhAlreadyRegistered = true;
                }
                catch ( Exception ex )
                {
                    //ignore
                }

                if ( !mhAlreadyRegistered && Common.MANAGEMENT_HOSTNAME.equals( info.getHostname() ) )
                {
                    try
                    {
                        containerHost =
                                new ContainerHostEntity( peerId, info.getId(), info.getHostname(), info.getArch(),
                                        info.getHostInterfaces(), info.getContainerName(),
                                        getLocalPeer().getTemplateByName( Common.MANAGEMENT_HOSTNAME ).getId(),
                                        Common.MANAGEMENT_HOSTNAME, null, null,
                                        new ContainerQuota( ContainerSize.SMALL ) );

                        addContainerHost( containerHost );
                    }
                    catch ( PeerException e1 )
                    {
                        LOG.warn( "Could not register management host, error obtaining management template info", e );
                    }
                }
                else
                {
                    LOG.warn( String.format( "Found not registered container host: %s %s", info.getId(),
                            info.getHostname() ) );
                }
            }
            catch ( Exception e )
            {
                LOG.warn( "Error updating container info {}", e.getMessage() );
            }
        }
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
            return getNetworkManager().getP2pVersion( this );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException( String.format( "Error obtaining P2P version: %s", e.getMessage() ), e );
        }
    }


    @Override
    public P2pLogs getP2pLogs( LogLevel logLevel, Date from, Date till ) throws ResourceHostException
    {
        Preconditions.checkNotNull( logLevel, "Invalid log level" );
        Preconditions.checkNotNull( from, "Invalid from date" );
        Preconditions.checkNotNull( till, "Invalid till date" );

        try
        {
            return getNetworkManager().getP2pLogs( this, logLevel, from, till );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException( String.format( "Error obtaining P2P logs: %s", e.getMessage() ), e );
        }
    }


    @Override
    public int getVlan() throws ResourceHostException
    {
        try
        {
            return Integer.parseInt(
                    commandUtil.execute( resourceHostCommands.getGetVlanCommand(), this ).getStdOut().trim() );
        }
        catch ( Exception e )
        {
            throw new ResourceHostException( String.format( "Error obtaining VLAN : %s", e.getMessage() ), e );
        }
    }


    @Override
    public void setContainerHostname( final ContainerHost containerHost, final String newHostname )
            throws ResourceHostException
    {
        Preconditions.checkNotNull( containerHost, "Invalid container" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newHostname ), "Invalid hostname" );

        //check if new hostname differs from current one
        if ( !StringUtils.equalsIgnoreCase( containerHost.getHostname(), newHostname ) )
        {
            try
            {
                commandUtil.execute( resourceHostCommands
                        .getSetContainerHostnameCommand( containerHost.getContainerName(), newHostname ), this );
            }
            catch ( CommandException e )
            {
                throw new ResourceHostException(
                        String.format( "Error setting container hostname: %s", e.getMessage() ), e );
            }
        }
    }


    @Override
    public void setHostname( final String newHostname ) throws ResourceHostException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newHostname ), "Invalid hostname" );

        if ( !StringUtils.equalsIgnoreCase( this.hostname, newHostname ) )
        {
            try
            {
                commandUtil.execute( resourceHostCommands.getGetSetRhHostnameCommand( newHostname ), this );

                this.hostname = newHostname; //not updating db record b/c heartbeat will handle that
            }
            catch ( CommandException e )
            {
                throw new ResourceHostException(
                        String.format( "Error setting resource host hostname: %s", e.getMessage() ), e );
            }
        }
    }


    @Override
    public void promoteTemplate( final String containerName, final String templateName ) throws ResourceHostException
    {
        try
        {
            commandUtil.execute( resourceHostCommands.getPromoteTemplateCommand( containerName, templateName ), this );
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException(
                    String.format( "Error promoting container to template: %s", e.getMessage() ), e );
        }
    }


    @Override
    public String exportTemplate( final String templateName, final boolean isPrivateTemplate, final String token )
            throws ResourceHostException
    {
        try
        {
            CommandResult result = commandUtil
                    .execute( resourceHostCommands.getExportTemplateCommand( templateName, isPrivateTemplate, token ),
                            this );

            Pattern p = Pattern.compile( "hash:\\s+(\\S+)\\s*\"" );

            Matcher m = p.matcher( result.getStdOut() );

            if ( m.find() && m.groupCount() == 1 )
            {
                return m.group( 1 );
            }
            else
            {
                throw new ResourceHostException( "Template hash is not found in the output of subutai export command" );
            }
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( String.format( "Error exporting template: %s", e.getMessage() ), e );
        }
    }


    @Override
    public boolean isManagementHost()
    {
        try
        {
            return getLocalPeer().getManagementHost().getId().equals( getId() );
        }
        catch ( HostNotFoundException e )
        {
            return false;
        }
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
        return PermissionObject.RESOURCE_MANAGEMENT.getName();
    }


    @Override
    public String getKeyId()
    {
        return getId();
    }


    @Override
    public Set<String> listExistingContainerNames() throws ResourceHostException
    {

        Set<String> containerNames = Sets.newHashSet();
        try
        {
            CommandResult result = commandUtil.execute( resourceHostCommands.getListContainersCommand(), this );

            StringTokenizer tokenizer = new StringTokenizer( result.getStdOut(), System.lineSeparator() );

            int i = 0;
            while ( tokenizer.hasMoreTokens() )
            {
                String token = tokenizer.nextToken();
                //skip header
                if ( i > 1 )
                {
                    containerNames.add( token );
                }
                i++;
            }
        }
        catch ( CommandException e )
        {
            throw new ResourceHostException( String.format( "Error obtaining list of containers %s", e.getMessage() ),
                    e );
        }


        return containerNames;
    }


    @Override
    public ReservedPorts getReservedPorts() throws ResourceHostException
    {
        try
        {
            return getNetworkManager().getReservedPorts( this );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException( String.format( "Failed to get reserved ports: %s", e.getMessage() ), e );
        }
    }


    @Override
    public ReservedPorts getContainerPortMappings( final Protocol protocol ) throws ResourceHostException
    {
        try
        {
            return getNetworkManager().getContainerPortMappings( this, protocol );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException( String.format( "Failed to get reserved ports: %s", e.getMessage() ), e );
        }
    }


    @Override
    public int mapContainerPort( final Protocol protocol, final String containerIp, final int containerPort )
            throws ResourceHostException
    {
        try
        {
            return getNetworkManager().mapContainerPort( this, protocol, containerIp, containerPort );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException( String.format( "Failed to map container port %s", e.getMessage() ), e );
        }
    }


    @Override
    public void mapContainerPort( final Protocol protocol, final String containerIp, final int containerPort,
                                  final int rhPort ) throws ResourceHostException
    {
        try
        {
            getNetworkManager().mapContainerPort( this, protocol, containerIp, containerPort, rhPort );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException( String.format( "Failed to map container port %s", e.getMessage() ), e );
        }
    }


    @Override
    public void removeContainerPortMapping( final Protocol protocol, final String containerIp, final int containerPort,
                                            final int rhPort ) throws ResourceHostException
    {
        try
        {
            getNetworkManager().removeContainerPortMapping( this, protocol, containerIp, containerPort, rhPort );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException(
                    String.format( "Failed to remove container port mapping %s", e.getMessage() ), e );
        }
    }


    @Override
    public void mapContainerPortToDomain( final Protocol protocol, final String containerIp, final int containerPort,
                                          final int rhPort, final String domain, final String sslCertPath,
                                          final LoadBalancing loadBalancing, final boolean sslBackend )
            throws ResourceHostException
    {
        try
        {
            getNetworkManager()
                    .mapContainerPortToDomain( this, protocol, containerIp, containerPort, rhPort, domain, sslCertPath,
                            loadBalancing, sslBackend );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException(
                    String.format( "Failed to map container port to domain %s", e.getMessage() ), e );
        }
    }


    @Override
    public void removeContainerPortDomainMapping( final Protocol protocol, final String containerIp,
                                                  final int containerPort, final int rhPort, final String domain )
            throws ResourceHostException
    {
        try
        {
            getNetworkManager()
                    .removeContainerPortDomainMapping( this, protocol, containerIp, containerPort, rhPort, domain );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException(
                    String.format( "Failed to remove container port domain mapping %s", e.getMessage() ), e );
        }
    }


    @Override
    public boolean isPortMappingReserved( final Protocol protocol, final int externalPort, final String ipAddress,
                                          final int internalPort ) throws ResourceHostException
    {
        try
        {
            return getNetworkManager().isPortMappingReserved( this, protocol, externalPort, ipAddress, internalPort );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException(
                    String.format( "Failed to check port mapping existence: %s", e.getMessage() ), e );
        }
    }


    @Override
    public List<ReservedPortMapping> getReservedPortMappings() throws ResourceHostException
    {
        try
        {
            return getNetworkManager().getReservedPortMappings( this );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException( String.format( "Failed to get port mapping list: %s", e.getMessage() ),
                    e );
        }
    }


    @Override
    public String getIp() throws ResourceHostException
    {
        try
        {
            return getNetworkManager().getResourceHostIp( this );
        }
        catch ( NetworkManagerException e )
        {
            throw new ResourceHostException( String.format( "Failed to get ip: %s", e.getMessage() ), e );
        }
    }
}
