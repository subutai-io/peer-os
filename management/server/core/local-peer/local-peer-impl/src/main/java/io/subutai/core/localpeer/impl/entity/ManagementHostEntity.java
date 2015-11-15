package io.subutai.core.localpeer.impl.entity;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
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
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.host.HostId;
import io.subutai.common.host.Interface;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.mdc.SubutaiExecutors;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.network.VniVlanMapping;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.protocol.Disposable;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.NumUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.localpeer.impl.tasks.CreateGatewayTask;
import io.subutai.core.localpeer.impl.tasks.ReserveVniTask;
import io.subutai.core.localpeer.impl.tasks.SetupTunnelsTask;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.common.peer.ManagementHost;
import io.subutai.common.protocol.Tunnel;
import io.subutai.core.repository.api.RepositoryException;
import io.subutai.core.repository.api.RepositoryManager;


@Entity
@Table( name = "m_host" )
@Access( AccessType.FIELD )
public class ManagementHostEntity extends AbstractSubutaiHost implements ManagementHost, Disposable
{
    private static final Logger LOG = LoggerFactory.getLogger( ManagementHostEntity.class );

    private static final String GATEWAY_INTERFACE_NAME_REGEX = "^br-(\\d+)$";
    private static final Pattern GATEWAY_INTERFACE_NAME_PATTERN = Pattern.compile( GATEWAY_INTERFACE_NAME_REGEX );


    @Column
    private String name = "Subutai Management Host";

    @Lob
    @Column
    private String peerInfo;

    @Transient
    protected ExecutorService singleThreadExecutorService = SubutaiExecutors.newSingleThreadExecutor();
    @Transient
    protected ServiceLocator serviceLocator = new ServiceLocator();


    protected ManagementHostEntity()
    {
    }


    public ManagementHostEntity( final String peerId, final ResourceHostInfo resourceHostInfo )
    {
        super( peerId, resourceHostInfo );
    }


    public void init()
    {
        // empty
    }


    public void dispose()
    {
        singleThreadExecutorService.shutdown();
    }


    @Override
    public String getExternalIp()
    {
        return getPeer().getPeerInfo().getIp();
    }


    @Override
    public String getVlanDomain( final int vlan ) throws PeerException
    {
        try
        {
            return getNetworkManager().getVlanDomain( vlan );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( String.format( "Error obtaining domain by vlan %d", vlan ), e );
        }
    }


    @Override
    public void removeVlanDomain( final int vlan ) throws PeerException
    {
        try
        {
            getNetworkManager().removeVlanDomain( vlan );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( String.format( "Error removing domain by vlan %d", vlan ), e );
        }
    }


    @Override
    public void setVlanDomain( final int vlan, final String domain,
                               final DomainLoadBalanceStrategy domainLoadBalanceStrategy, final String sslCertPath )
            throws PeerException
    {
        try
        {
            getNetworkManager().setVlanDomain( vlan, domain, domainLoadBalanceStrategy, sslCertPath );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( String.format( "Error setting domain by vlan %d", vlan ), e );
        }
    }


    @Override
    public boolean isIpInVlanDomain( final String hostIp, final int vlan ) throws PeerException
    {
        try
        {
            return getNetworkManager().isIpInVlanDomain( hostIp, vlan );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( String.format( "Error checking domain by ip %s and vlan %d", hostIp, vlan ), e );
        }
    }


    @Override
    public void addIpToVlanDomain( final String hostIp, final int vlan ) throws PeerException
    {
        try
        {
            getNetworkManager().addIpToVlanDomain( hostIp, vlan );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( String.format( "Error adding ip %s to domain by vlan %d", hostIp, vlan ), e );
        }
    }


    @Override
    public void removeIpFromVlanDomain( final String hostIp, final int vlan ) throws PeerException
    {
        try
        {
            getNetworkManager().removeIpFromVlanDomain( hostIp, vlan );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( String.format( "Error removing ip %s from domain by vlan %d", hostIp, vlan ), e );
        }
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


    public void addRepository( final String ip ) throws PeerException
    {
        try
        {
            getRepositoryManager().addRepository( ip );
        }
        catch ( RepositoryException e )
        {
            //            throw new PeerException( "Error adding repository", e );
            LOG.error( "Error adding repository", e );
        }
    }


    public void removeRepository( final String host, final String ip ) throws PeerException
    {
        try
        {
            getRepositoryManager().removeRepository( ip );
        }
        catch ( RepositoryException e )
        {
            //            throw new PeerException( "Error removing repository", e );
            LOG.error( "Error removing repository", e );
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


    public void cleanupEnvironmentNetworkSettings( final EnvironmentId environmentId ) throws PeerException
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
    public void removeTunnel( final String tunnelIp )
    {
        try
        {
            SubnetUtils.SubnetInfo subnetInfo = new SubnetUtils( tunnelIp, "255.255.255.0" ).getInfo();
            Set<Tunnel> tunnels = listTunnels();
            LOG.debug( String.format( "Found %d tunnels.", tunnels.size() ) );
            for ( final Tunnel tunnel : tunnels )
            {
                if ( subnetInfo.isInRange( tunnel.getTunnelIp() ) )
                {
                    getNetworkManager().removeTunnel( tunnel.getTunnelId() );
                    LOG.debug( String.format( "Tunnel '%s' destroyed successfully.", tunnel.getTunnelName() ) );
                }
            }
        }
        catch ( PeerException | NetworkManagerException e )
        {
            LOG.warn( "Error removing tunnel", e );
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

        for ( Interface iface : getInterfaces() )
        {
            Matcher matcher = GATEWAY_INTERFACE_NAME_PATTERN.matcher( iface.getName().trim() );
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
    public Vni reserveVni( final Vni vni ) throws PeerException
    {
        Preconditions.checkNotNull( vni, "Invalid vni" );

        //need to execute sequentially since other parallel executions can take the same VNI
        Future<Vni> future = queueSequentialTask( new ReserveVniTask( getNetworkManager(), vni, this ) );

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
    public int setupTunnels( final Map<String, String> peerIps, final String environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( peerIps, "Invalid peer ips set" );
        Preconditions.checkArgument( !peerIps.isEmpty(), "Invalid peer ips set" );
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


    @Override
    public Vni findVniByEnvironmentId( String environmentId ) throws PeerException
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


    @Override
    public void setupVniVlanMapping( final int tunnelId, final long vni, final int vlanId, final String environmentId )
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


    @Override
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


    @Override
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


    @Override
    public void setupN2NConnection( final N2NConfig config ) throws PeerException
    {
        try
        {
            getNetworkManager()
                    .setupN2NConnection( config.getSuperNodeIp(), config.getN2NPort(), config.getInterfaceName(),
                            config.getCommunityName(), config.getAddress(), NetworkManager.N2N_STRING_KEY,
                            config.getSharedKey() );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( "Unable add host to n2n tunnel.", e );
        }
    }


    @Override
    public void removeN2NConnection( final N2NConfig config )
    {
        try
        {
            getNetworkManager().removeN2NConnection( config.getInterfaceName(), config.getCommunityName() );
        }
        catch ( PeerException | NetworkManagerException e )
        {
            LOG.warn( "Unable remove host from n2n tunnel.", e );
        }
    }


    @Override
    public boolean isConnected()
    {
        return getPeer().isConnected( new HostId( getId() ) );
    }


    public void setPeerInfo( final PeerInfo peerInfo )
    {
        this.peerInfo = JsonUtil.toJson( peerInfo );
    }


    public PeerInfo getPeerInfo()
    {
        return JsonUtil.fromJson( peerInfo, PeerInfo.class );
    }
}
