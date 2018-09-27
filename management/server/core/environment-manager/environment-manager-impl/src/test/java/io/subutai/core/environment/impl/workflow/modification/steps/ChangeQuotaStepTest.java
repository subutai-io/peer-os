package io.subutai.core.environment.impl.workflow.modification.steps;


import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;

import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.quota.ContainerSize;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.environment.impl.TestHelper;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class ChangeQuotaStepTest
{
    ChangeQuotaStep step;


    LocalEnvironment environment = TestHelper.ENVIRONMENT();
    @Mock
    TaskUtil<Object> taskUtil;
    @Mock
    TaskUtil.TaskResults taskResults;
    @Mock
    TaskUtil.TaskResult taskResult;
    EnvironmentContainerImpl environmentContainer = TestHelper.ENV_CONTAINER();
    TrackerOperation trackerOperation = TestHelper.TRACKER_OPERATION();


    @Before
    public void setUp() throws Exception
    {
        Map<String, ContainerQuota> changedContainers = Maps.newHashMap();
        changedContainers.put( TestHelper.CONTAINER_ID, new ContainerQuota( ContainerSize.LARGE ) );
        TestHelper.bind( taskUtil, taskResults, taskResult );
        doReturn( environmentContainer ).when( environment ).getContainerHostById( TestHelper.CONTAINER_ID );

        step = new ChangeQuotaStep( environment, changedContainers, trackerOperation );
        step.quotaUtil = taskUtil;
    }


    @Test( expected = PeerException.class )
    public void testExecute() throws Exception
    {
        doReturn( true ).when( taskResult ).hasSucceeded();

        step.execute();

        verify( trackerOperation ).addLog( anyString() );

        doReturn( true ).when( taskResults ).hasFailures();
        doReturn( false ).when( taskResult ).hasSucceeded();

        step.execute();
    }
}
