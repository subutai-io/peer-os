package org.safehaus.subutai.core.hostregistry.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;

import com.google.common.collect.Sets;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
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

    ListHostsCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new ListHostsCommand( hostRegistry );
        when( hostRegistry.getResourceHostsInfo() ).thenReturn( Sets.newHashSet( resourceHostInfo ) );
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

        assertThat(getSysOut(), containsString(containerHostInfo.toString()));
        assertThat(getSysOut(), containsString(resourceHostInfo.toString()));
    }
}
