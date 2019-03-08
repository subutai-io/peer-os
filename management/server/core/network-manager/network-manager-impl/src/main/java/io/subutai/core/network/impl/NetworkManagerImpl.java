package io.subutai.core.network.impl;


import java.time.Instant;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.network.SshTunnel;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.LoadBalancing;
import io.subutai.common.protocol.P2PConnection;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.Protocol;
import io.subutai.common.protocol.ReservedPort;
import io.subutai.common.protocol.ReservedPorts;
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.protocol.Tunnels;
import io.subutai.common.settings.Common;
import io.subutai.common.util.NumUtil;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.peer.api.PeerManager;


/**
 * Implementation of Network Manager
 */
public class NetworkManagerImpl implements NetworkManager
{
    private static final String LINE_DELIMITER = System.lineSeparator();
    private final PeerManager peerManager;
    protected Commands commands = new Commands();


    public NetworkManagerImpl( final PeerManager peerManager )
    {
        Preconditions.checkNotNull( peerManager );

        this.peerManager = peerManager;
    }


    //---------------- P2P SECTION BEGIN ------------------------


    @Override
    public void joinP2PSwarm( final Host host, final String interfaceName, final String localIp, final String p2pHash,
                              final String secretKey, final long secretKeyTtlSec ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !StringUtils.isBlank( interfaceName ), "Invalid interface name" );
        Preconditions.checkArgument( !StringUtils.isBlank( p2pHash ), "Invalid P2P hash" );
        Preconditions.checkArgument( !StringUtils.isBlank( localIp ), "Invalid ip" );
        Preconditions.checkArgument( localIp.matches( Common.IP_REGEX ), "Invalid ip" );
        Preconditions.checkArgument( !StringUtils.isBlank( secretKey ), "Invalid secret key" );
        Preconditions.checkArgument( secretKeyTtlSec > 0, "Invalid time-to-live" );


        execute( host, commands.getJoinP2PSwarmCommand( interfaceName, localIp, p2pHash, secretKey,
                getUnixTimestampOffset( secretKeyTtlSec ),
                String.format( "%s-%s", Common.P2P_PORT_RANGE_START, Common.P2P_PORT_RANGE_END ) ) );
    }


    @Override
    public void joinP2PSwarmDHCP( final Host host, final String interfaceName, final String p2pHash,
                                  final String secretKey, final long secretKeyTtlSec ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !StringUtils.isBlank( interfaceName ), "Invalid interface name" );
        Preconditions.checkArgument( !StringUtils.isBlank( p2pHash ), "Invalid P2P hash" );
        Preconditions.checkArgument( !StringUtils.isBlank( secretKey ), "Invalid secret key" );
        Preconditions.checkArgument( secretKeyTtlSec > 0, "Invalid time-to-live" );


        execute( host, commands.getJoinP2PSwarmDHCPCommand( interfaceName, p2pHash, secretKey,
                getUnixTimestampOffset( secretKeyTtlSec ),
                String.format( "%s-%s", Common.P2P_PORT_RANGE_START, Common.P2P_PORT_RANGE_END ) ) );
    }


    @Override
    public void removeP2PSwarm( final Host host, String p2pHash ) throws NetworkManagerException
    {
        Preconditions.checkArgument( !StringUtils.isBlank( p2pHash ), "Invalid P2P hash" );

        execute( host, commands.getRemoveP2PSwarmCommand( p2pHash ) );
    }


    @Override
    public void removeP2PIface( Host host, String interfaceName ) throws NetworkManagerException
    {
        Preconditions.checkArgument( !StringUtils.isBlank( interfaceName ), "Invalid interface name" );

        execute( host, commands.getRemoveP2PIfaceCommand( interfaceName ) );
    }


    @Override
    public void resetSwarmSecretKey( final Host host, final String p2pHash, final String newSecretKey,
                                     final long ttlSeconds ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host );
        Preconditions.checkArgument( !StringUtils.isBlank( p2pHash ), "Invalid P2P hash" );
        Preconditions.checkArgument( !StringUtils.isBlank( newSecretKey ), "Invalid secret key" );
        Preconditions.checkArgument( ttlSeconds > 0, "Invalid time-to-live" );

        execute( host, commands.getResetP2PSecretKey( p2pHash, newSecretKey, getUnixTimestampOffset( ttlSeconds ) ) );
    }


    @Override
    public P2PConnections getP2PConnections( final Host host ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );

        P2PConnections connections = new P2PConnections();

        CommandResult result = execute( host, commands.getP2PConnectionsCommand() );

        StringTokenizer st = new StringTokenizer( result.getStdOut(), LINE_DELIMITER );

        Pattern p = Pattern.compile( "\\s*(\\S+)\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\s+(\\S+)\\s*" );

        while ( st.hasMoreTokens() )
        {
            Matcher m = p.matcher( st.nextToken() );

            if ( m.find() && m.groupCount() == 3 )
            {
                connections.addConnection( new P2PConnection( m.group( 1 ), m.group( 2 ), m.group( 3 ) ) );
            }
        }

        return connections;
    }


    @Override
    public String getP2pVersion( final Host host ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );

        CommandResult result = execute( host, commands.getGetP2pVersionCommand() );

        String p2pVersion = result.getStdOut();
        if ( p2pVersion != null )
        {
            p2pVersion = p2pVersion.replace( "p2p version", "" ).trim();
        }

        return p2pVersion;
    }


    @Override
    public String getP2pStatusByP2PHash( Host host, String p2pHash ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkNotNull( p2pHash, "Invalid p2pHash" );
        Preconditions.checkState( !p2pHash.isEmpty(), "p2pHash can not be empty!" );

        CommandResult result = execute( host, commands.getP2pStatusBySwarm( p2pHash ) );

        if ( result.hasSucceeded() )
        {
            return result.getStdOut();
        }
        else
        {
            return result.getStdErr();
        }
    }


    public Set<String> getUsedP2pIfaceNames( final Host host ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );

        CommandResult result = execute( host, commands.getGetUsedP2pIfaceNamesCommand() );

        StringTokenizer st = new StringTokenizer( result.getStdOut(), LINE_DELIMITER );

        Set<String> p2pIfaceNames = Sets.newHashSet();

        while ( st.hasMoreTokens() )
        {
            String p2pIface = st.nextToken();

            if ( p2pIface.matches( Common.P2P_INTERFACE_NAME_REGEX ) )
            {
                p2pIfaceNames.add( p2pIface );
            }
        }

        return p2pIfaceNames;
    }
    //------------------ P2P SECTION END --------------------------------


    @Override
    public void createTunnel( final Host host, final String tunnelName, final String tunnelIp, final int vlan,
                              final long vni ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !StringUtils.isBlank( tunnelName ), "Invalid tunnel name" );
        Preconditions.checkArgument( !StringUtils.isBlank( tunnelIp ), "Invalid tunnel ip" );
        Preconditions.checkArgument( tunnelIp.matches( Common.IP_REGEX ), "Invalid tunnel ip" );
        Preconditions
                .checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ), "Invalid vlan" );
        Preconditions
                .checkArgument( NumUtil.isLongBetween( vni, Common.MIN_VNI_ID, Common.MAX_VNI_ID ), "Invalid vni" );


        execute( host, commands.getCreateTunnelCommand( tunnelName, tunnelIp, vlan, vni ) );
    }


    @Override
    public void deleteTunnel( final Host host, final String tunnelName ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !StringUtils.isBlank( tunnelName ), "Invalid tunnel name" );

        execute( host, commands.getDeleteTunnelCommand( tunnelName ) );
    }


    @Override
    public Tunnels getTunnels( final Host host ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );

        Tunnels tunnels = new Tunnels();

        CommandResult result = execute( host, commands.getGetTunnelsCommand() );

        StringTokenizer st = new StringTokenizer( result.getStdOut(), LINE_DELIMITER );

        Pattern p =
                Pattern.compile( "\\s*(\\S+)\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\s+(\\d+)\\s+(\\d+)\\s*" );

        while ( st.hasMoreTokens() )
        {
            Matcher m = p.matcher( st.nextToken() );

            if ( m.find() && m.groupCount() == 4 )
            {
                tunnels.addTunnel( new Tunnel( m.group( 1 ), m.group( 2 ), Integer.parseInt( m.group( 3 ) ),
                        Long.parseLong( m.group( 4 ) ) ) );
            }
        }

        return tunnels;
    }


    @Override
    public SshTunnel setupContainerSshTunnel( final String containerIp, final int sshIdleTimeout )
            throws NetworkManagerException
    {
        Preconditions.checkArgument( !StringUtils.isBlank( containerIp ), "Invalid container IP" );
        Preconditions.checkArgument( sshIdleTimeout > 0, "Timeout must be greater than 0" );
        Preconditions.checkArgument( containerIp.matches( Common.HOSTNAME_REGEX ), "Invalid container IP" );

        CommandResult result = execute( getManagementHost(),
                commands.getSetupContainerSshTunnelCommand( containerIp, sshIdleTimeout ) );

        try
        {
            String output = result.getStdOut().trim();

            String[] tunnelParts = output.split( ":" );

            return new SshTunnel( tunnelParts[0], Integer.parseInt( tunnelParts[1] ) );
        }
        catch ( Exception e )
        {
            throw new NetworkManagerException(
                    String.format( "Could not parse port out of response %s", result.getStdOut() ) );
        }
    }


    private long getUnixTimestampOffset( final long offsetSec )
    {
        long unixTimestamp = Instant.now().getEpochSecond();
        return unixTimestamp + offsetSec;
    }


    protected Host getManagementHost() throws NetworkManagerException
    {
        try
        {
            return peerManager.getLocalPeer().getManagementHost();
        }
        catch ( PeerException e )
        {
            throw new NetworkManagerException( e );
        }
    }


    protected CommandResult execute( final Host host, final RequestBuilder requestBuilder )
            throws NetworkManagerException
    {
        try
        {
            CommandResult result = host.execute( requestBuilder );
            if ( !result.hasSucceeded() )
            {
                throw new NetworkManagerException(
                        String.format( "Command failed: %s, %s", result.getStdErr(), result.getStatus() ) );
            }

            return result;
        }
        catch ( CommandException e )
        {
            throw new NetworkManagerException( e );
        }
    }


    @Override
    public ReservedPorts getReservedPorts( final Host host ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );

        CommandResult result = execute( host, commands.getGetReservedPortsCommand() );

        StringTokenizer st = new StringTokenizer( result.getStdOut(), LINE_DELIMITER );

        Pattern p = Pattern.compile( "\\s*(\\w+)\\s*:\\s*(\\d+)\\s*" );

        ReservedPorts reservedPorts = new ReservedPorts();

        while ( st.hasMoreTokens() )
        {
            Matcher m = p.matcher( st.nextToken() );

            if ( m.find() && m.groupCount() == 2 )
            {
                reservedPorts.addReservedPort( new ReservedPort( Protocol.valueOf( m.group( 1 ).toUpperCase() ),
                        Integer.parseInt( m.group( 2 ) ) ) );
            }
        }


        return reservedPorts;
    }


    public ReservedPorts getContainerPortMappings( final Host host, final Protocol protocol )
            throws NetworkManagerException
    {
        Preconditions.checkNotNull( host );

        CommandResult result = execute( host, commands.getListPortMappingsCommand( protocol ) );

        StringTokenizer st = new StringTokenizer( result.getStdOut(), LINE_DELIMITER );

        Pattern p = Pattern.compile( "\\s*(\\w+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)?" );

        ReservedPorts reservedPorts = new ReservedPorts();

        while ( st.hasMoreTokens() )
        {
            Matcher m = p.matcher( st.nextToken() );

            if ( m.find() && m.groupCount() >= 3 )
            {
                String[] containerIpPort = m.group( 3 ).split( ":" );
                reservedPorts.addReservedPort( new ReservedPort( Protocol.valueOf( m.group( 1 ).toUpperCase() ),
                        Integer.parseInt( m.group( 2 ) ), containerIpPort[0], Integer.parseInt( containerIpPort[1] ),
                        m.groupCount() > 3 ? m.group( 4 ) : null ) );
            }
        }


        return reservedPorts;
    }


    @Override
    public boolean isPortMappingReserved( final Host host, final Protocol protocol, final int externalPort,
                                          final String ipAddress, final int internalPort, final String domain )
            throws NetworkManagerException
    {
        for ( final ReservedPort mapping : getContainerPortMappings( host, null ).getReservedPorts() )
        {
            if ( mapping.getProtocol().name().equalsIgnoreCase( protocol.name() ) && mapping.getPort() == externalPort
                    && mapping.getContainerPort() == internalPort && mapping.getContainerIp()
                                                                            .equalsIgnoreCase( ipAddress ) && (
                    domain == null || domain.equals( mapping.getDomain() ) ) )
            {
                return true;
            }
        }

        return false;
    }


    @Override
    public void mapContainerPort( final Host host, final Protocol protocol, final String containerIp,
                                  final int containerPort, final int rhPort ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host );
        Preconditions.checkNotNull( protocol );
        Preconditions.checkArgument( !StringUtils.isBlank( containerIp ) );
        Preconditions.checkArgument( containerIp.matches( Common.IP_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( containerPort, Common.MIN_PORT, Common.MAX_PORT ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( rhPort, Common.MIN_PORT, Common.MAX_PORT ) );

        execute( host, commands.getMapContainerPortCommand( protocol, containerIp, containerPort, rhPort ) );
    }


    @Override
    public void removeContainerPortMapping( final Host host, final Protocol protocol, final String containerIp,
                                            final int containerPort, final int rhPort ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host );
        Preconditions.checkNotNull( protocol );
        Preconditions.checkArgument( !StringUtils.isBlank( containerIp ) );
        Preconditions.checkArgument( containerIp.matches( Common.IP_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( containerPort, Common.MIN_PORT, Common.MAX_PORT ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( rhPort, Common.MIN_PORT, Common.MAX_PORT ) );

        execute( host, commands.getRemoveContainerPortMappingCommand( protocol, containerIp, containerPort, rhPort ) );
    }


    @Override
    public void mapContainerPortToDomain( final Host host, final Protocol protocol, final String containerIp,
                                          final int containerPort, final int rhPort, final String domain,
                                          final String sslCertPath, final LoadBalancing loadBalancing,
                                          final boolean sslBackend, final boolean redirect, final boolean http2 )
            throws NetworkManagerException
    {
        Preconditions.checkNotNull( host );
        Preconditions.checkNotNull( protocol );
        Preconditions.checkArgument( protocol == Protocol.HTTP || protocol == Protocol.HTTPS );
        Preconditions.checkArgument( !StringUtils.isBlank( containerIp ) );
        Preconditions.checkArgument( containerIp.matches( Common.IP_REGEX ) );
        Preconditions.checkArgument( !StringUtils.isBlank( domain ) );
        Preconditions.checkArgument( domain.matches( Common.HOSTNAME_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( containerPort, Common.MIN_PORT, Common.MAX_PORT ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( rhPort, Common.MIN_PORT, Common.MAX_PORT ) );

        execute( host,
                commands.getMapContainerPortToDomainCommand( protocol, containerIp, containerPort, rhPort, domain,
                        sslCertPath, loadBalancing, sslBackend, redirect, http2 ) );
    }


    @Override
    public void removeContainerPortDomainMapping( final Host host, final Protocol protocol, final String containerIp,
                                                  final int containerPort, final int rhPort, final String domain )
            throws NetworkManagerException
    {
        Preconditions.checkNotNull( host );
        Preconditions.checkNotNull( protocol );
        Preconditions.checkArgument( protocol == Protocol.HTTP || protocol == Protocol.HTTPS );
        Preconditions.checkArgument( !StringUtils.isBlank( containerIp ) );
        Preconditions.checkArgument( containerIp.matches( Common.IP_REGEX ) );
        Preconditions.checkArgument( !StringUtils.isBlank( domain ) );
        Preconditions.checkArgument( domain.matches( Common.HOSTNAME_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( containerPort, Common.MIN_PORT, Common.MAX_PORT ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( rhPort, Common.MIN_PORT, Common.MAX_PORT ) );

        execute( host,
                commands.getRemoveContainerPortDomainMappingCommand( protocol, containerIp, containerPort, rhPort,
                        domain ) );
    }


    @Override
    public String getResourceHostIp( final Host host ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host );

        CommandResult result = execute( host, commands.getGetIPAddressCommand() );

        return !StringUtils.isBlank( result.getStdOut() ) ? result.getStdOut().trim() : result.getStdOut();
    }
}
