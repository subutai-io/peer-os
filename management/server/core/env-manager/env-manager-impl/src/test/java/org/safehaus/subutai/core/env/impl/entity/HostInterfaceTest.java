package org.safehaus.subutai.core.env.impl.entity;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.host.Interface;
import org.safehaus.subutai.core.env.impl.TestUtil;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class HostInterfaceTest
{
    @Mock
    Interface anInterface;
    @Mock
    EnvironmentContainerImpl environmentContainer;

    HostInterface hostInterface;


    @Before
    public void setUp() throws Exception
    {
        when( anInterface.getIp() ).thenReturn( TestUtil.IP );
        when( anInterface.getInterfaceName() ).thenReturn( TestUtil.INTERFACE_NAME );
        when( anInterface.getMac() ).thenReturn( TestUtil.MAC );

        hostInterface = new HostInterface( anInterface );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( TestUtil.IP, hostInterface.getIp() );

        assertEquals( TestUtil.INTERFACE_NAME, hostInterface.getInterfaceName() );
        assertEquals( TestUtil.MAC, hostInterface.getMac() );
    }


    @Test
    public void testGettersNSetters() throws Exception
    {
        hostInterface = new HostInterface();

        hostInterface.setHost( environmentContainer );

        assertEquals( environmentContainer, hostInterface.getHost() );

        hostInterface.setIp( TestUtil.IP );

        assertEquals( TestUtil.IP, hostInterface.getIp() );

        hostInterface.setId( TestUtil.INTERFACE_ID );

        assertEquals( TestUtil.INTERFACE_ID, hostInterface.getId() );

        hostInterface.setInterfaceName( TestUtil.INTERFACE_NAME );

        assertEquals( TestUtil.INTERFACE_NAME, hostInterface.getInterfaceName() );

        hostInterface.setMac( TestUtil.MAC );

        assertEquals( TestUtil.MAC, hostInterface.getMac() );
    }
}
