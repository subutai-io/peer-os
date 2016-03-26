package io.subutai.core.environment.impl.adapter;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.PeerConfImpl;
import io.subutai.core.peer.api.PeerManager;


public class EnvironmentAdapter
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final EnvironmentManagerImpl environmentManager;

    private final PeerManager peerManager;


    public EnvironmentAdapter( EnvironmentManagerImpl environmentManager, PeerManager peerManager )
    {
        this.environmentManager = environmentManager;

        this.peerManager = peerManager;
    }


    // ===

    static String envId = "c326a0ee-db5a-493d-be42-d61c59685be9";

    static String peerId = "AC1BC9D5F025E3AA84B6B088B20136F12C2FD06F";

    static String subnetCidr = "192.168.2.1/24";

    static long vni = 1372649;

    static String p2pSubnet = "10.11.1.0";

    static String peerP2p = "10.11.1.1";

    static String chIp = "192.168.2.2";

    static String chId = "4FCEEC86A960A99D5520F6C38C01426EEB73BF13";

    static String lxcName = "92b51495-e1d9-4dff-b01a-fe395ddd359f";

    static String templateName = "elasticsearch";

    static String rhId = "93C21AEAA7A89798DE38836AFDAFEB0013F7F03B";


    private ProxyEnvironmentContainer getContainer()
    {
        HostInterfaceModel him = new HostInterfaceModel( "eth0", chIp );

        Set<HostInterfaceModel> set = Sets.newHashSet();
        set.add( him );

        HostInterfaces hi = new HostInterfaces( chId, set );

        ProxyEnvironmentContainer ec = new ProxyEnvironmentContainer(
                peerId,
                peerId,
                lxcName,

                new ContainerHostInfoModel(
                        chId,
                        lxcName, hi,
                        HostArchitecture.AMD64,
                        ContainerHostState.RUNNING
                ),

                templateName,
                HostArchitecture.AMD64, 0, 0,
                "intra.lan", ContainerSize.SMALL,
                rhId,
                "Container 2"
        );

        ec.setEnvironmentManager( environmentManager );

        return ec;
    }


    private Set<ProxyEnvironmentContainer> getContainers()
    {
        HashSet<ProxyEnvironmentContainer> envContainers = new HashSet<>();

        envContainers.add( getContainer() );

        Set<String> localContainerIds = getLocalContainerIds();

        Host proxyContainer = getProxyContainer( envContainers, localContainerIds );

        setProxyToRemoteContainers( envContainers, localContainerIds, proxyContainer );

        return envContainers;
    }


    private void setProxyToRemoteContainers( Set<ProxyEnvironmentContainer> envContainers, Set<String> localContainerIds, Host proxyContainer )
    {
        for ( ProxyEnvironmentContainer c : envContainers )
        {
//            if ( !localContainerIds.contains( c.getId() ) )
//            {
                c.setProxyContainer( proxyContainer );
//            }
        }
    }


    // Returns a first local container which will be used as to execute SSH commands to remote containers
    private Host getProxyContainer( Set<ProxyEnvironmentContainer> envContainers, Set<String> localHostIds )
    {
        for ( ProxyEnvironmentContainer host : envContainers )
        {
            if ( localHostIds.contains( host.getId() ) && host.getState() == ContainerHostState.RUNNING ) {
                return host;
            }
        }

        return null;
    }


    private Set<String> getLocalContainerIds()
    {
        HashSet<String> ids = new HashSet<>();

        for ( ResourceHost rh : peerManager.getLocalPeer().getResourceHosts() )
        {
            for ( ContainerHost ch : rh.getContainerHosts() )
            {
                ids.add( ch.getId() );
            }
        }

        return ids;
    }


    public ProxyEnvironment get( final String id )
    {
        ProxyEnvironment e = new ProxyEnvironment(
                "Mock Env",
                subnetCidr,
                null,
                3L,
                peerId
        );

        e.setId( id );
        e.setP2PSubnet( p2pSubnet );
        e.setVni( vni );
        e.setVersion( 1L );
        e.setStatus( EnvironmentStatus.HEALTHY );
        e.getEnvironmentId();


        P2PConfig p2PConfig = new P2PConfig( peerId, null, null, peerP2p, null, 0 );

        PeerConfImpl peerConf = new PeerConfImpl( p2PConfig );
        peerConf.setId( 51L );

        e.addEnvironmentPeer( peerConf );

        e.setEnvironmentManager( environmentManager );

        log.debug( "env: {}", e );

        Set<EnvironmentContainerImpl> containers = new HashSet<>();

        containers.addAll( getContainers() );

        e.addContainers( containers );

        return e;
    }


    public Set<Environment> getEnvironments()
    {
        log.debug( "=== Giving mock environments ===" );

        HashSet<Environment> set = new HashSet<>();

        Environment env = get( envId );
        set.add( env );

        log.debug( "env: {}", env );

        return set;
    }
}
