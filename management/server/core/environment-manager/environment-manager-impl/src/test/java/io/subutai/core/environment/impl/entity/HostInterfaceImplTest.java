package io.subutai.core.environment.impl.entity;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.host.HostInterface;
import io.subutai.common.settings.Common;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


@RunWith( MockitoJUnitRunner.class )
public class HostInterfaceImplTest
{

    @Mock
    HostInterface hostIface;

    HostInterfaceImpl hostInterface;


    @Before
    public void setUp() throws Exception
    {

        doReturn( Common.DEFAULT_CONTAINER_INTERFACE ).when( hostIface ).getName();
        doReturn( Common.LOCAL_HOST_IP ).when( hostIface ).getIp();

        hostInterface = new HostInterfaceImpl( hostIface );
    }


    @Test
    public void testGetName() throws Exception
    {
        assertEquals( Common.DEFAULT_CONTAINER_INTERFACE, hostInterface.getName() );
    }


    @Test
    public void testGetIp() throws Exception
    {
        assertEquals( Common.LOCAL_HOST_IP, hostInterface.getIp() );
    }


    @Test
    public void testHost() throws Exception
    {
        EnvironmentContainerImpl host = mock( EnvironmentContainerImpl.class );

        hostInterface.setHost( host );

        assertEquals( host, hostInterface.getHost() );
    }
}
