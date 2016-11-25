package io.subutai.core.environment.impl.workflow.modification.steps;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;
import io.subutai.core.environment.impl.workflow.modification.steps.helpers.RenameContainerTask;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class ChangeHostnameStepTest
{

    ChangeHostnameStep step;

    @Mock
    EnvironmentManagerImpl environmentManager;
    LocalEnvironment environment = TestHelper.ENVIRONMENT();
    private static final String NEW_HOSTNAME = "new";

    @Mock
    TaskUtil<Object> taskUtil;
    @Mock
    TaskUtil.TaskResults taskResults;
    @Mock
    TaskUtil.TaskResult taskResult;
    EnvironmentContainerImpl environmentContainer = TestHelper.ENV_CONTAINER();
    @Mock
    RenameContainerTask task;


    @Before
    public void setUp() throws Exception
    {
        step = new ChangeHostnameStep( environmentManager, environment, TestHelper.CONT_HOST_ID, NEW_HOSTNAME );
        step.renameUtil = taskUtil;
        TestHelper.bind( taskUtil, taskResults, taskResult );
        doReturn( environmentContainer ).when( taskResult ).getResult();
        doReturn( task ).when( taskResult ).getTask();
        doReturn( NEW_HOSTNAME ).when( task ).getNewHostname();
        doReturn( TestHelper.HOSTNAME ).when( task ).getOldHostname();
    }


    @Test( expected = EnvironmentModificationException.class )
    public void testExecute() throws Exception
    {
        doReturn( true ).when( taskResult ).hasSucceeded();

        step.execute();

        verify( environmentManager ).loadEnvironment( TestHelper.ENV_ID );


        assertEquals( NEW_HOSTNAME, step.getNewHostname() );

        assertNotSame( NEW_HOSTNAME, step.getOldHostname() );


        doReturn( false ).when( taskResult ).hasSucceeded();

        step.execute();
    }
}
