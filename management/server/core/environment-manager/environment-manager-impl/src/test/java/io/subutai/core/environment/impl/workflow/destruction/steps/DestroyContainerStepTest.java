package io.subutai.core.environment.impl.workflow.destruction.steps;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;

import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class DestroyContainerStepTest
{
    DestroyContainerStep step;


    @Mock
    EnvironmentContainerImpl containerHost;


    @Before
    public void setUp() throws Exception
    {
        step = new DestroyContainerStep( containerHost );
    }


    @Test
    public void testExecute() throws Exception
    {
        step.execute();

        verify( containerHost ).destroy();
    }
}
