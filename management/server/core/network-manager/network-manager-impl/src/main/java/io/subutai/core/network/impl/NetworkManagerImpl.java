package io.subutai.core.network.impl;


import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.network.LogLevel;
import io.subutai.common.network.P2pLogs;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.network.SshTunnel;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.CustomProxyConfig;
import io.subutai.common.protocol.LoadBalancing;
import io.subutai.common.protocol.P2PConnection;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.Protocol;
import io.subutai.common.protocol.ReservedPort;
import io.subutai.common.protocol.ReservedPorts;
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.protocol.Tunnels;
import io.subutai.common.settings.Common;
import io.subutai.common.util.IPUtil;
import io.subutai.common.util.NumUtil;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.domain.PortMapDto;
import io.subutai.hub.share.dto.domain.ReservedPortMapping;


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
        Preconditions.checkArgument( !Strings.isNullOrEmpty( interfaceName ), "Invalid interface name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pHash ), "Invalid P2P hash" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( localIp ), "Invalid ip" );
        Preconditions.checkArgument( localIp.matches( Common.IP_REGEX ), "Invalid ip" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( secretKey ), "Invalid secret key" );
        Preconditions.checkArgument( secretKeyTtlSec > 0, "Invalid time-to-live" );


        execute( host, commands.getJoinP2PSwarmCommand( interfaceName, localIp, p2pHash, secretKey,
                getUnixTimestampOffset( secretKeyTtlSec ),
                String.format( "%s-%s", Common.P2P_PORT_RANGE_START, Common.P2P_PORT_RANGE_END ) ) );
    }


    @Override
    public void removeP2PSwarm( final Host host, String p2pHash ) throws NetworkManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pHash ), "Invalid P2P hash" );

        execute( host, commands.getRemoveP2PSwarmCommand( p2pHash ) );
    }


    @Override
    public void resetSwarmSecretKey( final Host host, final String p2pHash, final String newSecretKey,
                                     final long ttlSeconds ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pHash ), "Invalid P2P hash" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newSecretKey ), "Invalid secret key" );
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

        return result.getStdOut();
    }


    @Override
    public P2pLogs getP2pLogs( final Host host, final LogLevel logLevel, final Date from, final Date till )
            throws NetworkManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkNotNull( logLevel, "Invalid log level" );
        Preconditions.checkNotNull( from, "Invalid from date" );
        Preconditions.checkNotNull( till, "Invalid till date" );

        P2pLogs p2pLogs = new P2pLogs();

        try
        {
            CommandResult result = host.execute( commands.getGetP2pLogsCommand( from, till, logLevel ) );

            StringTokenizer st = new StringTokenizer( result.getStdOut(), System.lineSeparator() );

            while ( st.hasMoreTokens() )
            {
                String logLine = st.nextToken();

                if ( !Strings.isNullOrEmpty( logLine ) )
                {
                    p2pLogs.addLog( logLine );
                }
            }
        }
        catch ( CommandException e )
        {
            throw new NetworkManagerException( e );
        }

        return p2pLogs;
    }

    //------------------ P2P SECTION END --------------------------------


    @Override
    public void createTunnel( final Host host, final String tunnelName, final String tunnelIp, final int vlan,
                              final long vni ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host, "Invalid host" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( tunnelName ), "Invalid tunnel name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( tunnelIp ), "Invalid tunnel ip" );
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
        Preconditions.checkArgument( !Strings.isNullOrEmpty( tunnelName ), "Invalid tunnel name" );

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
    public String getVlanDomain( final int vLanId ) throws NetworkManagerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ),
                "Invalid vlan" );

        try
        {
            CommandResult result = getManagementHost().execute( commands.getGetVlanDomainCommand( vLanId ) );
            if ( result.hasSucceeded() )
            {
                return result.getStdOut();
            }
        }
        catch ( CommandException e )
        {
            throw new NetworkManagerException( e );
        }

        return null;
    }


    @Override
    public void removeVlanDomain( final int vLanId ) throws NetworkManagerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ),
                "Invalid vlan" );

        execute( getManagementHost(), commands.getRemoveVlanDomainCommand( String.valueOf( vLanId ) ) );
    }


    @Override
    public void setVlanDomain( final int vLanId, final String domain,
                               final ProxyLoadBalanceStrategy proxyLoadBalanceStrategy, final String sslCertPath )
            throws NetworkManagerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ),
                "Invalid vlan" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domain ), "Invalid domain" );
        Preconditions.checkNotNull( proxyLoadBalanceStrategy, "Invalid strategy" );

        execute( getManagementHost(),
                commands.getSetVlanDomainCommand( String.valueOf( vLanId ), domain, proxyLoadBalanceStrategy,
                        sslCertPath ) );
    }


    @Override
    public boolean isIpInVlanDomain( final String hostIp, final int vLanId ) throws NetworkManagerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ),
                "Invalid vlan" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostIp ), "Invalid host IP" );
        Preconditions.checkArgument( hostIp.matches( Common.HOSTNAME_REGEX_WITH_PORT ), "Invalid host IP" );

        try
        {
            CommandResult result =
                    getManagementHost().execute( commands.getCheckIpInVlanDomainCommand( hostIp, vLanId ) );
            if ( result.hasSucceeded() )
            {
                return true;
            }
        }
        catch ( CommandException e )
        {
            throw new NetworkManagerException( e );
        }

        return false;
    }


    @Override
    public void addIpToVlanDomain( final String hostIp, final int vLanId ) throws NetworkManagerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ),
                "Invalid vlan" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostIp ), "Invalid host IP" );

        Preconditions.checkArgument( hostIp.matches( Common.HOSTNAME_REGEX_WITH_PORT ), "Invalid host IP" );

        execute( getManagementHost(), commands.getAddIpToVlanDomainCommand( hostIp, String.valueOf( vLanId ) ) );
    }


    @Override
    public void removeIpFromVlanDomain( final String hostIp, final int vLanId ) throws NetworkManagerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ),
                "Invalid vlan" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostIp ), "Invalid host IP" );
        Preconditions.checkArgument( hostIp.matches( Common.HOSTNAME_REGEX_WITH_PORT ), "Invalid host IP" );

        execute( getManagementHost(), commands.getRemoveIpFromVlanDomainCommand( hostIp, vLanId ) );
    }


    @Override
    public SshTunnel setupContainerSshTunnel( final String containerIp, final int sshIdleTimeout )
            throws NetworkManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerIp ), "Invalid container IP" );
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
    public void addCustomProxy( final CustomProxyConfig proxyConfig, ContainerHost containerHost )
            throws NetworkManagerException
    {
        Preconditions.checkNotNull( proxyConfig );
        Preconditions.checkNotNull( containerHost );

        execute( getManagementHost(), commands.getRemoveVlanDomainCommand( proxyConfig.getVlan() ) );
        execute( getManagementHost(), commands.getSetVlanDomainCommand( proxyConfig.getVlan(), proxyConfig.getDomain(),
                ProxyLoadBalanceStrategy.NONE,
                String.format( "/mnt/lib/lxc/%s/rootfs/etc/nginx/ssl.pem", containerHost.getContainerName() ) ) );
        execute( getManagementHost(), commands.getAddIpToVlanDomainCommand(
                containerHost.getIp() + ( proxyConfig.getPort() == -1 ? "" : ":" + proxyConfig.getPort() ),
                proxyConfig.getVlan() ) );
    }


    @Override
    public void removeCustomProxy( final String vlan ) throws NetworkManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( vlan ) );

        execute( getManagementHost(), commands.getRemoveVlanDomainCommand( vlan ) );
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

        Pattern p = Pattern.compile( "\\s*(\\w+)\\s+(\\d+)\\s+(\\S+)\\s*" );

        ReservedPorts reservedPorts = new ReservedPorts();

        while ( st.hasMoreTokens() )
        {
            Matcher m = p.matcher( st.nextToken() );

            if ( m.find() && m.groupCount() == 3 )
            {
                reservedPorts.addReservedPort( new ReservedPort( Protocol.valueOf( m.group( 1 ).toUpperCase() ),
                        Integer.parseInt( m.group( 2 ) ), m.group( 3 ) ) );
            }
        }


        return reservedPorts;
    }


    @Override
    public List<ReservedPortMapping> getReservedPortMappings( final Host host ) throws NetworkManagerException
    {
        List<ReservedPortMapping> mappedPorts = Lists.newArrayList();

        Preconditions.checkNotNull( host );

        CommandResult result = execute( host, commands.getListOfReservedPortMappingCommand() );

        StringTokenizer st = new StringTokenizer( result.getStdOut(), LINE_DELIMITER );

        while ( st.hasMoreTokens() )
        {
            StringTokenizer parts = new StringTokenizer( st.nextToken(), "\t:" );

            if ( parts.countTokens() >= 4 )
            {
                try
                {
                    ReservedPortMapping mapping = new ReservedPortMapping();

                    mapping.setProtocol( PortMapDto.Protocol.valueOf( parts.nextToken().toUpperCase() ) );
                    mapping.setExternalPort( Integer.parseInt( parts.nextToken() ) );
                    mapping.setIpAddress( parts.nextToken() );
                    mapping.setInternalPort( Integer.parseInt( parts.nextToken() ) );
                    mapping.setDomain( parts.hasMoreTokens() ? parts.nextToken() : null );

                    mappedPorts.add( mapping );
                }
                catch ( NumberFormatException e )
                {
                    continue;
                }
            }
        }

        return mappedPorts;
    }


    @Override
    public boolean isPortMappingReserved( final Host host, final Protocol protocol, final int externalPort,
                                          final String ipAddress, final int internalPort )
            throws NetworkManagerException
    {
        for ( final ReservedPortMapping mapping : getReservedPortMappings( host ) )
        {
            if ( mapping.getProtocol().name().equalsIgnoreCase( protocol.name() )
                    &&  mapping.getExternalPort() == externalPort
                    && mapping.getInternalPort() == internalPort
                    && mapping.getIpAddress().equalsIgnoreCase( ipAddress ) )
            {
                return true;
            }
        }

        return false;
    }


    @Override
    public int mapContainerPort( final Host host, final Protocol protocol, final String containerIp,
                                 final int containerPort ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host );
        Preconditions.checkNotNull( protocol );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerIp ) );
        Preconditions.checkArgument( containerIp.matches( Common.IP_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( containerPort, Common.MIN_PORT, Common.MAX_PORT ) );

        CommandResult result = execute( host,
                commands.getMapContainerPortToRandomPortCommand( protocol, containerIp, containerPort ) );

        return Integer.parseInt( result.getStdOut().trim() );
    }


    @Override
    public void mapContainerPort( final Host host, final Protocol protocol, final String containerIp,
                                  final int containerPort, final int rhPort ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host );
        Preconditions.checkNotNull( protocol );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerIp ) );
        Preconditions.checkArgument( containerIp.matches( Common.IP_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( containerPort, Common.MIN_PORT, Common.MAX_PORT ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( rhPort, Common.MIN_PORT, Common.MAX_PORT ) );

        execute( host,
                commands.getMapContainerPortToSpecificPortCommand( protocol, containerIp, containerPort, rhPort ) );
    }


    @Override
    public void removeContainerPortMapping( final Host host, final Protocol protocol, final String containerIp,
                                            final int containerPort, final int rhPort ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( host );
        Preconditions.checkNotNull( protocol );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerIp ) );
        Preconditions.checkArgument( containerIp.matches( Common.IP_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( containerPort, Common.MIN_PORT, Common.MAX_PORT ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( rhPort, Common.MIN_PORT, Common.MAX_PORT ) );

        execute( host, commands.getRemoveContainerPortMappingCommand( protocol, containerIp, containerPort, rhPort ) );
    }


    @Override
    public void mapContainerPortToDomain( final Host host, final Protocol protocol, final String containerIp,
                                          final int containerPort, final int rhPort, final String domain,
                                          final String sslCertPath, final LoadBalancing loadBalancing )
            throws NetworkManagerException
    {
        Preconditions.checkNotNull( host );
        Preconditions.checkNotNull( protocol );
        Preconditions.checkArgument( protocol == Protocol.HTTP || protocol == Protocol.HTTPS );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerIp ) );
        Preconditions.checkArgument( containerIp.matches( Common.IP_REGEX ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domain ) );
        Preconditions.checkArgument( domain.matches( Common.HOSTNAME_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( containerPort, Common.MIN_PORT, Common.MAX_PORT ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( rhPort, Common.MIN_PORT, Common.MAX_PORT ) );

        execute( host,
                commands.getMapContainerPortToDomainCommand( protocol, containerIp, containerPort, rhPort, domain,
                        sslCertPath, loadBalancing ) );
    }


    @Override
    public void removeContainerPortDomainMapping( final Host host, final Protocol protocol, final String containerIp,
                                                  final int containerPort, final int rhPort, final String domain )
            throws NetworkManagerException
    {
        Preconditions.checkNotNull( host );
        Preconditions.checkNotNull( protocol );
        Preconditions.checkArgument( protocol == Protocol.HTTP || protocol == Protocol.HTTPS );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerIp ) );
        Preconditions.checkArgument( containerIp.matches( Common.IP_REGEX ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domain ) );
        Preconditions.checkArgument( domain.matches( Common.HOSTNAME_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( containerPort, Common.MIN_PORT, Common.MAX_PORT ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( rhPort, Common.MIN_PORT, Common.MAX_PORT ) );

        execute( host,
                commands.getRemoveContainerPortDomainMappingCommand( protocol, containerIp, containerPort, rhPort,
                        domain ) );
    }
}
