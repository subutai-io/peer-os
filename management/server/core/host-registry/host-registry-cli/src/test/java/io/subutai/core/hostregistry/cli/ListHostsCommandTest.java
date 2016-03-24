package io.subutai.core.hostregistry.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.common.host.ResourceHostInfo;

import com.google.common.collect.Sets;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
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
        when( hostInterfaces.getAll() ).thenReturn( Sets.<HostInterfaceModel>newHashSet(hostInterface) );
        when( resourceHostInfo.getContainers() ).thenReturn( Sets.newHashSet( containerHostInfo ) );
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
