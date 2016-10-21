package io.subutai.core.network.impl;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.network.ProxyLoadBalanceStrategy;


/**
 * Networking commands
 */

public class Commands
{
    private static final String TUNNEL_BINDING = "subutai tunnel";
    private static final String VXLAN_BINDING = "subutai vxlan";
    private static final String P2P_BINDING = "subutai p2p";
    private static final String PROXY_BINDING = "subutai proxy";
    private final SimpleDateFormat p2pDateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );


    public RequestBuilder getGetP2pVersionCommand()
    {
        return new RequestBuilder( P2P_BINDING ).withCmdArgs( Lists.<String>newArrayList( "-v" ) );
    }


    public RequestBuilder getP2PConnectionsCommand()
    {
        return new RequestBuilder( P2P_BINDING ).withCmdArgs( Lists.newArrayList( "-p" ) );
    }


    public RequestBuilder getJoinP2PSwarmCommand( String interfaceName, String localIp, String p2pHash,
                                                  String secretKey, long secretKeyTtlSec, String portRange )
    {
        return new RequestBuilder( P2P_BINDING ).withCmdArgs(
                Lists.newArrayList( "-c", interfaceName, p2pHash, secretKey, String.valueOf( secretKeyTtlSec ), localIp,
                        portRange ) ).withTimeout( 90 );
    }


    public RequestBuilder getResetP2PSecretKey( String p2pHash, String newSecretKey, long ttlSeconds )
    {
        return new RequestBuilder( P2P_BINDING )
                .withCmdArgs( Lists.newArrayList( "-u", p2pHash, newSecretKey, String.valueOf( ttlSeconds ) ) );
    }


    public RequestBuilder getGetP2pLogsCommand( Date from, Date till )
    {
        return new RequestBuilder(
                String.format( "journalctl -u *p2p* --since \"%s\" --until " + "\"%s\"", p2pDateFormat.format( from ),
                        p2pDateFormat.format( till ) ) );
    }


    public RequestBuilder getCreateTunnelCommand( String tunnelName, String tunnelIp, int vlan, long vni )
    {
        return new RequestBuilder( VXLAN_BINDING ).withCmdArgs(
                Lists.newArrayList( "-create", tunnelName, "-remoteip", tunnelIp, "-vlan", String.valueOf( vlan ),
                        "-vni", String.valueOf( vni ) ) );
    }


    public RequestBuilder getGetTunnelsCommand()
    {
        return new RequestBuilder( VXLAN_BINDING ).withCmdArgs( Lists.newArrayList( "-list" ) );
    }


    public RequestBuilder getGetVlanDomainCommand( int vLanId )
    {
        return new RequestBuilder( PROXY_BINDING )
                .withCmdArgs( Lists.newArrayList( "check", String.valueOf( vLanId ), "-d" ) );
    }


    public RequestBuilder getRemoveVlanDomainCommand( final String vLanId )
    {
        return new RequestBuilder( PROXY_BINDING ).withCmdArgs( Lists.newArrayList( "del", vLanId, "-d" ) );
    }


    public RequestBuilder getSetVlanDomainCommand( final String vLanId, final String domain,
                                                   final ProxyLoadBalanceStrategy proxyLoadBalanceStrategy,
                                                   final String sslCertPath )
    {
        List<String> args = Lists.newArrayList( "add", vLanId, "-d", domain );

        if ( proxyLoadBalanceStrategy != ProxyLoadBalanceStrategy.NONE )
        {
            args.add( "-p" );
            args.add( proxyLoadBalanceStrategy.getValue() );
        }

        if ( !Strings.isNullOrEmpty( sslCertPath ) )
        {
            args.add( "-f" );
            args.add( sslCertPath );
        }

        return new RequestBuilder( PROXY_BINDING ).withCmdArgs( args );
    }


    public RequestBuilder getCheckIpInVlanDomainCommand( final String hostIp, final int vLanId )
    {
        return new RequestBuilder( PROXY_BINDING )
                .withCmdArgs( Lists.newArrayList( "check", String.valueOf( vLanId ), "-h", hostIp ) );
    }


    public RequestBuilder getAddIpToVlanDomainCommand( final String hostIp, final String vLanId )
    {
        return new RequestBuilder( PROXY_BINDING ).withCmdArgs( Lists.newArrayList( "add", vLanId, "-h", hostIp ) );
    }


    public RequestBuilder getRemoveIpFromVlanDomainCommand( final String hostIp, final int vLanId )
    {
        return new RequestBuilder( PROXY_BINDING )
                .withCmdArgs( Lists.newArrayList( "del", String.valueOf( vLanId ), "-h", hostIp ) );
    }


    public RequestBuilder getSetupContainerSshTunnelCommand( final String containerIp, final int sshIdleTimeout )
    {
        return new RequestBuilder( TUNNEL_BINDING )
                .withCmdArgs( Lists.newArrayList( "add", containerIp, String.valueOf( sshIdleTimeout ) ) );
    }
}
