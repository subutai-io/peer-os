package org.safehaus.subutai.core.peer.impl.entity;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.network.api.Tunnel;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.impl.Commands;


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
    private NetworkManager networkManager;
    @Transient
    private ExecutorService singleThreadExecutorService;


    protected ManagementHostEntity()
    {
    }


    public ManagementHostEntity( final String peerId, final ResourceHostInfo resourceHostInfo,
                                 final NetworkManager networkManager )
    {
        super( peerId, resourceHostInfo );
        this.networkManager = networkManager;
    }


    public void init()
    {
        this.commands = new Commands();
        this.commandUtil = new CommandUtil();
        this.singleThreadExecutorService = Executors.newSingleThreadExecutor();
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


    @Override
    public VniVlanMapping reserveVniVlanMapping( String remotePeerIp ) throws PeerException
    {
        /*
            TODO execute this in single threaded executor
            1) obtain used mappings
            2) reserve new mapping
         */

        try
        {
            //figure out max tunnel id
            Set<Tunnel> tunnels = networkManager.listTunnels();

            int maxTunnelId = 0;

            for ( Tunnel tunnel : tunnels )
            {
                int tunnelId = Integer.parseInt( tunnel.getTunnelName().replace( "tunnel", "" ).trim() );
                if ( tunnelId > maxTunnelId )
                {
                    maxTunnelId = tunnelId;
                }
            }

            //figure out max vlan and vni
            Set<VniVlanMapping> mappings = networkManager.getVniVlanMappings();

            int maxVlan = 0;
            long maxVni = 0;

            for ( VniVlanMapping mapping : mappings )
            {
                if ( mapping.getVlan() > maxVlan )
                {
                    maxVlan = mapping.getVlan();
                }
                if ( mapping.getVni() > maxVni )
                {
                    maxVni = mapping.getVni();
                }
            }

            //TODO check boundaries of vlan 100 - 4096, vni 0 - 16 M & throw exception

            //create tunnel
            networkManager.setupTunnel( maxTunnelId + 1, remotePeerIp );

            //create vni-vlan mapping

            networkManager.setupVniVLanMapping( maxTunnelId + 1, maxVni + 1, maxVlan + 1 );

            return new VniVlanMapping( String.format( "%s%d", NetworkManager.TUNNEL_PREFIX, maxTunnelId + 1 ),
                    maxVni + 1, maxVlan + 1 );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( e );
        }
    }
}
