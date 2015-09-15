package io.subutai.core.env.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.env.api.EnvironmentManager;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class DestroyContainerCommandTest extends SystemOutRedirectTest
{
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    Environment environment;
    @Mock
    ContainerHost containerHost;

    private DestroyContainerCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new DestroyContainerCommand( environmentManager );
        command.async = TestUtil.ASYNC;
        command.containerIdStr = TestUtil.CONTAINER_ID;
        command.forceMetadataRemoval = TestUtil.FORCE;
        when( containerHost.getId() ).thenReturn( TestUtil.CONTAINER_ID );
        when( environmentManager.getEnvironments() ).thenReturn( Sets.newHashSet( environment ) );
        when( environment.getContainerHosts() ).thenReturn( Sets.newHashSet( containerHost ) );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        verify( environmentManager )
                .destroyContainer( environment.getId(), containerHost.getId(), TestUtil.ASYNC, TestUtil.FORCE );

        when( environment.getContainerHosts() ).thenReturn( Sets.<ContainerHost>newHashSet() );

        resetSysOut();

        command.doExecute();

        assertEquals( "Container environment not found", getSysOut() );
    }
}
