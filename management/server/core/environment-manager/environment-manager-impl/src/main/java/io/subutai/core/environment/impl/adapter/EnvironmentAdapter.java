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


public class EnvironmentAdapter
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private EnvironmentManagerImpl environmentManager;


    public EnvironmentAdapter( EnvironmentManagerImpl environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    // ===

    static String envId = "738b897b-d99d-40b1-b6f2-59a520ddec5e";

    static String peerId = "7367531C1CF348904D774F3EFD1AF00CA34B2E33";

    static String subnetCidr = "192.168.2.1/24";

    static long vni = 6759079;

    static String p2pSubnet = "10.11.1.0";

    static String peerP2p = "10.11.0.1";

    static String chIp = "192.168.2.2";

    static String chId = "3C6F5D514B51E789940FD7A8659A708659EFC551";

    static String lxcName = "5bf15be3-1254-4ede-ad0c-d017f4741c16";

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

        HashSet<EnvironmentContainerImpl> set3 = new HashSet<>();
        set3.add( getContainer() );
        e.addContainers( set3 );


        P2PConfig p2PConfig = new P2PConfig( peerId, null, null, peerP2p, null, 0 );

        PeerConfImpl peerConf = new PeerConfImpl( p2PConfig );
        peerConf.setId( 51L );

        e.addEnvironmentPeer( peerConf );

        e.setEnvironmentManager( environmentManager );

        log.debug( "env: {}", e );

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
