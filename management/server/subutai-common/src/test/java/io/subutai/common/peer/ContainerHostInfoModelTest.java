package io.subutai.common.peer;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.host.HostInfo;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.Interface;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ContainerHostInfoModelTest
{
    private ContainerHostInfoModel containerHostInfoModel;

    @Mock
    HostInfo hostInfo;
    @Mock
    ContainerHost containerHost;
    @Mock
    Interface anInterface;


    @Before
    public void setUp() throws Exception
    {
        Set<Interface> mySet = new HashSet<>();
        mySet.add( anInterface );

        when( containerHost.getId() ).thenReturn( UUID.randomUUID().toString() );
        when( containerHost.getHostname() ).thenReturn( "testHostName" );
        when( containerHost.getArch() ).thenReturn( null );
        when( containerHost.getInterfaces() ).thenReturn( mySet );
        when( anInterface.getName() ).thenReturn( "testInterFace" );
        when( anInterface.getIp() ).thenReturn( "testIp" );
        when( anInterface.getMac() ).thenReturn( "testMac" );
        when( hostInfo.getHostname() ).thenReturn( "testHostName" );
        when( hostInfo.getInterfaces() ).thenReturn( mySet );

        containerHostInfoModel = new ContainerHostInfoModel( hostInfo );
        containerHostInfoModel = new ContainerHostInfoModel( containerHost );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( containerHostInfoModel.getArch() );
        assertNotNull( containerHostInfoModel.getHostname() );
        assertNotNull( containerHostInfoModel.getId() );
        assertNotNull( containerHostInfoModel.getInterfaces() );
        containerHostInfoModel.compareTo( hostInfo );
        containerHostInfoModel.hashCode();
        containerHostInfoModel.equals( "test" );
        containerHostInfoModel.equals( containerHostInfoModel );
    }
}