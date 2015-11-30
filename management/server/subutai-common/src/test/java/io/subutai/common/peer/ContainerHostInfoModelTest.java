package io.subutai.common.peer;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ContainerHostInfoModelTest
{
    private ContainerHostInfoModel containerHostInfoModel;

    @Mock
    ContainerHostInfo hostInfo;
    @Mock
    ContainerHost containerHost;
    @Mock
    HostInterfaceModel anHostInterface;
    @Mock
    HostInterfaces hostInterfaces;


    @Before
    public void setUp() throws Exception
    {
        Set<HostInterfaceModel> mySet = new HashSet<>();
        mySet.add( anHostInterface );

        when( containerHost.getId() ).thenReturn( UUID.randomUUID().toString() );
        when( containerHost.getHostname() ).thenReturn( "testHostName" );
        when( containerHost.getArch() ).thenReturn( null );
        when( hostInterfaces.getAll() ).thenReturn( mySet );
        when( containerHost.getHostInterfaces() ).thenReturn( hostInterfaces );
        when( anHostInterface.getName() ).thenReturn( "testInterFace" );
        when( anHostInterface.getIp() ).thenReturn( "testIp" );
        when( anHostInterface.getMac() ).thenReturn( "testMac" );
        when( hostInfo.getHostname() ).thenReturn( "testHostName" );
        when( hostInfo.getHostInterfaces() ).thenReturn( hostInterfaces );

        containerHostInfoModel = new ContainerHostInfoModel( hostInfo );
        containerHostInfoModel = new ContainerHostInfoModel( containerHost );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( containerHostInfoModel.getArch() );
        assertNotNull( containerHostInfoModel.getHostname() );
        assertNotNull( containerHostInfoModel.getId() );
        assertNotNull( containerHostInfoModel.getHostInterfaces() );
        containerHostInfoModel.compareTo( hostInfo );
        containerHostInfoModel.hashCode();
        containerHostInfoModel.equals( "test" );
        containerHostInfoModel.equals( containerHostInfoModel );
    }
}