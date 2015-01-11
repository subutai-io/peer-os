package org.safehaus.subutai.core.environment.impl.net;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentPersistenceException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;
import org.safehaus.subutai.core.environment.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.network.api.N2NConnection;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerInfo;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;


public class NetworkSetup
{
    private static final Logger LOGGER = LoggerFactory.getLogger( NetworkSetup.class );

    private final EnvironmentBuildProcess buildProcess;
    private NetworkManager networkManager;
    private PeerManager peerManager;
    private EnvironmentManager environmentManager;


    public NetworkSetup( EnvironmentBuildProcess buildProcess )
    {
        this.buildProcess = buildProcess;
    }


    public void setNetworkManager( NetworkManager networkManager )
    {
        this.networkManager = networkManager;
    }


    public void setPeerManager( PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public void setEnvironmentManager( EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public void setupN2Nconnections( N2NConnection n2n, String keyFilePath ) throws NetworkManagerException
    {
        List<PeerInfo> peers = getPeers( buildProcess );
        for ( PeerInfo pi : peers )
        {
            NetworkManager nm = defineNetworkManager( pi );
            nm.setupN2NConnection( n2n.getSuperNodeIp(), n2n.getSuperNodePort(), n2n.getInterfaceName(),
                                   n2n.getCommunityName(), pi.getIp(), keyFilePath );
        }
    }


    public void setupTunnels( String tunnelName ) throws NetworkManagerException
    {
        List<PeerInfo> peers = getPeers( buildProcess );
        for ( PeerInfo pi : peers )
        {
            NetworkManager nm = defineNetworkManager( pi );
            for ( PeerInfo pi2 : peers )
            {
                if ( !pi2.equals( pi ) )
                {
                    nm.setupTunnel( tunnelName, pi2.getIp(), NetworkSetupHelper.getTunnelType() );
                }
            }
        }
    }


    public void setupGateways( Environment environment ) throws NetworkManagerException
    {
        EnvironmentImpl env = ( EnvironmentImpl ) environment;
        SubnetUtils subnet = new SubnetUtils( env.getSubnetCidr() );
        String[] allAddresses = subnet.getInfo().getAllAddresses();

        int lastIndex = env.getLastUsedIpIndex();
        List<PeerInfo> peers = getPeers( buildProcess );
        for ( PeerInfo pi : peers )
        {
            int vlanId = pi.getLastUsedVlanId() + 1;
            env.getPeerVlanId().put( pi.getId(), vlanId );
            pi.setLastUsedVlanId( vlanId );
            pi.setGatewayIp( allAddresses[++lastIndex] );

            NetworkManager nm = defineNetworkManager( pi );
            nm.setupGateway( pi.getGatewayIp(), vlanId );

            env.setLastUsedIpIndex( lastIndex );
            environmentManager.saveEnvironment( env );
            peerManager.update( pi );
        }
    }


    public void setupVniVlanMappings( String tunnelName, Environment environment ) throws NetworkManagerException
    {
        // define environment vni
        EnvironmentImpl env = ( EnvironmentImpl ) environment;
        env.setVni( nextVni() );
        environmentManager.saveEnvironment( env );

        EnvironmentManagerImpl emi = ( EnvironmentManagerImpl ) environmentManager;

        List<PeerInfo> peers = getPeers( buildProcess );
        for ( PeerInfo pi : peers )
        {
            int vlanId = env.getPeerVlanId().get( pi.getId() );
            NetworkManager nm = defineNetworkManager( pi );
            nm.setupVniVLanMapping( tunnelName, env.getVni(), vlanId );
            try
            {
                emi.getEnvironmentDAO().saveVniVlanMapping( env.getVni(), vlanId );
            }
            catch ( EnvironmentPersistenceException ex )
            {
                LOGGER.error( "Failed to save vni-vlan mapping for peer {}", pi.getId(), ex );
                throw new NetworkManagerException( ex );
            }
        }
    }


    public void setupGatewaysOnContainers( Environment env ) throws NetworkManagerException
    {
        for ( ContainerHost ch : env.getContainerHosts() )
        {
            PeerInfo pi = peerManager.getPeerInfo( UUID.fromString( ch.getPeerId() ) );
            NetworkManager nm = defineNetworkManager( pi );
            nm.setupGatewayOnContainer( ch.getHostname(), pi.getGatewayIp(), NetworkSetupHelper.getInterfaceName() );
        }
    }


    public void setupContainerIpAddresses( Environment environment ) throws NetworkManagerException
    {
        EnvironmentImpl env = ( EnvironmentImpl ) environment;

        SubnetUtils subnet = new SubnetUtils( env.getSubnetCidr() );
        String[] allAddresses = subnet.getInfo().getAllAddresses();
        int netMask = Integer.parseInt( subnet.getInfo().getNetmask() );
        int ind = env.getLastUsedIpIndex() + 1;

        for ( ContainerHost ch : env.getContainerHosts() )
        {
            PeerInfo pi = peerManager.getPeerInfo( UUID.fromString( ch.getPeerId() ) );

            NetworkManager nm = defineNetworkManager( pi );
            nm.setContainerIp( ch.getHostname(), allAddresses[ind], netMask, env.getPeerVlanId().get( pi.getId() ) );

            env.setLastUsedIpIndex( ind++ );
            environmentManager.saveEnvironment( env );
        }
    }


    private NetworkManager defineNetworkManager( PeerInfo pi )
    {
        return peerManager.getLocalPeerInfo().equals( pi )
                ? networkManager
                : networkManager.getRemoteManager( pi.getIp(), pi.getPort() );
    }


    private List<PeerInfo> getPeers( EnvironmentBuildProcess process )
    {
        List<PeerInfo> peers = new ArrayList<>();
        for ( CloneContainersMessage ccm : process.getMessageMap().values() )
        {
            PeerInfo peer = peerManager.getPeerInfo( ccm.getTargetPeerId() );
            if ( peer != null )
            {
                peers.add( peer );
            }
        }
        return peers;
    }


    private int nextVni()
    {
        EnvironmentManagerImpl emi = ( EnvironmentManagerImpl ) environmentManager;
        try
        {
            Map<Integer, Set<Integer>> map = emi.getEnvironmentDAO().getVniVlanMappings();
            List<Integer> ls = new ArrayList<>( map.keySet() );

            Collections.sort( ls );
            return ls.get( ls.size() - 1 ) + 1;
        }
        catch ( EnvironmentPersistenceException ex )
        {
            LOGGER.error( "vni-vlan mappings", ex );
            return 0;
        }
    }
}

