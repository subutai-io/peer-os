package io.subutai.core.network.impl;


import java.util.List;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.settings.Common;


/**
 * Networking commands
 */

public class Commands
{
    private static final String MANAGEMENT_HOST_NETWORK_BINDING = "subutai management_network";
    private static final String MANAGEMENT_PROXY_BINDING = "subutai proxy";
    private static final String SSH_FOLDER = "/root/.ssh";
    private static final String SSH_FILE = String.format( "%s/authorized_keys", SSH_FOLDER );


    public RequestBuilder getP2PConnectionsCommand( String p2pHash )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "p2p", "-p", Strings.isNullOrEmpty( p2pHash ) ? "" : p2pHash ) );
    }


    public RequestBuilder getSetupP2PConnectionCommand( String interfaceName, String localIp, String p2pHash,
                                                        String secretKey, long secretKeyTtlSec )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING ).withCmdArgs(
                Lists.newArrayList( "p2p", "-c", interfaceName, p2pHash, secretKey, String.valueOf( secretKeyTtlSec ),
                        Strings.isNullOrEmpty( localIp ) ? "" : localIp ) ).withTimeout( 90 );
    }


    public RequestBuilder getRemoveP2PConnectionCommand( String p2pHash )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "p2p", "-d", p2pHash ) );
    }


    public RequestBuilder getResetP2PSecretKey( String p2pHash, String newSecretKey, long ttlSeconds )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "p2p", "-u", p2pHash, newSecretKey, String.valueOf( ttlSeconds ) ) );
    }


    public RequestBuilder getSetupTunnelCommand( String tunnelName, String tunnelIp, String tunnelType )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "-c", tunnelName, tunnelIp, tunnelType ) );
    }


    public RequestBuilder getRemoveTunnelCommand( String tunnelName )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "-r", tunnelName ) );
    }


    public RequestBuilder getListTunnelsCommand()
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING ).withCmdArgs( Lists.newArrayList( "-l" ) );
    }


    public RequestBuilder getSetupVniVlanMappingCommand( String tunnelName, long vni, int vLanId, String environmentId )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING ).withCmdArgs(
                Lists.newArrayList( "-m", tunnelName, String.valueOf( vni ), String.valueOf( vLanId ),
                        environmentId ) );
    }


    public RequestBuilder getRemoveVniVlanMappingCommand( String tunnelName, long vni, int vLanId )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "-M", tunnelName, String.valueOf( vni ), String.valueOf( vLanId ) ) );
    }


    public RequestBuilder getListVniVlanMappingsCommand()
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING ).withCmdArgs( Lists.newArrayList( "-v" ) );
    }


    public RequestBuilder getReserveVniCommand( long vni, int vlan, String environmentId )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING ).withCmdArgs(
                Lists.newArrayList( "-E", String.valueOf( vni ), String.valueOf( vlan ), environmentId ) );
    }


    public RequestBuilder getListReservedVnisCommand()
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING ).withCmdArgs( Lists.newArrayList( "-Z", "list" ) );
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


    // ssh and hosts


    public RequestBuilder getCreateNReadSSHCommand()
    {
        return new RequestBuilder( String.format( "rm -rf %1$s && " +
                "mkdir -p %1$s && " +
                "chmod 700 %1$s && " +
                "ssh-keygen -t dsa -P '' -f %1$s/id_dsa && " + "cat %1$s/id_dsa.pub", SSH_FOLDER ) );
    }


    public RequestBuilder getCreateNewAuthKeysFileCommand( String keys )
    {
        return new RequestBuilder( String.format( "mkdir -p %1$s && " +
                "chmod 700 %1$s && " +
                "echo '%3$s' >> %2$s && " +
                "chmod 644 %2$s", SSH_FOLDER, SSH_FILE, keys ) );
    }


    public RequestBuilder getAppendSshKeyCommand( String key )
    {
        return new RequestBuilder( String.format(
                "mkdir -p '%1$s' && " + "echo '%3$s' >> '%2$s' && " + "chmod 700 -R '%1$s' && "
                        + "sort -u '%2$s' -o '%2$s'", SSH_FOLDER, SSH_FILE, key ) );
    }


    public RequestBuilder getReplaceSshKeyCommand( String oldKey, String newKey )
    {
        return new RequestBuilder( String.format( "mkdir -p %1$s && " +
                "chmod 700 %1$s && " +
                "sed -i \"\\,%3$s,d\" %2$s ; " +
                "echo '%4$s' >> %2$s && " +
                "chmod 644 %2$s", SSH_FOLDER, SSH_FILE, oldKey, newKey ) );
    }


    public RequestBuilder getConfigSSHCommand()
    {
        return new RequestBuilder( String.format( "echo 'Host *' > %1$s/config && " +
                "echo '    StrictHostKeyChecking no' >> %1$s/config && " +
                "chmod 644 %1$s/config", SSH_FOLDER ) );
    }


    public RequestBuilder getRemoveSshKeyCommand( final String key )
    {
        return new RequestBuilder( String.format( "chmod 700 %1$s && " +
                "sed -i \"\\,%3$s,d\" %2$s && " +
                "chmod 644 %2$s", SSH_FOLDER, SSH_FILE, key ) );
    }


    public RequestBuilder getSetupContainerSshCommand( final String containerIp, final int sshIdleTimeout )
    {
        return new RequestBuilder( String.format( "subutai tunnel %s %d", containerIp, sshIdleTimeout ) );
    }


    public RequestBuilder getAddIpHostToEtcHostsCommand( String domainName, Set<ContainerHost> containerHosts )
    {
        StringBuilder cleanHosts = new StringBuilder( "localhost|127.0.0.1|" );
        StringBuilder appendHosts = new StringBuilder();

        for ( ContainerHost host : containerHosts )
        {
            String ip = host.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE ).getIp();
            String hostname = host.getHostname();
            cleanHosts.append( ip ).append( "|" ).append( hostname ).append( "|" );
            appendHosts.append( "/bin/echo '" ).
                    append( ip ).append( " " ).
                               append( hostname ).append( "." ).append( domainName ).
                               append( " " ).append( hostname ).
                               append( "' >> '/etc/hosts'; " );
        }

        if ( cleanHosts.length() > 0 )
        {
            //drop pipe | symbol
            cleanHosts.setLength( cleanHosts.length() - 1 );
            cleanHosts.insert( 0, "egrep -v '" );
            cleanHosts.append( "' /etc/hosts > etc-hosts-cleaned; mv etc-hosts-cleaned /etc/hosts;" );
            appendHosts.insert( 0, cleanHosts );
        }

        appendHosts.append( "/bin/echo '127.0.0.1 localhost " ).append( "' >> '/etc/hosts';" );

        return new RequestBuilder( appendHosts.toString() );
    }


    public RequestBuilder getPingDistanceCommand( final String ip )
    {
        return new RequestBuilder( "ping -c 10 -i 0.2 -w 3 " + ip );
    }
}
