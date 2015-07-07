package io.subutai.core.peer.impl.tasks;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.network.Vni;
import org.safehaus.subutai.common.peer.PeerException;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.impl.entity.ManagementHostEntity;
import io.subutai.core.peer.impl.tasks.SetupTunnelsTask;

import com.google.common.collect.Sets;

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
    private static final UUID ENV_ID = UUID.randomUUID();
    private static final String PEER_IP = "127.0.0.1";
    @Mock
    NetworkManager networkManager;
    @Mock
    ManagementHostEntity managementHostEntity;
    @Mock
    Vni vni;


    SetupTunnelsTask task;


    @Before
    public void setUp() throws Exception
    {
        task = new SetupTunnelsTask( networkManager, managementHostEntity, ENV_ID, Sets.newHashSet( PEER_IP ) );
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

        when( managementHostEntity.findVniByEnvironmentId( ENV_ID ) ).thenReturn( vni );
        when( managementHostEntity.findTunnel( anyString(), anySet() ) ).thenReturn( -1 );

        task.call();

        verify( managementHostEntity ).setupVniVlanMapping( anyInt(), anyLong(), anyInt(), any( UUID.class ) );
    }
}
