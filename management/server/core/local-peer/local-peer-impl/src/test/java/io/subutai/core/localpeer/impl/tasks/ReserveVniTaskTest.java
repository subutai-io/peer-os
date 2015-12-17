package io.subutai.core.localpeer.impl.tasks;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.network.Vni;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.network.api.NetworkManager;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ReserveVniTaskTest
{
    private static final String ENV_ID = UUID.randomUUID().toString();
    private static final int VLAN = 100;
    private static final long VNI = 10000;
    @Mock
    NetworkManager networkManager;
    @Mock
    LocalPeer localPeer;
    @Mock
    Vni vni;

    ReserveVniTask task;


    @Before
    public void setUp() throws Exception
    {
        task = new ReserveVniTask( networkManager, vni, localPeer );
        when( vni.getEnvironmentId() ).thenReturn( ENV_ID );
        when( vni.getVni() ).thenReturn( VNI );
        when( vni.getVlan() ).thenReturn( VLAN );
        when( localPeer.findVniByEnvironmentId( ENV_ID ) ).thenReturn( vni );
//        when( localPeer.findAvailableVlanId() ).thenReturn( VLAN );
    }


    @Test
    public void testCall() throws Exception
    {
        task.call();

        when( localPeer.findVniByEnvironmentId( ENV_ID ) ).thenReturn( null );

        task.call();

        verify( networkManager ).reserveVni( any( Vni.class ) );
    }
}
