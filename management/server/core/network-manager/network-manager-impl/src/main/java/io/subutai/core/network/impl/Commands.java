package io.subutai.core.network.impl;


import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.protocol.LoadBalancing;
import io.subutai.common.protocol.Protocol;
import io.subutai.common.settings.Common;


/**
 * Networking commands
 */

public class Commands
{
    private static final String TUNNEL_BINDING = "subutai tunnel";
    private static final String VXLAN_BINDING = "subutai vxlan";
    private static final String P2P_BINDING = "p2p";
    private static final String PROXY_BINDING = "subutai proxy";
    private static final String INFO_BINDING = "subutai info";
    private static final String MAP_BINDING = "subutai map";

    private static final String NETWORK_IFACE_REMOVAL = "ip link delete";


    RequestBuilder getGetReservedPortsCommand()
    {
        return new RequestBuilder( INFO_BINDING ).withCmdArgs( "ports" );
    }


    RequestBuilder getGetP2pVersionCommand()
    {
        return new RequestBuilder( P2P_BINDING ).withCmdArgs( "-v" );
    }


    RequestBuilder getP2PConnectionsCommand()
    {
        return new RequestBuilder( P2P_BINDING ).withCmdArgs( "show" );
    }


    RequestBuilder getJoinP2PSwarmCommand( String interfaceName, String localIp, String p2pHash, String secretKey,
                                           long secretKeyTtlSec, String portRange )
    {
        return new RequestBuilder( P2P_BINDING )
                .withCmdArgs( "start", "-dev", interfaceName, "-hash", p2pHash, "-key", secretKey, "-ttl",
                        String.valueOf( secretKeyTtlSec ), "-ip", localIp, "-ports", portRange )
                .withTimeout( Common.JOIN_P2P_TIMEOUT_SEC );
    }


    RequestBuilder getP2pStatusBySwarm( String p2pHash )
    {
        return new RequestBuilder( P2P_BINDING ).withCmdArgs( "status", "-hash", p2pHash );
    }


    RequestBuilder getJoinP2PSwarmDHCPCommand( String interfaceName, String p2pHash, String secretKey,
                                               long secretKeyTtlSec, String portRange )
    {
        return new RequestBuilder( P2P_BINDING )
                .withCmdArgs( "start", "-dev", interfaceName, "-hash", p2pHash, "-key", secretKey, "-ttl",
                        String.valueOf( secretKeyTtlSec ), "-ports", portRange )
                .withTimeout( Common.JOIN_P2P_TIMEOUT_SEC );
    }


    RequestBuilder getRemoveP2PSwarmCommand( String p2pHash )
    {
        return new RequestBuilder( P2P_BINDING ).withCmdArgs( "stop", "-hash", p2pHash )
                                                .withTimeout( Common.LEAVE_P2P_TIMEOUT_SEC );
    }


    RequestBuilder getResetP2PSecretKey( String p2pHash, String newSecretKey, long ttlSeconds )
    {
        return new RequestBuilder( P2P_BINDING )
                .withCmdArgs( "set", "-key", newSecretKey, "-ttl", String.valueOf( ttlSeconds ), "-hash", p2pHash );
    }


    RequestBuilder getGetUsedP2pIfaceNamesCommand()
    {
        return new RequestBuilder( P2P_BINDING ).withCmdArgs( "show", "--interfaces", "--all" );
    }


    RequestBuilder getRemoveP2PIfaceCommand( String interfaceName )
    {
        return new RequestBuilder( NETWORK_IFACE_REMOVAL ).withCmdArgs( interfaceName )
                                                          .withTimeout( Common.IP_LINK_REMOVE_TIMEOUT_SEC );
    }


    RequestBuilder getCreateTunnelCommand( String tunnelName, String tunnelIp, int vlan, long vni )
    {
        return new RequestBuilder( VXLAN_BINDING )
                .withCmdArgs( "add", tunnelName, "--remoteip", tunnelIp, "--vlan", String.valueOf( vlan ), "--vni",
                        String.valueOf( vni ) );
    }


    RequestBuilder getDeleteTunnelCommand( final String tunnelName )
    {
        return new RequestBuilder( VXLAN_BINDING ).withCmdArgs( "del", tunnelName );
    }


    RequestBuilder getGetTunnelsCommand()
    {
        return new RequestBuilder( VXLAN_BINDING ).withCmdArgs( "list" );
    }


    RequestBuilder getGetVlanDomainCommand( int vLanId )
    {
        return new RequestBuilder( PROXY_BINDING ).withCmdArgs( "domain", "check", String.valueOf( vLanId ) );
    }


    RequestBuilder getRemoveVlanDomainCommand( final String vLanId )
    {
        return new RequestBuilder( PROXY_BINDING ).withCmdArgs( "domain", "del", vLanId );
    }


    RequestBuilder getSetVlanDomainCommand( final String vLanId, final String domain,
                                            final ProxyLoadBalanceStrategy proxyLoadBalanceStrategy,
                                            final String sslCertPath )
    {
        List<String> args = Lists.newArrayList( "domain", "add", vLanId, domain );

        if ( proxyLoadBalanceStrategy != ProxyLoadBalanceStrategy.NONE )
        {
            args.add( "-b" );
            args.add( proxyLoadBalanceStrategy.getValue() );
        }

        if ( !Strings.isNullOrEmpty( sslCertPath ) )
        {
            args.add( "-f" );
            args.add( sslCertPath );
        }

        return new RequestBuilder( PROXY_BINDING ).withCmdArgs( args.toArray( new String[0] ) );
    }


    RequestBuilder getCheckIpInVlanDomainCommand( final String hostIp, final int vLanId )
    {
        return new RequestBuilder( PROXY_BINDING ).withCmdArgs( "host", "check", String.valueOf( vLanId ), hostIp );
    }


    RequestBuilder getAddIpToVlanDomainCommand( final String hostIp, final String vLanId )
    {
        return new RequestBuilder( PROXY_BINDING ).withCmdArgs( "host", "add", vLanId, hostIp );
    }


    RequestBuilder getRemoveIpFromVlanDomainCommand( final String hostIp, final int vLanId )
    {
        return new RequestBuilder( PROXY_BINDING ).withCmdArgs( "host", "del", String.valueOf( vLanId ), hostIp );
    }


    RequestBuilder getSetupContainerSshTunnelCommand( final String containerIp, final int sshIdleTimeout )
    {
        return new RequestBuilder( TUNNEL_BINDING ).withCmdArgs( "add", containerIp, String.valueOf( sshIdleTimeout ) );
    }


    RequestBuilder getMapContainerPortToRandomPortCommand( final Protocol protocol, final String containerIp,
                                                           final int containerPort )
    {
        return new RequestBuilder( MAP_BINDING ).withCmdArgs( "add", "-p", protocol.name().toLowerCase(), "-i",
                String.format( "%s:%s", containerIp, containerPort ) );
    }


    RequestBuilder getMapContainerPortToSpecificPortCommand( final Protocol protocol, final String containerIp,
                                                             final int containerPort, final int rhPort )
    {
        return new RequestBuilder( MAP_BINDING ).withCmdArgs( "add", "-p", protocol.name().toLowerCase(), "-i",
                String.format( "%s:%s", containerIp, containerPort ), "-e", String.valueOf( rhPort ) );
    }


    RequestBuilder getRemoveContainerPortMappingCommand( final Protocol protocol, final String containerIp,
                                                         final int containerPort, final int rhPort )
    {
        return new RequestBuilder( MAP_BINDING ).withCmdArgs( "rm", "-p", protocol.name().toLowerCase(), "-i",
                String.format( "%s:%s", containerIp, containerPort ), "-e", String.valueOf( rhPort ) );
    }


    RequestBuilder getMapContainerPortToDomainCommand( final Protocol protocol, final String containerIp,
                                                       final int containerPort, final int rhPort, final String domain,
                                                       final String sslCertPath, final LoadBalancing loadBalancing,
                                                       final boolean sslBackend )
    {
        List<String> args = Lists.newArrayList( "add", "-p", protocol.name().toLowerCase(), "-i",
                String.format( "%s:%s", containerIp, containerPort ), "-e", String.valueOf( rhPort ), "-n", domain );

        if ( !Strings.isNullOrEmpty( sslCertPath ) )
        {
            args.add( "-c" );
            args.add( sslCertPath );
        }

        if ( sslBackend )
        {
            args.add( "--sslbackend" );
        }

        if ( loadBalancing != null )
        {
            args.add( "-b" );
            args.add( loadBalancing.name().toLowerCase() );
        }

        return new RequestBuilder( MAP_BINDING ).withCmdArgs( args.toArray( new String[0] ) );
    }


    RequestBuilder getRemoveContainerPortDomainMappingCommand( final Protocol protocol, final String containerIp,
                                                               final int containerPort, final int rhPort,
                                                               final String domain )
    {
        return new RequestBuilder( MAP_BINDING ).withCmdArgs( "rm", "-p", protocol.name().toLowerCase(), "-i",
                String.format( "%s:%s", containerIp, containerPort ), "-e", String.valueOf( rhPort ), "-n", domain );
    }


    RequestBuilder getListPortMappingsCommand( final Protocol protocol )
    {
        List<String> args = Lists.newArrayList( "ls" );

        if ( protocol != null )
        {
            args.add( "-p" );
            args.add( protocol.name().toLowerCase() );
        }

        return new RequestBuilder( MAP_BINDING ).withCmdArgs( args.toArray( new String[0] ) );
    }


    RequestBuilder getGetIPAddressCommand()
    {
        return new RequestBuilder( INFO_BINDING ).withCmdArgs( "ipaddr" );
    }
}
