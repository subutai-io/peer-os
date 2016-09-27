package io.subutai.core.environment.impl;


import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.Node;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.security.api.crypto.KeyManager;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class TestHelper
{
    public static final String SUBNET_CIDR = "192.168.0.1/24";
    public static final String CONTAINER_ID = "123";
    public static final String PEER_NAME = "123";
    public static final String SSH_KEY = "123";
    public static final String RH_ID = "123";
    public static final String PEER_ID = "123";
    public static final String ENV_ID = "123";
    public static final String OWNER_ID = "123";
    public static final String TEMPLATE_ID = "123";
    public static final String HOSTNAME = "hostname";
    public static final String CONTAINER_NAME = "container";
    public static final String MESSAGE = "msg";
    public static final EnvironmentId ENVIRONMENT_ID = new EnvironmentId( ENV_ID );


    public static EnvironmentImpl ENVIRONMENT()
    {
        EnvironmentImpl ENVIRONMENT = mock( EnvironmentImpl.class );
        doReturn( new EnvironmentId( ENV_ID ) ).when( ENVIRONMENT ).getEnvironmentId();
        doReturn( ENV_ID ).when( ENVIRONMENT ).getId();
        doReturn( SUBNET_CIDR ).when( ENVIRONMENT ).getSubnetCidr();


        return ENVIRONMENT;
    }


    public static KeyManager KEY_MANAGER()
    {
        return mock( KeyManager.class );
    }


    public static Node NODE()
    {
        Node NODE = mock( Node.class );
        doReturn( ContainerSize.SMALL ).when( NODE ).getType();
        doReturn( TEMPLATE_ID ).when( NODE ).getTemplateId();
        doReturn( RH_ID ).when( NODE ).getHostId();
        doReturn( HOSTNAME ).when( NODE ).getHostname();
        doReturn( CONTAINER_NAME ).when( NODE ).getName();

        return NODE;
    }


    public static TrackerOperation TRACKER_OPERATION()
    {
        return mock( TrackerOperation.class );
    }


    public static Peer PEER()
    {
        Peer PEER = mock( Peer.class );
        doReturn( PEER_ID ).when( PEER ).getId();
        doReturn( PEER_NAME ).when( PEER ).getName();

        return PEER;
    }


    public static LocalPeer LOCAL_PEER()
    {
        LocalPeer LOCAL_PEER = mock( LocalPeer.class );
        doReturn( PEER_ID ).when( LOCAL_PEER ).getId();
        doReturn( PEER_NAME ).when( LOCAL_PEER ).getName();
        doReturn( OWNER_ID ).when( LOCAL_PEER ).getOwnerId();


        return LOCAL_PEER;
    }


    public static void bind( Environment environment, Peer peer, PeerUtil peerUtil,
                             PeerUtil.PeerTaskResults peerTaskResults, PeerUtil.PeerTaskResult peerTaskResult )
            throws PeerException
    {

        doReturn( Sets.newHashSet( peer ) ).when( environment ).getPeers();
        doReturn( peerTaskResults ).when( peerUtil ).executeParallel();
        doReturn( Sets.newHashSet( peerTaskResult ) ).when( peerTaskResults ).getPeerTaskResults();
        doReturn( peer ).when( peerTaskResult ).getPeer();
    }
}
