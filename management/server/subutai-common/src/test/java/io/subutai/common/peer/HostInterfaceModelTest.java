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
public class HostInterfaceModelTest
{
    private HostInterfaceModel interfaceModel;

    @Mock
    HostInterface anInterface;


    @Before
    public void setUp() throws Exception
    {
        when( anInterface.getMac() ).thenReturn( "testMac" );
        when( anInterface.getName() ).thenReturn( "testInterfaceName" );
        when( anInterface.getIp() ).thenReturn( "testIp" );

        interfaceModel = new HostInterfaceModel( anInterface );
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