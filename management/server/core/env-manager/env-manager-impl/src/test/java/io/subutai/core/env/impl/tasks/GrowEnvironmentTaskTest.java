package io.subutai.core.env.impl.tasks;


import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.environment.EnvironmentModificationException;
import org.safehaus.subutai.common.environment.Topology;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.ExceptionUtil;
import io.subutai.core.env.impl.EnvironmentManagerImpl;
import io.subutai.core.env.impl.TestUtil;
import io.subutai.core.env.impl.entity.EnvironmentImpl;
import io.subutai.core.env.impl.exception.EnvironmentBuildException;
import io.subutai.core.env.impl.exception.ResultHolder;

import com.google.common.collect.Sets;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class GrowEnvironmentTaskTest
{
    @Mock
    EnvironmentManagerImpl environmentManager;
    @Mock
    EnvironmentImpl environment;
    @Mock
    Topology topology;
    @Mock
    ResultHolder<EnvironmentModificationException> resultHolder;
    @Mock
    ContainerHost containerHost;
    @Mock
    TrackerOperation op;
    @Mock
    Semaphore semaphore;
    @Mock
    ExceptionUtil exceptionUtil;
    @Mock
    EnvironmentBuildException environmentBuildException;
    @Mock
    EnvironmentModificationException environmentModificationException;

    GrowEnvironmentTask task;


    @Before
    public void setUp() throws Exception
    {
        task = new GrowEnvironmentTask( environmentManager, environment, topology, resultHolder,
                Sets.newHashSet( containerHost ), op );
        task.semaphore = semaphore;
        task.exceptionUtil = exceptionUtil;
        when( environment.getSshKey() ).thenReturn( TestUtil.SSH_KEY );
    }


    @Test
    public void testWaitCompletion() throws Exception
    {
        task.waitCompletion();

        verify( semaphore ).acquire();
    }


    @Test
    public void testRun() throws Exception
    {
        task.run();

        verify( op ).addLogDone( anyString() );

        doThrow( environmentBuildException ).when( environmentManager ).build( environment, topology );
        when( resultHolder.getResult() ).thenReturn( environmentModificationException );

        task.run();

        verify( resultHolder ).setResult( isA( EnvironmentModificationException.class ) );
    }
}
