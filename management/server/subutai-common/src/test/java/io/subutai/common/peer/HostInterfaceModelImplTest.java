package io.subutai.common.peer;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterface;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class HostInterfaceModelImplTest
{
    private HostInterfaceModel interfaceModel;

    @Mock
    HostInterface anHostInterface;


    @Before
    public void setUp() throws Exception
    {
        when( anHostInterface.getMac() ).thenReturn( "testMac" );
        when( anHostInterface.getName() ).thenReturn( "testInterfaceName" );
        when( anHostInterface.getIp() ).thenReturn( "testIp" );

        interfaceModel = new HostInterfaceModel( anHostInterface );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( interfaceModel.getName() );
        assertNotNull( interfaceModel.getIp() );
        assertNotNull( interfaceModel.getMac());
        interfaceModel.hashCode();
        interfaceModel.equals( "test" );
        interfaceModel.equals( interfaceModel );
    }
}