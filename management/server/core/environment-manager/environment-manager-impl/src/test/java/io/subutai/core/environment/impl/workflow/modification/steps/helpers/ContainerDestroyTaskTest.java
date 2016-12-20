package io.subutai.core.environment.impl.workflow.modification.steps.helpers;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class ContainerDestroyTaskTest
{
    ContainerDestroyTask task;

    EnvironmentContainerImpl container = TestHelper.ENV_CONTAINER();


    @Before
    public void setUp() throws Exception
    {
        task = new ContainerDestroyTask( container );
    }


    @Test
    public void tastCall() throws Exception
    {
        task.call();

        verify( container ).destroy( false );
    }


    @Test
    public void testGetContainerHost() throws Exception
    {
        assertEquals( container, task.getContainerHost() );
    }
}
