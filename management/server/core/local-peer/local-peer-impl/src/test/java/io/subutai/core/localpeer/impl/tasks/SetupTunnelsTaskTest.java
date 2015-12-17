package io.subutai.core.localpeer.impl.tasks;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.network.Vni;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.core.localpeer.impl.entity.ManagementHostEntity;
import io.subutai.core.network.api.NetworkManager;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class SetupTunnelsTaskTest
{
    private static final String ENV_ID = UUID.randomUUID().toString();
    private static final String PEER_IP = "127.0.0.1";
    private static final String N2N_IP = "10.11.0.1";
    @Mock
    NetworkManager networkManager;
    @Mock
    LocalPeer managementHostEntity;
    @Mock
    Vni vni;


    SetupTunnelsTask task;


    @Before
    public void setUp() throws Exception
    {
        Map<String, String> m = new HashMap<>();
        m.put( PEER_IP, N2N_IP );
        task = new SetupTunnelsTask( networkManager, managementHostEntity, ENV_ID, m );
    }


    @Test
    public void testCall() throws Exception
    {
        try
        {

            task.call();
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }

//        when( managementHostEntity.findVniByEnvironmentId( ENV_ID ) ).thenReturn( vni );
//        when( managementHostEntity.findTunnel( anyString(), anySet() ) ).thenReturn( -1 );
//
//        task.call();
//
//        verify( managementHostEntity ).setupVniVlanMapping( anyInt(), anyLong(), anyInt(), any( String.class ) );
    }
}
