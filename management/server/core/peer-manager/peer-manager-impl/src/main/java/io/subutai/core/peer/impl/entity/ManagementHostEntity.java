package io.subutai.core.peer.impl.entity;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.host.Interface;
import io.subutai.common.mdc.SubutaiExecutors;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.network.VniVlanMapping;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.Disposable;
import io.subutai.common.settings.Common;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.NumUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.hostregistry.api.ResourceHostInfo;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.network.api.Tunnel;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.impl.tasks.CreateGatewayTask;
import io.subutai.core.peer.impl.tasks.ReserveVniTask;
import io.subutai.core.peer.impl.tasks.SetupTunnelsTask;
import io.subutai.core.repository.api.RepositoryException;
import io.subutai.core.repository.api.RepositoryManager;


@Entity
@Table( name = "management_host" )
@Access( AccessType.FIELD )
public class ManagementHostEntity extends AbstractSubutaiHost implements ManagementHost, Disposable
{
    private static final Logger LOG = LoggerFactory.getLogger( ManagementHostEntity.class.getName() );

    private static final String GATEWAY_INTERFACE_NAME_REGEX = "^br-(\\d+)$";
    private static final Pattern GATEWAY_INTERFACE_NAME_PATTERN = Pattern.compile( GATEWAY_INTERFACE_NAME_REGEX );


    @Column
    String name = "Subutai Management Host";

    @Transient
    protected ExecutorService singleThreadExecutorService = SubutaiExecutors.newSingleThreadExecutor();
    @Transient
    protected ServiceLocator serviceLocator = new ServiceLocator();

    @Transient
    private String externalIpInterface;


    protected ManagementHostEntity()
    {
    }


    public ManagementHostEntity( final String peerId, final ResourceHostInfo resourceHostInfo,
                                 final String externalIpInterface )
    {
        super( peerId, resourceHostInfo );
        this.externalIpInterface = externalIpInterface;
    }


    public void init()
    {
        //for future use
    }


    public void dispose()
    {
        singleThreadExecutorService.shutdown();
    }


    @Override
    public String getExternalIp()
    {
        return getIpByInterfaceName( externalIpInterface );
    }


    public <T> Future<T> queueSequentialTask( Callable<T> callable )
    {
        return singleThreadExecutorService.submit( callable );
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public void addAptSource( final String hostname, final String ip ) throws PeerException
    {
        try
        {
            getRepositoryManager().addAptSource( hostname, ip );
        }
        catch ( RepositoryException e )
        {
            //            throw new PeerException( "Error adding apt source", e );
            LOG.error( "Error adding apt source", e );
        }
    }


    public void removeAptSource( final String host, final String ip ) throws PeerException
    {
        try
        {
            getRepositoryManager().removeAptSource( ip );
        }
        catch ( RepositoryException e )
        {
            //            throw new PeerException( "Error removing apt source", e );
            LOG.error( "Error removing apt source", e );
        }
    }


    @Override
    public String readFile( final String path ) throws IOException
    {
        byte[] encoded = Files.readAllBytes( Paths.get( path ) );
        return new String( encoded, Charset.defaultCharset() );
    }


    @Override
    public void removeGateway( final int vlan ) throws PeerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ),
                String.format( "VLAN must be in the range from %d to %d", Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );

        try
        {
            getNetworkManager().removeGateway( vlan );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( String.format( "Error removing gateway tap device with VLAN %d", vlan ), e );
        }
    }


    public void cleanupEnvironmentNetworkSettings( final UUID environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        try
        {
            getNetworkManager().cleanupEnvironmentNetworkSettings( environmentId );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException(
                    String.format( "Error cleaning up environment %s network settings", environmentId ), e );
        }
    }


    protected Set<Tunnel> listTunnels() throws PeerException
    {
        try
        {
            return getNetworkManager().listTunnels();
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( "Error retrieving peer tunnels", e );
        }
    }


    @Override
    public void removeTunnel( final String peerIp ) throws PeerException
    {
        try
        {
            Set<Tunnel> tunnels = listTunnels();
            for ( final Tunnel tunnel : tunnels )
            {
                if ( tunnel.getTunnelIp().equalsIgnoreCase( peerIp ) )
                {
                    getNetworkManager().removeTunnel( tunnel.getTunnelId() );
                    break;
                }
            }
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( "Error removing tunnel", e );
        }
    }


    protected NetworkManager getNetworkManager() throws PeerException
    {
        try
        {
            return serviceLocator.getService( NetworkManager.class );
        }
        catch ( NamingException e )
        {
            throw new PeerException( e );
        }
    }


    protected RepositoryManager getRepositoryManager() throws PeerException
    {
        try
        {
            return serviceLocator.getService( RepositoryManager.class );
        }
        catch ( NamingException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public Set<Vni> getReservedVnis() throws PeerException
    {
        try
        {
            return getNetworkManager().getReservedVnis();
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public Set<Gateway> getGateways() throws PeerException
    {
        Set<Gateway> gateways = Sets.newHashSet();

        for ( Interface iface : interfaces )
        {
            Matcher matcher = GATEWAY_INTERFACE_NAME_PATTERN.matcher( iface.getInterfaceName().trim() );
            if ( matcher.find() )
            {
                int vlan = Integer.parseInt( matcher.group( 1 ) );
                String ip = iface.getIp();

                gateways.add( new Gateway( vlan, ip ) );
            }
        }

        return gateways;
    }


    @Override
    public void createGateway( final String gatewayIp, final int vlan ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( gatewayIp ) && gatewayIp.matches( Common.IP_REGEX ),
                "Invalid gateway IP" );
        Preconditions.checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ),
                String.format( "VLAN must be in the range from %d to %d", Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );

        //need to execute sequentially since other parallel executions can take the same gateway
        Future<Boolean> future =
                queueSequentialTask( new CreateGatewayTask( gatewayIp, vlan, getNetworkManager(), this ) );

        try
        {
            future.get();
        }
        catch ( InterruptedException e )
        {
            throw new PeerException( e );
        }
        catch ( ExecutionException e )
        {
            if ( e.getCause() instanceof PeerException )
            {
                throw ( PeerException ) e.getCause();
            }
            throw new PeerException( "Error creating gateway", e.getCause() );
        }
    }


    @Override
    public int reserveVni( final Vni vni ) throws PeerException
    {
        Preconditions.checkNotNull( vni, "Invalid vni" );

        //need to execute sequentially since other parallel executions can take the same VNI
        Future<Integer> future = queueSequentialTask( new ReserveVniTask( getNetworkManager(), vni, this ) );

        try
        {
            return future.get();
        }
        catch ( InterruptedException e )
        {
            throw new PeerException( e );
        }
        catch ( ExecutionException e )
        {
            if ( e.getCause() instanceof PeerException )
            {
                throw ( PeerException ) e.getCause();
            }
            throw new PeerException( "Error reserving VNI", e.getCause() );
        }
    }


    @Override
    public int setupTunnels( final Set<String> peerIps, final UUID environmentId ) throws PeerException
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( peerIps ), "Invalid peer ips set" );
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        //need to execute sequentially since other parallel executions can setup the same tunnel
        Future<Integer> future =
                queueSequentialTask( new SetupTunnelsTask( getNetworkManager(), this, environmentId, peerIps ) );

        try
        {
            return future.get();
        }
        catch ( InterruptedException e )
        {
            throw new PeerException( e );
        }
        catch ( ExecutionException e )
        {
            if ( e.getCause() instanceof PeerException )
            {
                throw ( PeerException ) e.getCause();
            }
            throw new PeerException( "Error setting up tunnels", e.getCause() );
        }
    }


    public Vni findVniByEnvironmentId( UUID environmentId ) throws PeerException
    {
        //check if vni is already reserved
        for ( Vni aVni : getReservedVnis() )
        {
            if ( aVni.getEnvironmentId().equals( environmentId ) )
            {
                return aVni;
            }
        }

        return null;
    }


    public void setupVniVlanMapping( final int tunnelId, final long vni, final int vlanId, final UUID environmentId )
            throws PeerException
    {
        try
        {
            Set<VniVlanMapping> mappings = getNetworkManager().getVniVlanMappings();

            for ( VniVlanMapping mapping : mappings )
            {
                if ( mapping.getTunnelId() == tunnelId && mapping.getEnvironmentId().equals( environmentId ) )
                {
                    return;
                }
            }

            getNetworkManager().setupVniVLanMapping( tunnelId, vni, vlanId, environmentId );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( e );
        }
    }


    public int findTunnel( String peerIp, Set<Tunnel> tunnels )
    {
        for ( Tunnel tunnel : tunnels )
        {
            if ( tunnel.getTunnelIp().equals( peerIp ) )
            {
                return tunnel.getTunnelId();
            }
        }

        return -1;
    }


    public int calculateNextTunnelId( Set<Tunnel> tunnels )
    {
        int maxTunnelId = 0;
        for ( Tunnel tunnel : tunnels )
        {
            if ( tunnel.getTunnelId() > maxTunnelId )
            {
                maxTunnelId = tunnel.getTunnelId();
            }
        }

        return maxTunnelId + 1;
    }


    public int findAvailableVlanId() throws PeerException
    {
        SortedSet<Integer> takenIds = Sets.newTreeSet();

        for ( Vni vni : getReservedVnis() )
        {
            takenIds.add( vni.getVlan() );
        }

        for ( int i = Common.MIN_VLAN_ID; i <= Common.MAX_VLAN_ID; i++ )
        {
            if ( !takenIds.contains( i ) )
            {
                return i;
            }
        }

        throw new PeerException( "No available vlan found" );
    }
}
