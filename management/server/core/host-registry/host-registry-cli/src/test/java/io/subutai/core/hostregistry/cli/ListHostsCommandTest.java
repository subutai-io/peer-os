package io.subutai.core.hostregistry.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.hostregistry.api.HostRegistry;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ListHostsCommandTest extends SystemOutRedirectTest
{

    @Mock
    HostRegistry hostRegistry;
    @Mock
    ResourceHostInfo resourceHostInfo;
    @Mock
    ContainerHostInfo containerHostInfo;
    @Mock
    HostInterfaces hostInterfaces;
    @Mock
    HostInterfaceModel hostInterface;

    ListHostsCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new ListHostsCommand( hostRegistry );
        when( hostRegistry.getResourceHostsInfo() ).thenReturn( Sets.newHashSet( resourceHostInfo ) );
        when( resourceHostInfo.getHostInterfaces() ).thenReturn( hostInterfaces );
        when( containerHostInfo.getHostInterfaces() ).thenReturn( hostInterfaces );
        when( hostInterfaces.getAll() ).thenReturn( Sets.newHashSet( hostInterface ) );
        when( resourceHostInfo.getContainers() ).thenReturn( Sets.newHashSet( containerHostInfo ) );
        when( hostInterfaces.findByName( anyString() ) ).thenReturn( hostInterface );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructor() throws Exception
    {
        new ListHostsCommand( null );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        verify( resourceHostInfo ).getHostInterfaces();
        verify( containerHostInfo ).getState();
    }
}
