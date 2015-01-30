package org.safehaus.subutai.core.environment.impl.net;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.core.network.api.N2NConnection;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.commons.net.util.SubnetUtils;


public class NetworkSetup
{

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
            env.setPeerVlanInfo( pi.getId(), vlanId );
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

        List<PeerInfo> peers = getPeers( buildProcess );
        for ( PeerInfo pi : peers )
        {
            int vlanId = env.getPeerVlanInfo().get( pi.getId() );
            NetworkManager nm = defineNetworkManager( pi );
            nm.setupVniVLanMapping( tunnelName, env.getVni(), vlanId );
            environmentManager.saveEnvironment( env );
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
            nm.setContainerIp( ch.getHostname(), allAddresses[ind], netMask, env.getPeerVlanInfo().get( pi.getId() ) );

            env.setLastUsedIpIndex( ind++ );
            environmentManager.saveEnvironment( env );
        }
    }


    private NetworkManager defineNetworkManager( PeerInfo pi )
    {
        return peerManager.getLocalPeerInfo().equals( pi ) ? networkManager :
               networkManager.getRemoteManager( pi.getIp(), pi.getPort() );
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


    private synchronized int nextVni()
    {
        List<Environment> environments = environmentManager.getEnvironments();
        if ( environments == null || environments.isEmpty() )
        {
            return 1;
        }

        // sort environments by descending vni values
        Comparator<Environment> comparatorByVni = new Comparator<Environment>()
        {
            @Override
            public int compare( Environment o1, Environment o2 )
            {
                return -1 * Integer.compare( o1.getVni(), o2.getVni() );
            }
        };
        Collections.sort( environments, comparatorByVni );

        // return next available value
        return environments.get( 0 ).getVni() + 1;
    }
}

