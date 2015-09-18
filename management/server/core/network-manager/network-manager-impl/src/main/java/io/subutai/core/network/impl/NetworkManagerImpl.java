package io.subutai.core.network.impl;


import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.network.Vni;
import io.subutai.common.network.VniVlanMapping;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.common.util.NumUtil;
import io.subutai.core.network.api.ContainerInfo;
import io.subutai.core.network.api.N2NConnection;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.peer.api.Tunnel;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.peer.api.ResourceHost;


/**
 * Implementation of Network Manager
 */
public class NetworkManagerImpl implements NetworkManager
{
    private static final Logger LOG = LoggerFactory.getLogger( NetworkManagerImpl.class );
    private static final String LINE_DELIMITER = "\n";
    private final PeerManager peerManager;
    protected Commands commands = new Commands();


    public NetworkManagerImpl( final PeerManager peerManager )
    {
        Preconditions.checkNotNull( peerManager );

        this.peerManager = peerManager;
    }


    @Override
    public void setupN2NConnection( final String superNodeIp, final int superNodePort, final String interfaceName,
                                    final String communityName, final String localIp, final String keyType,
                                    final String pathToKeyFile ) throws NetworkManagerException
    {
        execute( getManagementHost(),
                commands.getSetupN2NConnectionCommand( superNodeIp, superNodePort, interfaceName, communityName,
                        localIp, keyType, pathToKeyFile ) );
    }


    @Override
    public void removeN2NConnection( final String interfaceName, final String communityName )
            throws NetworkManagerException
    {
        execute( getManagementHost(), commands.getRemoveN2NConnectionCommand( interfaceName, communityName ) );
    }


    @Override
    public void setupTunnel( final int tunnelId, final String tunnelIp ) throws NetworkManagerException
    {
        Preconditions.checkArgument( tunnelId > 0, "Tunnel id must be greater than 0" );

        execute( getManagementHost(),
                commands.getSetupTunnelCommand( String.format( "%s%d", TUNNEL_PREFIX, tunnelId ), tunnelIp,
                        TUNNEL_TYPE ) );
    }


    @Override
    public void removeTunnel( final int tunnelId ) throws NetworkManagerException
    {
        Preconditions.checkArgument( tunnelId > 0, "Tunnel id must be greater than 0" );

        execute( getManagementHost(),
                commands.getRemoveTunnelCommand( String.format( "%s%d", TUNNEL_PREFIX, tunnelId ) ) );
    }


    @Override
    public void setupGateway( final String gatewayIp, final int vLanId ) throws NetworkManagerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );

        execute( getManagementHost(), commands.getSetupGatewayCommand( gatewayIp, vLanId ) );
    }


    @Override
    public void setupGatewayOnContainer( final String containerName, final String gatewayIp,
                                         final String interfaceName ) throws NetworkManagerException
    {
        execute( getContainerHost( containerName ),
                commands.getSetupGatewayOnContainerCommand( gatewayIp, interfaceName ) );
    }


    @Override
    public void removeGateway( final int vLanId ) throws NetworkManagerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );

        execute( getManagementHost(), commands.getRemoveGatewayCommand( vLanId ) );
    }


    @Override
    public void cleanupEnvironmentNetworkSettings( final String environmentId ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        Set<Vni> reservedVnis = getReservedVnis();

        for ( Vni vni : reservedVnis )
        {
            if ( vni.getEnvironmentId().equals( environmentId ) )
            {
                execute( getManagementHost(), commands.getCleanupEnvironmentNetworkSettingsCommand( vni.getVlan() ) );
                break;
            }
        }
    }


    @Override
    public void removeGatewayOnContainer( final String containerName ) throws NetworkManagerException
    {
        execute( getContainerHost( containerName ), commands.getRemoveGatewayOnContainerCommand() );
    }


    @Override
    public Set<Tunnel> listTunnels() throws NetworkManagerException
    {
        Set<Tunnel> tunnels = Sets.newHashSet();

        CommandResult result = execute( getManagementHost(), commands.getListTunnelsCommand() );

        StringTokenizer st = new StringTokenizer( result.getStdOut(), LINE_DELIMITER );

        Pattern p = Pattern.compile( "(tunnel\\d+)-(.+)" );

        while ( st.hasMoreTokens() )
        {
            Matcher m = p.matcher( st.nextToken() );

            if ( m.find() && m.groupCount() == 2 )
            {
                LOG.debug( String.format( "Adding new tunnel: %s %s", m.group( 1 ), m.group( 2 ) ) );
                tunnels.add( new TunnelImpl( m.group( 1 ), m.group( 2 ) ) );
            }
        }

        LOG.debug( String.format( "Total count of tunnel: %d", tunnels.size() ) );
        return tunnels;
    }


    @Override
    public Set<N2NConnection> listN2NConnections() throws NetworkManagerException
    {
        Set<N2NConnection> connections = Sets.newHashSet();

        CommandResult result = execute( getManagementHost(), commands.getListN2NConnectionsCommand() );

        StringTokenizer st = new StringTokenizer( result.getStdOut(), LINE_DELIMITER );

        Pattern p = Pattern.compile(
                "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\s+(\\d+)"
                        + "\\s+(\\w+)\\s+(\\w+)" );

        while ( st.hasMoreTokens() )
        {
            Matcher m = p.matcher( st.nextToken() );

            if ( m.find() && m.groupCount() == 5 )
            {
                connections.add( new N2NConnectionImpl( m.group( 1 ), m.group( 2 ), Integer.parseInt( m.group( 3 ) ),
                        m.group( 4 ), m.group( 5 ) ) );
            }
        }

        return connections;
    }


    @Override
    public void setupVniVLanMapping( final int tunnelId, final long vni, final int vLanId, final String environmentId )
            throws NetworkManagerException
    {
        Preconditions.checkArgument( tunnelId > 0, "Tunnel id must be greater than 0" );
        Preconditions.checkArgument( NumUtil.isLongBetween( vni, Common.MIN_VNI_ID, Common.MAX_VNI_ID ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );

        execute( getManagementHost(),
                commands.getSetupVniVlanMappingCommand( String.format( "%s%d", TUNNEL_PREFIX, tunnelId ), vni, vLanId,
                        environmentId ) );
    }


    @Override
    public void removeVniVLanMapping( final int tunnelId, final long vni, final int vLanId )
            throws NetworkManagerException
    {
        Preconditions.checkArgument( tunnelId > 0, "Tunnel id must be greater than 0" );
        Preconditions.checkArgument( NumUtil.isLongBetween( vni, Common.MIN_VNI_ID, Common.MAX_VNI_ID ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );

        execute( getManagementHost(),
                commands.getRemoveVniVlanMappingCommand( String.format( "%s%d", TUNNEL_PREFIX, tunnelId ), vni,
                        vLanId ) );
    }


    @Override
    public Set<VniVlanMapping> getVniVlanMappings() throws NetworkManagerException
    {
        Set<VniVlanMapping> mappings = Sets.newHashSet();

        CommandResult result = execute( getManagementHost(), commands.getListVniVlanMappingsCommand() );

        Pattern p = Pattern.compile( String.format(
                        "\\s*(%s\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*([0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3"
                                + "}-[89ab][0-9a-f]{3}-[0-9a-f]{12})\\s*", NetworkManager.TUNNEL_PREFIX ),
                Pattern.CASE_INSENSITIVE );

        StringTokenizer st = new StringTokenizer( result.getStdOut(), LINE_DELIMITER );

        while ( st.hasMoreTokens() )
        {
            Matcher m = p.matcher( st.nextToken() );

            if ( m.find() && m.groupCount() == 4 )
            {
                mappings.add( new VniVlanMapping(
                        Integer.parseInt( m.group( 1 ).replace( NetworkManager.TUNNEL_PREFIX, "" ) ),
                        Long.parseLong( m.group( 2 ) ), Integer.parseInt( m.group( 3 ) ), m.group( 4 ) ) );
            }
        }

        return mappings;
    }


    @Override
    public void reserveVni( Vni vni ) throws NetworkManagerException
    {
        Preconditions.checkNotNull( vni );
        Preconditions.checkArgument( NumUtil.isIntBetween( vni.getVlan(), Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );


        execute( getManagementHost(),
                commands.getReserveVniCommand( vni.getVni(), vni.getVlan(), vni.getEnvironmentId() ) );
    }


    @Override
    public String getVlanDomain( final int vLanId ) throws NetworkManagerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );

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
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );

        execute( getManagementHost(), commands.getRemoveVlanDomainCommand( vLanId ) );
    }


    @Override
    public void setVlanDomain( final int vLanId, final String domain ) throws NetworkManagerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domain ), "Invalid domain" );
        Preconditions.checkArgument( domain.matches( Common.HOSTNAME_REGEX ), "Invalid domain" );

        execute( getManagementHost(), commands.getSetVlanDomainCommand( vLanId, domain ) );
    }


    @Override
    public boolean isIpInVlanDomain( final String hostIp, final int vLanId ) throws NetworkManagerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );
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
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostIp ), "Invalid host IP" );
        Preconditions.checkArgument( hostIp.matches( Common.HOSTNAME_REGEX ), "Invalid host IP" );

        execute( getManagementHost(), commands.getAddIpToVlanDomainCommand( hostIp, vLanId ) );
    }


    @Override
    public void removeIpFromVlanDomain( final String hostIp, final int vLanId ) throws NetworkManagerException
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostIp ), "Invalid host IP" );
        Preconditions.checkArgument( hostIp.matches( Common.HOSTNAME_REGEX ), "Invalid host IP" );

        execute( getManagementHost(), commands.getRemoveIpFromVlanDomainCommand( hostIp, vLanId ) );
    }


    @Override
    public Set<Vni> getReservedVnis() throws NetworkManagerException
    {
        Set<Vni> reservedVnis = Sets.newHashSet();

        CommandResult result = execute( getManagementHost(), commands.getListReservedVnisCommand() );

        Pattern p = Pattern.compile( "\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*"
                        + "([0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12})\\s*",
                Pattern.CASE_INSENSITIVE );


        StringTokenizer st = new StringTokenizer( result.getStdOut(), LINE_DELIMITER );

        while ( st.hasMoreTokens() )
        {
            Matcher m = p.matcher( st.nextToken() );

            if ( m.find() && m.groupCount() == 3 )
            {
                reservedVnis.add( new Vni( Long.parseLong( m.group( 1 ) ), Integer.parseInt( m.group( 2 ) ),
                        m.group( 3 ) ) );
            }
        }

        return reservedVnis;
    }


    @Override
    public void setContainerIp( final String containerName, final String ip, final int netMask, final int vLanId )
            throws NetworkManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ) && ip.matches( Common.IP_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( vLanId, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );

        execute( getResourceHost( containerName ),
                commands.getSetContainerIpCommand( containerName, ip, netMask, vLanId ) );
    }


    @Override
    public void removeContainerIp( final String containerName ) throws NetworkManagerException
    {
        execute( getResourceHost( containerName ), commands.getRemoveContainerIpCommand( containerName ) );
    }


    @Override
    public ContainerInfo getContainerIp( final String containerName ) throws NetworkManagerException
    {
        CommandResult result =
                execute( getResourceHost( containerName ), commands.getShowContainerIpCommand( containerName ) );

        Pattern pattern = Pattern.compile(
                "Environment IP:\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})/(\\d+)\\s+Vlan ID:\\s+(\\d+)\\s+" );
        Matcher m = pattern.matcher( result.getStdOut() );
        if ( m.find() && m.groupCount() == 3 )
        {
            return new ContainerInfoImpl( m.group( 1 ), Integer.parseInt( m.group( 2 ) ),
                    Integer.parseInt( m.group( 3 ) ) );
        }
        else
        {
            throw new NetworkManagerException( String.format( "Network info of %s not found", containerName ) );
        }
    }


    protected ManagementHost getManagementHost() throws NetworkManagerException
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


    protected ResourceHost getResourceHost( String containerName ) throws NetworkManagerException
    {
        try
        {
            ContainerHost containerHost = getContainerHost( containerName );
            return peerManager.getLocalPeer().getResourceHostByContainerName( containerHost.getHostname() );
        }
        catch ( PeerException e )
        {
            throw new NetworkManagerException( e );
        }
    }


    protected ContainerHost getContainerHost( String containerName ) throws NetworkManagerException
    {
        try
        {
            return peerManager.getLocalPeer().getContainerHostByName( containerName );
        }
        catch ( PeerException e )
        {
            throw new NetworkManagerException( e );
        }
    }


    protected CommandResult execute( Host host, RequestBuilder requestBuilder ) throws NetworkManagerException
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
    public void exchangeSshKeys( final Set<ContainerHost> containers ) throws NetworkManagerException
    {
        getSshManager( containers ).execute();
    }


    @Override
    public void addSshKeyToAuthorizedKeys( final Set<ContainerHost> containers, final String sshKey )
            throws NetworkManagerException
    {
        getSshManager( containers ).appendSshKey( sshKey );
    }


    @Override
    public void replaceSshKeyInAuthorizedKeys( final Set<ContainerHost> containers, final String oldSshKey,
                                               final String newSshKey ) throws NetworkManagerException
    {
        getSshManager( containers ).replaceSshKey( oldSshKey, newSshKey );
    }


    @Override
    public void removeSshKeyFromAuthorizedKeys( final Set<ContainerHost> containers, final String sshKey )
            throws NetworkManagerException
    {
        getSshManager( containers ).removeSshKey( sshKey );
    }


    @Override
    public void registerHosts( final Set<ContainerHost> containerHosts, final String domainName )
            throws NetworkManagerException
    {
        getHostManager( containerHosts, domainName ).execute();
    }


    protected SshManager getSshManager( final Set<ContainerHost> containers )
    {
        return new SshManager( containers );
    }


    protected HostManager getHostManager( final Set<ContainerHost> containerHosts, final String domainName )
    {
        return new HostManager( containerHosts, domainName );
    }
}
