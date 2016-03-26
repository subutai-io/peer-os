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
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.PeerConfImpl;
import io.subutai.core.peer.api.PeerManager;


public class EnvironmentAdapter
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final EnvironmentManagerImpl environmentManager;

    private final ProxyContainerHelper proxyContainerHelper;


    public EnvironmentAdapter( EnvironmentManagerImpl environmentManager, PeerManager peerManager )
    {
        this.environmentManager = environmentManager;

        proxyContainerHelper = new ProxyContainerHelper( peerManager );
    }


    //
    // Env
    //

    static String envId = "90e7ad5d-1497-45ab-a301-8d1cbad7944d";

    static String peerId = "0D091F8269B5B608F2E065601DD655B5A7C3DA37";

    static String subnetCidr = "192.168.3.1/24";

    static long vni = 674804;

    static String p2pSubnet = "10.11.2.0";

    static String peerP2p = "10.11.2.1";

    //
    // Containers
    //

    static String rhId = "5B7E40F52DD51F07FB098BABFCD5D347679AD897";

    static String templateName = "elasticsearch";


    private ProxyEnvironmentContainer getContainer( String ip, String id, String lxcName )
    {
        HostInterfaceModel him = new HostInterfaceModel( "eth0", ip );

        Set<HostInterfaceModel> set = Sets.newHashSet();
        set.add( him );

        HostInterfaces hi = new HostInterfaces( id, set );

        ProxyEnvironmentContainer ec = new ProxyEnvironmentContainer(
                peerId,
                peerId,
                lxcName,

                new ContainerHostInfoModel(
                        id,
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

        envContainers.add( getContainer( "192.168.3.2", "AF70232E4BCDC2436D21F1F31A248B945E6233B8", "1e221fd8-9c8c-43b3-9806-d84a41c30f50" ) );

        envContainers.add( getContainer( "192.168.3.3", "DC10DA1433EF35D905A0F9D434FDD7C3821BEC17", "6b8d4cb4-2e80-421f-b26b-4b9a1249cf82" ) );

        proxyContainerHelper.setProxyToRemoteContainers( envContainers );

        return envContainers;
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

        Set<EnvironmentContainerImpl> containers = new HashSet<>();

        containers.addAll( getContainers() );

        e.addContainers( containers );

        return e;
    }


    public Set<Environment> getEnvironments()
    {
        HashSet<Environment> set = new HashSet<>();

        Environment env = get( envId );
        set.add( env );

        return set;
    }
}
