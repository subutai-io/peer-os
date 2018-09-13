package io.subutai.core.environment.impl.workflow.modification.steps;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.environment.impl.workflow.modification.steps.helpers.ContainerDestroyTask;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class DestroyContainersStepTest
{
    DestroyContainersStep step;

    LocalEnvironment environment = TestHelper.ENVIRONMENT();
    @Mock
    TaskUtil<Object> taskUtil;
    @Mock
    TaskUtil.TaskResults taskResults;
    @Mock
    TaskUtil.TaskResult taskResult;
    @Mock
    EnvironmentManagerImpl environmentManager;
    TrackerOperation trackerOperation = TestHelper.TRACKER_OPERATION();
    @Mock
    ContainerDestroyTask task;
    EnvironmentContainerImpl environmentContainer = TestHelper.ENV_CONTAINER();

    @Mock
    RelationManager relationManager;


    @Before
    public void setUp() throws Exception
    {
        step = new DestroyContainersStep( environment, environmentManager, Sets.newHashSet( TestHelper.CONTAINER_ID ),
                trackerOperation );
        step.destroyUtil = taskUtil;
        TestHelper.bind( taskUtil, taskResults, taskResult );
        doReturn( task ).when( taskResult ).getTask();
        doReturn( environmentContainer ).when( task ).getContainerHost();
        doReturn( environmentContainer ).when( environment ).getContainerHostById( TestHelper.CONTAINER_ID );

        doReturn( relationManager ).when( environmentManager ).getRelationManager();
    }


    @Test
    public void testExecute() throws Exception
    {
        doReturn( true ).when( taskResult ).hasSucceeded();

        step.execute();

        verify( environmentManager ).loadEnvironment( TestHelper.ENV_ID );

        doReturn( false ).when( taskResult ).hasSucceeded();

        step.execute();

        verify( environmentManager, times( 2 ) ).loadEnvironment( TestHelper.ENV_ID );
    }
}
