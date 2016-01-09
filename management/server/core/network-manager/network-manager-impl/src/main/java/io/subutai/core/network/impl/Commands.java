package io.subutai.core.network.impl;


import java.util.List;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.subutai.common.command.OutputRedirection;
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
    private static final String RESOURCE_HOST_NETWORK_BINDING = "subutai network";
    private static final String MANAGEMENT_PROXY_BINDING = "subutai proxy";


    //container commands


    public RequestBuilder getSetContainerIpCommand( String containerName, String ip, int netMask, int vLanId )
    {
        return new RequestBuilder( RESOURCE_HOST_NETWORK_BINDING ).withCmdArgs(
                Lists.newArrayList( containerName, "-s", String.format( "%s/%s", ip, netMask ),
                        String.valueOf( vLanId ) ) );
    }


    public RequestBuilder getShowContainerIpCommand( String containerName )
    {
        return new RequestBuilder( RESOURCE_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( containerName, "-l" ) );
    }


    public RequestBuilder getRemoveContainerIpCommand( String containerName )
    {
        return new RequestBuilder( RESOURCE_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( containerName, "-r" ) );
    }


    //management host commands


    public RequestBuilder getSetupN2NConnectionCommand( String superNodeIp, int superNodePort, String interfaceName,
                                                        String communityName, String localIp, String keyType,
                                                        String pathToKeyFile )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING ).withCmdArgs(
                Lists.newArrayList( "-N", superNodeIp, String.valueOf( superNodePort ), interfaceName, communityName,
                        localIp, keyType, pathToKeyFile ) ).withTimeout( 15 ).daemon();
    }


    public RequestBuilder getRemoveN2NConnectionCommand( String interfaceName, String communityName )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "-R", interfaceName, communityName ) );
    }


    public RequestBuilder getListN2NConnectionsCommand()
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING ).withCmdArgs( Lists.newArrayList( "-L" ) );
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


    public RequestBuilder getSetupGatewayCommand( String gatewayIp, int vLanId )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "-T", gatewayIp, String.valueOf( vLanId ) ) ).withTimeout( 90 )
                .withStdOutRedirection( OutputRedirection.NO );
    }


    public RequestBuilder getSetupGatewayOnContainerCommand( String gatewayIp, String interfaceName )
    {
        return new RequestBuilder( "route add default gw" )
                .withCmdArgs( Lists.newArrayList( gatewayIp, interfaceName ) );
    }


    public RequestBuilder getRemoveGatewayCommand( int vLanId )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "-D", String.valueOf( vLanId ) ) );
    }


    public RequestBuilder getCleanupEnvironmentNetworkSettingsCommand( int vLanId )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "-Z", "deleteall", String.valueOf( vLanId ) ) );
    }


    public RequestBuilder getRemoveGatewayOnContainerCommand()
    {
        return new RequestBuilder( "route del default gw" );
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


    public RequestBuilder getCreateSSHCommand()
    {
        return new RequestBuilder( "rm -rf /root/.ssh && " +
                "mkdir -p /root/.ssh && " +
                "chmod 700 /root/.ssh && " +
                "ssh-keygen -t dsa -P '' -f /root/.ssh/id_dsa" );
    }


    public RequestBuilder getReadSSHCommand()
    {
        return new RequestBuilder( "cat /root/.ssh/id_dsa.pub" );
    }


    public RequestBuilder getAppendSshKeyCommand( String key )
    {
        return new RequestBuilder( String.format( "mkdir -p /root/.ssh && " +
                "chmod 700 /root/.ssh && " +
                "echo '%s' >> /root/.ssh/authorized_keys && " +
                "chmod 644 /root/.ssh/authorized_keys", key ) );
    }


    public RequestBuilder getReplaceSshKeyCommand( String oldKey, String newKey )
    {
        return new RequestBuilder( String.format( "mkdir -p /root/.ssh && " +
                "chmod 700 /root/.ssh && " +
                "sed -i \"\\,%s,d\" /root/.ssh/authorized_keys ; " +
                "echo '%s' >> /root/.ssh/authorized_keys && " +
                "chmod 644 /root/.ssh/authorized_keys", oldKey, newKey ) );
    }


    public RequestBuilder getConfigSSHCommand()
    {
        return new RequestBuilder( "echo 'Host *' > /root/.ssh/config && " +
                "echo '    StrictHostKeyChecking no' >> /root/.ssh/config && " +
                "chmod 644 /root/.ssh/config" );
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


    public RequestBuilder getRemoveSshKeyCommand( final String key )
    {
        return new RequestBuilder( String.format( "chmod 700 /root/.ssh && " +
                "sed -i \"\\,%s,d\" /root/.ssh/authorized_keys && " +
                "chmod 644 /root/.ssh/authorized_keys", key ) );
    }


    public RequestBuilder getSetupContainerSshCommand( final String containerIp, final int sshIdleTimeout )
    {
        return new RequestBuilder( String.format( "subutai tunnel %s %d", containerIp, sshIdleTimeout ) );
    }
}
