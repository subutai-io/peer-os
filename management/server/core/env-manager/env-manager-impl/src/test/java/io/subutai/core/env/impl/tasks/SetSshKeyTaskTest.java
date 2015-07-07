package io.subutai.core.env.impl.tasks;


import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.environment.EnvironmentModificationException;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import io.subutai.core.env.impl.TestUtil;
import io.subutai.core.env.impl.entity.EnvironmentImpl;
import io.subutai.core.env.impl.exception.ResultHolder;

import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.network.api.NetworkManagerException;

import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class SetSshKeyTaskTest
{
    @Mock
    EnvironmentImpl environment;
    @Mock
    NetworkManager networkManager;
    @Mock
    ResultHolder<EnvironmentModificationException> resultHolder;
    @Mock
    TrackerOperation op;
    @Mock
    Semaphore semaphore;

    SetSshKeyTask task;


    @Before
    public void setUp() throws Exception
    {
        task = new SetSshKeyTask( environment, networkManager, resultHolder, TestUtil.SSH_KEY, op );
        task.semaphore = semaphore;
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
        when( environment.getSshKey() ).thenReturn( TestUtil.SSH_KEY );

        task.run();

        verify( networkManager ).replaceSshKeyInAuthorizedKeys( anySet(), anyString(), anyString() );

        when( environment.getSshKey() ).thenReturn( null );

        task.run();

        verify( networkManager ).addSshKeyToAuthorizedKeys( anySet(), anyString() );

        task = new SetSshKeyTask( environment, networkManager, resultHolder, null, op );
        when( environment.getSshKey() ).thenReturn( TestUtil.SSH_KEY );


        task.run();

        verify( networkManager ).removeSshKeyFromAuthorizedKeys( anySet(), anyString() );

        doThrow( new NetworkManagerException( "" ) ).when( networkManager )
                                                    .removeSshKeyFromAuthorizedKeys( anySet(), anyString() );

        task.run();

        verify( op ).addLogFailed( anyString() );
    }
}
