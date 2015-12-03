package io.subutai.core.localpeer.impl.container;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hostregistry.api.HostRegistry;

import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class CreateContainerWrapperTaskTest
{
    @Mock
    ResourceHost resourceHost;
    private static final String HOSTNAME = "hostname";
    private static final String IP = "192.168.1.0/24";
    private static final int VLAN = 100;
    private static final String ENV_ID = "123";
    private static final String TEMPLATE_NAME = "master";
    private static final int TIMEOUT = 30;

    @Mock
    private HostRegistry hostRegistry;

    CreateContainerWrapperTask task;


    @Before
    public void setUp() throws Exception
    {
        task = new CreateContainerWrapperTask( resourceHost, TEMPLATE_NAME, HOSTNAME, IP, VLAN, TIMEOUT, ENV_ID );
    }


    @Test
    public void testCall() throws Exception
    {
        task.call();

        verify( resourceHost ).createContainer( TEMPLATE_NAME, HOSTNAME, IP, VLAN, TIMEOUT, ENV_ID );
    }
}
