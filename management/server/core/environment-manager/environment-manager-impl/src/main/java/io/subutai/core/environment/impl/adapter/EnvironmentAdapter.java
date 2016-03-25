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
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.entity.PeerConfImpl;


public class EnvironmentAdapter
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    static String envId = "e3c47046-75ae-46b5-adf3-4b2b7012f6c6";

    static String peerId = "AFAE43FE560D309809EE238D6B598523E31B8AAA";

    static String subnetCidr = "192.168.2.1/24";

    static long vni = 15444522;

    static String p2pSubnet = "10.11.1.0";

    static String peerP2p = "10.11.1.1";

    static String chIp = "192.168.2.2";

    static String chId = "F428C7754FC7AE90188EC42E43BFBDF54D99936E";

    static String lxcName = "4f268646-0faf-45e7-8f21-0056668169b8";

    static String templateName = "elasticsearch";

    static String rhId = "F6E3A586B7B74F704DC40EE2698A25F71EF711F3";


    private ProxyEnvironmentContainer getContainer( EnvironmentManagerImpl environmentManager )
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


    public EnvironmentImpl get( final String id, EnvironmentManagerImpl environmentManager )
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
        set3.add( getContainer( environmentManager ) );
        e.addContainers( set3 );


        P2PConfig p2PConfig = new P2PConfig( peerId, null, null, peerP2p, null, 0 );

        PeerConfImpl peerConf = new PeerConfImpl( p2PConfig );
        peerConf.setId( 51L );

        e.addEnvironmentPeer( peerConf );

        e.setEnvironmentManager( environmentManager );

        return e;
    }


    public Set<Environment> getEnvironments( EnvironmentManagerImpl environmentManager )
    {
        log.debug( "=== Giving mock environments ===" );

        HashSet<Environment> set = new HashSet<>();

        Environment env = get( envId, environmentManager );
        set.add( env );

        log.debug( "env: {}", env );

        return set;
    }
}
