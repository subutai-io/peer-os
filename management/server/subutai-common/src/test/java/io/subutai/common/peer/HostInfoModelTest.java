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
import io.subutai.common.host.HostInfoModel;
import io.subutai.common.host.HostInterface;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class HostInfoModelTest
{
    private HostInfoModel hostInfoModel;

    @Mock
    HostInfo hostInfo;
    @Mock
    ContainerHost containerHost;
    @Mock
    HostInterface anInterface;


    @Before
    public void setUp() throws Exception
    {
        Set<HostInterface> mySet = new HashSet<>();
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

        hostInfoModel = new HostInfoModel( hostInfo );
        hostInfoModel = new HostInfoModel( containerHost );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( hostInfoModel.getArch() );
        assertNotNull( hostInfoModel.getHostname() );
        assertNotNull( hostInfoModel.getId() );
        assertNotNull( hostInfoModel.getInterfaces() );
        hostInfoModel.compareTo( hostInfo );
        hostInfoModel.hashCode();
        hostInfoModel.equals( "test" );
        hostInfoModel.equals( hostInfoModel );
    }
}