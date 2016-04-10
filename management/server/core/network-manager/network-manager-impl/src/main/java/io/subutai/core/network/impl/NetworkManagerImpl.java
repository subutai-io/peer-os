package io.subutai.core.network.impl;


import java.time.Instant;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.P2PConnection;
import io.subutai.common.protocol.P2PConnections;
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
        Preconditions.checkArgument( !Strings.isNullOrEmpty( interfaceName ), "Invalid interface name" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pHash ), "Invalid P2P hash" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( localIp ), "Invalid ip" );
        Preconditions.checkArgument( localIp.matches( Common.IP_REGEX ), "Invalid ip" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( secretKey ), "Invalid secret key" );
        Preconditions.checkArgument( secretKeyTtlSec > 0, "Invalid time-to-live" );


        execute( host, commands.getJoinP2PSwarmCommand( interfaceName, localIp, p2pHash, secretKey,
                getUnixTimestampOffset( secretKeyTtlSec ) ) );
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

        execute( getManagementHost(), commands.getRemoveVlanDomainCommand( vLanId ) );
    }


    @Override
    public void setVlanDomain( final int vLanId, final String domain,
                               final DomainLoadBalanceStrategy domainLoadBalanceStrategy, final String sslCertPath )
            throws NetworkManagerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ),
                "Invalid vlan" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domain ), "Invalid domain" );
        Preconditions.checkArgument( domain.matches( Common.HOSTNAME_REGEX ), "Invalid domain" );
        Preconditions.checkNotNull( domainLoadBalanceStrategy, "Invalid strategy" );

        execute( getManagementHost(),
                commands.getSetVlanDomainCommand( vLanId, domain, domainLoadBalanceStrategy, sslCertPath ) );
    }


    @Override
    public boolean isIpInVlanDomain( final String hostIp, final int vLanId ) throws NetworkManagerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ),
                "Invalid vlan" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostIp ), "Invalid host IP" );
        Preconditions.checkArgument( hostIp.matches( Common.HOSTNAME_REGEX ), "Invalid host IP" );

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
        Preconditions.checkArgument( hostIp.matches( Common.HOSTNAME_REGEX ), "Invalid host IP" );

        execute( getManagementHost(), commands.getAddIpToVlanDomainCommand( hostIp, vLanId ) );
    }


    @Override
    public void removeIpFromVlanDomain( final String hostIp, final int vLanId ) throws NetworkManagerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ),
                "Invalid vlan" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostIp ), "Invalid host IP" );
        Preconditions.checkArgument( hostIp.matches( Common.HOSTNAME_REGEX ), "Invalid host IP" );

        execute( getManagementHost(), commands.getRemoveIpFromVlanDomainCommand( hostIp, vLanId ) );
    }


    @Override
    public int setupContainerSsh( final String containerIp, final int sshIdleTimeout ) throws NetworkManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerIp ), "Invalid container IP" );
        Preconditions.checkArgument( sshIdleTimeout > 0, "Timeout must be greater than 0" );
        Preconditions.checkArgument( containerIp.matches( Common.HOSTNAME_REGEX ), "Invalid container IP" );

        CommandResult result =
                execute( getManagementHost(), commands.getSetupContainerSshCommand( containerIp, sshIdleTimeout ) );

        try
        {
            return Integer.parseInt( result.getStdOut().trim() );
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
}
