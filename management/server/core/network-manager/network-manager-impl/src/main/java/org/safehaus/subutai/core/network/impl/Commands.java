package org.safehaus.subutai.core.network.impl;


import org.safehaus.subutai.common.command.RequestBuilder;

import com.google.common.collect.Lists;


/**
 * Networking commands
 */
public class Commands
{
    private static final String MANAGEMENT_HOST_NETWORK_BINDING = "subutai management_network";
    private static final String RESOURCE_HOST_NETWORK_BINDING = "subutai network";


    public RequestBuilder getSetupN2NConnectionCommand( String superNodeIp, int superNodePort, String interfaceName,
                                                        String communityName, String localIp, String pathToKeyFile )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING ).withCmdArgs(
                Lists.newArrayList( "-N", superNodeIp, String.valueOf( superNodePort ), interfaceName, communityName,
                        localIp, "file", pathToKeyFile ) );
    }


    public RequestBuilder getRemoveN2NConnectionCommand( String interfaceName, String communityName )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "-R", communityName, interfaceName ) );
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


    public RequestBuilder getSetupGatewayCommand( String gatewayIp, int vLanId )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "-T", gatewayIp, String.valueOf( vLanId ) ) );
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


    public RequestBuilder getRemoveGatewayOnContainerCommand()
    {
        return new RequestBuilder( "route del default gw" );
    }


    public RequestBuilder getSetupVniVlanMappingCommand( String tunnelName, int vni, int vLanId )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "-m", tunnelName, String.valueOf( vni ), String.valueOf( vLanId ) ) );
    }


    public RequestBuilder getRemoveVniVlanMappingCommand( String tunnelName, int vni, int vLanId )
    {
        return new RequestBuilder( MANAGEMENT_HOST_NETWORK_BINDING )
                .withCmdArgs( Lists.newArrayList( "-M", tunnelName, String.valueOf( vni ), String.valueOf( vLanId ) ) );
    }
}
