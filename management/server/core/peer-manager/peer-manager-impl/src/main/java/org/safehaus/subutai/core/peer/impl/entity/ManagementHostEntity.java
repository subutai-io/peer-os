package org.safehaus.subutai.core.peer.impl.entity;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.naming.NamingException;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.network.VniVlanMapping;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.NumUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.network.api.Tunnel;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.impl.Commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


@Entity
@Table( name = "management_host" )
@Access( AccessType.FIELD )
public class ManagementHostEntity extends AbstractSubutaiHost implements ManagementHost
{
    @Column
    String name = "Subutai Management Host";

    @Transient
    private Commands commands;
    @Transient
    private CommandUtil commandUtil;
    @Transient
    private ExecutorService singleThreadExecutorService;
    @Transient
    ServiceLocator serviceLocator;


    protected ManagementHostEntity()
    {
    }


    public ManagementHostEntity( final String peerId, final ResourceHostInfo resourceHostInfo )
    {
        super( peerId, resourceHostInfo );
    }


    public void init()
    {
        this.commands = new Commands();
        this.commandUtil = new CommandUtil();
        this.singleThreadExecutorService = Executors.newSingleThreadExecutor();
        this.serviceLocator = new ServiceLocator();
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
            commandUtil.execute( commands.getAddAptSourceCommand( hostname, ip ), this );
        }
        catch ( CommandException e )
        {
            throw new PeerException( "Could not add remote host as apt source", e.toString() );
        }
    }


    public void removeAptSource( final String host, final String ip ) throws PeerException
    {
        try
        {
            commandUtil.execute( commands.getRemoveAptSourceCommand( ip ), this );
        }
        catch ( CommandException e )
        {
            throw new PeerException( "Could not add remote host as apt source", e.toString() );
        }
    }


    @Override
    public String readFile( final String path ) throws IOException
    {
        byte[] encoded = Files.readAllBytes( Paths.get( path ) );
        return new String( encoded, Charset.defaultCharset() );
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


    @Override
    public Set<Long> getTakenVniIds() throws PeerException
    {
        try
        {
            SortedSet<Long> vniIds = Sets.newTreeSet();
            Set<VniVlanMapping> mappings = getNetworkManager().getVniVlanMappings();
            for ( VniVlanMapping mapping : mappings )
            {
                vniIds.add( mapping.getVni() );
            }
            return vniIds;
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public int setupTunnels( final Set<String> peerIps, final long vni, final boolean newVni ) throws PeerException
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( peerIps ), "Invalid peer ips set" );
        Preconditions.checkArgument( NumUtil.isLongBetween( vni, NetworkManager.MIN_VNI_ID, NetworkManager.MAX_VNI_ID ),
                String.format( "Vni id must be in range %d - %d", NetworkManager.MIN_VNI_ID,
                        NetworkManager.MAX_VNI_ID ) );

        //need to execute sequentially since other parallel executions can take the same VNI
        Future<Integer> future = queueSequentialTask( new Callable<Integer>()
        {
            @Override
            public Integer call() throws Exception
            {

                NetworkManager networkManager = getNetworkManager();

                Set<VniVlanMapping> mappings = networkManager.getVniVlanMappings();

                //check if vni is free
                if ( newVni && !isAvailableVniId( vni, mappings ) )
                {
                    throw new PeerException( String.format( "Vni %d is already used", vni ) );
                }
                //check if vni exists
                else if ( !newVni && isAvailableVniId( vni, mappings ) )
                {
                    throw new PeerException( String.format( "Vni %d is not found", vni ) );
                }

                int vlanId = newVni ? findAvailableVlanId( mappings ) : findVlanByVni( vni, mappings );

                Set<Tunnel> tunnels = networkManager.listTunnels();

                for ( String peerIp : peerIps )
                {
                    int tunnelId = findTunnel( peerIp, tunnels );
                    //tunnel not found, create new one
                    if ( tunnelId == 0 )
                    {
                        //calculate tunnel id
                        tunnelId = calculateNextTunnelId( tunnels );

                        //create tunnel
                        networkManager.setupTunnel( tunnelId, peerIp );
                    }

                    //create vni-vlan mapping
                    setupVniVlanMapping( tunnelId, vni, vlanId, mappings );
                }

                return vlanId;
            }
        } );

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
            throw new PeerException( e.getCause() );
        }
    }


    private void setupVniVlanMapping( final int tunnelId, final long vni, final int vlanId,
                                      final Set<VniVlanMapping> mappings ) throws PeerException, NetworkManagerException
    {
        for ( VniVlanMapping mapping : mappings )
        {
            if ( mapping.getTunnelId() == tunnelId && mapping.getVni() == vni && mapping.getVlan() == vlanId )
            {
                return;
            }
        }

        getNetworkManager().setupVniVLanMapping( tunnelId, vni, vlanId );
    }


    protected int findVlanByVni( long vni, Set<VniVlanMapping> mappings ) throws PeerException
    {

        for ( VniVlanMapping mapping : mappings )
        {
            if ( mapping.getVni() == vni )
            {
                return mapping.getVlan();
            }
        }

        throw new PeerException( String.format( "Vlan not found by vni %d", vni ) );
    }


    protected int findTunnel( String peerIp, Set<Tunnel> tunnels )
    {
        for ( Tunnel tunnel : tunnels )
        {
            if ( tunnel.getTunnelIp().equals( peerIp ) )
            {
                return tunnel.getTunnelId();
            }
        }

        return 0;
    }


    protected int calculateNextTunnelId( Set<Tunnel> tunnels )
    {
        int maxTunnelId = 1;
        for ( Tunnel tunnel : tunnels )
        {
            if ( tunnel.getTunnelId() > maxTunnelId )
            {
                maxTunnelId = tunnel.getTunnelId();
            }
        }

        return maxTunnelId + 1;
    }


    protected int findAvailableVlanId( Set<VniVlanMapping> mappings )
    {
        SortedSet<Integer> takenIds = Sets.newTreeSet();

        int maxId = NetworkManager.MIN_VLAN_ID;

        for ( VniVlanMapping mapping : mappings )
        {
            takenIds.add( mapping.getVlan() );
            if ( mapping.getVlan() > maxId )
            {
                maxId = mapping.getVlan();
            }
        }

        for ( int i = NetworkManager.MIN_VLAN_ID; i <= NetworkManager.MAX_VLAN_ID; i++ )
        {
            if ( !takenIds.contains( i ) )
            {
                return i;
            }
        }

        if ( !NumUtil.isIntBetween( maxId + 1, NetworkManager.MIN_VLAN_ID, NetworkManager.MAX_VLAN_ID ) )
        {
            throw new IllegalArgumentException(
                    String.format( "Next VLAN id exceeds possible range %d - %d", NetworkManager.MIN_VLAN_ID,
                            NetworkManager.MAX_VLAN_ID ) );
        }

        return maxId + 1;
    }


    protected boolean isAvailableVniId( long vni, Set<VniVlanMapping> mappings )
    {
        if ( !NumUtil.isLongBetween( vni, NetworkManager.MIN_VNI_ID, NetworkManager.MAX_VNI_ID ) )
        {
            throw new IllegalArgumentException(
                    String.format( "VNI id %d exceeds possible range %d - %d", vni, NetworkManager.MIN_VLAN_ID,
                            NetworkManager.MAX_VLAN_ID ) );
        }

        for ( VniVlanMapping mapping : mappings )
        {
            if ( mapping.getVni() == vni )
            {
                return false;
            }
        }

        return true;
    }
}
