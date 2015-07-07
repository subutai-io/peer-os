package io.subutai.core.env.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.env.api.EnvironmentManager;

import com.google.common.collect.Sets;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ViewEnvironmentCommandTest extends SystemOutRedirectTest
{
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    Environment environment;
    @Mock
    ContainerHost containerHost;

    ViewEnvironmentCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new ViewEnvironmentCommand( environmentManager );
        command.environmentId = TestUtil.ENV_ID.toString();
        when( environmentManager.findEnvironment( TestUtil.ENV_ID ) ).thenReturn( environment );
        when( environment.getName() ).thenReturn( TestUtil.ENV_NAME );
        when( environment.getContainerHosts() ).thenReturn( Sets.newHashSet( containerHost ) );
        when( containerHost.getHostname() ).thenReturn( TestUtil.HOSTNAME );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        assertThat( getSysOut(), containsString( TestUtil.ENV_NAME ) );
        assertThat( getSysOut(), containsString( TestUtil.HOSTNAME ) );
    }
}
