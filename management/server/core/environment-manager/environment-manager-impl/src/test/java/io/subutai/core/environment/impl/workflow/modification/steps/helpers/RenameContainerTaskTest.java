package io.subutai.core.environment.impl.workflow.modification.steps.helpers;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentPeerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class RenameContainerTaskTest
{
    RenameContainerTask task;

    LocalEnvironment environment = TestHelper.ENVIRONMENT();
    private static final String NEW_HOSTNAME = "new";
    private static final Integer VLAN = 123;
    @Mock
    EnvironmentPeerImpl environmentPeer;
    EnvironmentContainerImpl environmentContainer = TestHelper.ENV_CONTAINER();


    @Before
    public void setUp() throws Exception
    {
        task = new RenameContainerTask( environment, TestHelper.CONT_HOST_ID, NEW_HOSTNAME );
        doReturn( environmentContainer ).when( environment ).getContainerHostById( TestHelper.CONTAINER_ID );
        doReturn( environmentPeer ).when( environment ).getEnvironmentPeer( TestHelper.PEER_ID );
        doReturn( VLAN ).when( environmentPeer ).getVlan();
    }


    @Test
    public void testCall() throws Exception
    {
        task.call();

        verify( environmentContainer ).setHostname( anyString(), eq( false ) );
    }


    @Test
    public void testHostname() throws Exception
    {
        task.call();

        assertEquals( TestHelper.HOSTNAME, task.getOldHostname() );
        assertNotSame( TestHelper.HOSTNAME, task.getNewHostname() );
    }
}
