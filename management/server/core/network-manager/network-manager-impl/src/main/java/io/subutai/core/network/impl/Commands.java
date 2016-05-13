package io.subutai.core.network.impl;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.network.DomainLoadBalanceStrategy;


/**
 * Networking commands
 */

public class Commands
{
    private static final String MANAGEMENT_HOST_NETWORK_BINDING = "subutai management_network";
    private static final String MANAGEMENT_PROXY_BINDING = "subutai proxy";
    private final SimpleDateFormat p2pDateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );


    public RequestBuilder getGetP2pVersionCommand()
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.<String>newArrayList( "p2p", "-v" ) );
    }


    public RequestBuilder getP2PConnectionsCommand()
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING ).withCmdArgs( Lists.newArrayList( "p2p", "-p" ) );
    }


    public RequestBuilder getJoinP2PSwarmCommand( String interfaceName, String localIp, String p2pHash,
                                                  String secretKey, long secretKeyTtlSec )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING ).withCmdArgs(
                Lists.newArrayList( "p2p", "-c", interfaceName, p2pHash, secretKey, String.valueOf( secretKeyTtlSec ),
                        localIp ) ).withTimeout( 90 );
    }


    public RequestBuilder getResetP2PSecretKey( String p2pHash, String newSecretKey, long ttlSeconds )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "p2p", "-u", p2pHash, newSecretKey, String.valueOf( ttlSeconds ) ) );
    }


    public RequestBuilder getCreateTunnelCommand( String tunnelName, String tunnelIp, int vlan, long vni )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING ).withCmdArgs(
                Lists.newArrayList( "tunnel", "-create", tunnelName, "-remoteip", tunnelIp, "-vlan",
                        String.valueOf( vlan ), "-vni", String.valueOf( vni ) ) );
    }


    public RequestBuilder getGetTunnelsCommand()
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "tunnel", "-list" ) );
    }


    public RequestBuilder getGetVlanDomainCommand( int vLanId )
    {
        return new RequestBuilder( MANAGEMENT_PROXY_BINDING )
                .withCmdArgs( Lists.newArrayList( "check", String.valueOf( vLanId ), "-d" ) );
    }


    public RequestBuilder getRemoveVlanDomainCommand( final int vLanId )
    {
        return new RequestBuilder( MANAGEMENT_PROXY_BINDING )
                .withCmdArgs( Lists.newArrayList( "del", String.valueOf( vLanId ), "-d" ) );
    }


    public RequestBuilder getSetVlanDomainCommand( final int vLanId, final String domain,
                                                   final DomainLoadBalanceStrategy domainLoadBalanceStrategy,
                                                   final String sslCertPath )
    {
        List<String> args = Lists.newArrayList( "add", String.valueOf( vLanId ), "-d", domain, "-p",
                domainLoadBalanceStrategy.getValue() );
        if ( !Strings.isNullOrEmpty( sslCertPath ) )
        {
            args.add( "-f" );
            args.add( sslCertPath );
        }
        return new RequestBuilder( MANAGEMENT_PROXY_BINDING ).withCmdArgs( args );
    }


    public RequestBuilder getCheckIpInVlanDomainCommand( final String hostIp, final int vLanId )
    {
        return new RequestBuilder( MANAGEMENT_PROXY_BINDING )
                .withCmdArgs( Lists.newArrayList( "check", String.valueOf( vLanId ), "-h", hostIp ) );
    }


    public RequestBuilder getAddIpToVlanDomainCommand( final String hostIp, final int vLanId )
    {
        return new RequestBuilder( MANAGEMENT_PROXY_BINDING )
                .withCmdArgs( Lists.newArrayList( "add", String.valueOf( vLanId ), "-h", hostIp ) );
    }


    public RequestBuilder getRemoveIpFromVlanDomainCommand( final String hostIp, final int vLanId )
    {
        return new RequestBuilder( MANAGEMENT_PROXY_BINDING )
                .withCmdArgs( Lists.newArrayList( "del", String.valueOf( vLanId ), "-h", hostIp ) );
    }


    public RequestBuilder getSetupContainerSshTunnelCommand( final String containerIp, final int sshIdleTimeout )
    {
        return new RequestBuilder( String.format( "subutai tunnel add %s %d", containerIp, sshIdleTimeout ) );
    }


    public RequestBuilder getGetP2pLogsCommand( Date from, Date till )
    {
        return new RequestBuilder(
                String.format( "journalctl -u *p2p* --since \"%s\" --until " + "\"%s\"", p2pDateFormat.format( from ),
                        p2pDateFormat.format( till ) ) );
    }
}
