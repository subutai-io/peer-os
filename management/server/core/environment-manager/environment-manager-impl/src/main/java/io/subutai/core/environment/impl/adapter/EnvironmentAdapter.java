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
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.entity.PeerConfImpl;


public class EnvironmentAdapter
{
    private final Logger log = LoggerFactory.getLogger( getClass() );


    public EnvironmentImpl get( final String id )
    {
        HostInterfaceModel him = new HostInterfaceModel( "eth0", "192.168.1.3" );

        Set<HostInterfaceModel> set = Sets.newHashSet();
        set.add( him );

        HostInterfaces hi = new HostInterfaces( "F7D931A6E757092632AFB3ADE80831A7E9C62A34", set );

        ProxyEnvironmentContainer ec = new ProxyEnvironmentContainer(
                "5104E12FA7FE92ECEFFCD9C73437F6BCEC556FE4",
                "5104E12FA7FE92ECEFFCD9C73437F6BCEC556FE4",
                "461f49b8-480b-40ad-a56d-464d3e21227f",

                new ContainerHostInfoModel(
                        "F7D931A6E757092632AFB3ADE80831A7E9C62A34",
                        "461f49b8-480b-40ad-a56d-464d3e21227f", hi,
                        HostArchitecture.AMD64,
                        ContainerHostState.RUNNING
                ),

                "elasticsearch",
                HostArchitecture.AMD64, 0, 0,
                "intra.lan", ContainerSize.SMALL,
                "F6E3A586B7B74F704DC40EE2698A25F71EF711F3",
                "Container 2"
        );




        HostInterfaceModel him2 = new HostInterfaceModel( "eth0", "192.168.1.2" );

        Set<HostInterfaceModel> set2 = Sets.newHashSet();
        set2.add( him2 );

        HostInterfaces hi2 = new HostInterfaces( "9D0281ABD6742B47D87D0D9E2F763ACCD9B913F9", set2 );

        ProxyEnvironmentContainer ec2 = new ProxyEnvironmentContainer(
                "5104E12FA7FE92ECEFFCD9C73437F6BCEC556FE4",
                "5104E12FA7FE92ECEFFCD9C73437F6BCEC556FE4",
                "3697b6ee-54c2-43bb-bdef-f4d83a03dfc0",

                new ContainerHostInfoModel(
                        "9D0281ABD6742B47D87D0D9E2F763ACCD9B913F9",
                        "3697b6ee-54c2-43bb-bdef-f4d83a03dfc0", hi2,
                        HostArchitecture.AMD64,
                        ContainerHostState.RUNNING
                ),

                "elasticsearch",
                HostArchitecture.AMD64, 0, 0,
                "intra.lan", ContainerSize.SMALL,
                "F6E3A586B7B74F704DC40EE2698A25F71EF711F3",
                "Container 1"
        );



        ProxyEnvironment e = new ProxyEnvironment(
                "Mock Env",
                "192.168.1.1/24",
                null,
                3L,
                "5104E12FA7FE92ECEFFCD9C73437F6BCEC556FE4"
        );

        e.setId( id );
        e.setP2PSubnet( "10.11.1.0" );
        e.setVni( 1234567 );
        e.setVersion( 1L );
        e.setStatus( EnvironmentStatus.HEALTHY );
        e.getEnvironmentId();

        HashSet<EnvironmentContainerImpl> set3 = new HashSet<>();
        set3.add( ec );
        set3.add( ec2 );

        e.addContainers( set3 );

        P2PConfig p2PConfig = new P2PConfig( "5104E12FA7FE92ECEFFCD9C73437F6BCEC556FE4", null, null, "10.11.1.1", null, 0 );

        PeerConfImpl peerConf = new PeerConfImpl( p2PConfig );
        peerConf.setId( 10L );

        e.addEnvironmentPeer( peerConf );

        return e;
    }


    public Set<Environment> getEnvironments()
    {
        HashSet<Environment> set = new HashSet<>();

        set.add( get( "729c6e58-3d25-40a9-8abc-e6384ab1d535" ) );

        return set;
    }
}
