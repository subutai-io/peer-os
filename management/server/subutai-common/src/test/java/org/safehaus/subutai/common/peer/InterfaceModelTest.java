package org.safehaus.subutai.common.peer;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.host.Interface;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class InterfaceModelTest
{
    private InterfaceModel interfaceModel;

    @Mock
    Interface anInterface;


    @Before
    public void setUp() throws Exception
    {
        when( anInterface.getMac() ).thenReturn( "testMac" );
        when( anInterface.getInterfaceName() ).thenReturn( "testInterfaceName" );
        when( anInterface.getIp() ).thenReturn( "testIp" );

        interfaceModel = new InterfaceModel( anInterface );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( interfaceModel.getInterfaceName() );
        assertNotNull( interfaceModel.getIp() );
        assertNotNull( interfaceModel.getMac());
        interfaceModel.hashCode();
        interfaceModel.equals( "test" );
        interfaceModel.equals( interfaceModel );
    }
}