package io.subutai.core.peer.impl.tasks;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.network.Gateway;
import io.subutai.common.peer.PeerException;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.peer.api.ManagementHost;

import static junit.framework.TestCase.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CreateGatewayTaskTest
{
    private static final String GATEWAY_IP = "127.0.0.1";
    private static final int VLAN = 100;
    @Mock
    NetworkManager networkManager;
    @Mock
    ManagementHost managementHost;
    @Mock
    Gateway gateway;

    CreateGatewayTask task;


    @Before
    public void setUp() throws Exception
    {
        task = new CreateGatewayTask( GATEWAY_IP, VLAN, networkManager, managementHost );
        when( managementHost.getGateways() ).thenReturn( Sets.newHashSet( gateway ) );
    }


    @Test( expected = PeerException.class )
    public void testCall() throws Exception
    {
        task.call();

        verify( networkManager ).setupGateway( GATEWAY_IP, VLAN );

        when( gateway.getIp() ).thenReturn( GATEWAY_IP );
        when( gateway.getVlan() ).thenReturn( VLAN );

        assertFalse( task.call() );

        doThrow( new NetworkManagerException( "" ) ).when( networkManager ).setupGateway( GATEWAY_IP, VLAN );
        reset( gateway );

        task.call();
    }
}
