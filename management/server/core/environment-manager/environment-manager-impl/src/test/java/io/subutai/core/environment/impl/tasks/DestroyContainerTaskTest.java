package io.subutai.core.environment.impl.tasks;


import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;
import io.subutai.core.environment.impl.exception.ResultHolder;

import com.google.common.collect.Sets;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class DestroyContainerTaskTest
{
    @Mock
    EnvironmentManagerImpl environmentManager;
    @Mock
    EnvironmentImpl environment;
    @Mock
    EnvironmentContainerImpl containerHost;
    @Mock
    ContainerHost containerHost2;
    @Mock
    ResultHolder<EnvironmentModificationException> resultHolder;
    @Mock
    TrackerOperation op;
    @Mock
    Semaphore semaphore;
    @Mock
    PeerException peerException;
    @Mock
    EnvironmentNotFoundException environmentNotFoundException;

    DestroyContainerTask task;


    @Before
    public void setUp() throws Exception
    {
        task = new DestroyContainerTask( environmentManager, environment, containerHost, false, resultHolder, op );
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
        task.run();

        verify( op ).addLogDone( anyString() );
        verify( semaphore ).release();

        doThrow( peerException ).when( containerHost ).destroy();
        task.forceMetadataRemoval = true;

        task.run();

        verify( resultHolder ).setResult( isA( EnvironmentModificationException.class ) );


        task.forceMetadataRemoval = false;

        task.run();

        verify( op ).addLogFailed( anyString() );

        reset( containerHost );

        when( environment.getContainerHosts() ).thenReturn( Sets.newHashSet( containerHost2 ) );

        task.run();

        verify( environmentManager, times( 2 ) ).removeEnvironment( any( UUID.class ), anyBoolean() );


        when( environment.getContainerHosts() ).thenReturn( Sets.<ContainerHost>newHashSet() );

        doThrow( environmentNotFoundException ).when( environmentManager )
                                               .removeEnvironment( any( UUID.class ), anyBoolean() );
        task.run();

        verify( environmentNotFoundException ).printStackTrace( any( PrintStream.class ) );
    }
}
