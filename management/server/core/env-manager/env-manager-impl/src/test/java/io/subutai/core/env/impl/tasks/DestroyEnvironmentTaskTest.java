package io.subutai.core.env.impl.tasks;


import java.io.PrintStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.EnvironmentStatus;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainersDestructionResult;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.core.env.api.exception.EnvironmentDestructionException;
import io.subutai.core.env.impl.EnvironmentManagerImpl;
import io.subutai.core.env.impl.TestUtil;
import io.subutai.core.env.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.env.impl.entity.EnvironmentImpl;
import io.subutai.core.env.impl.exception.ResultHolder;

import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.ManagementHost;

import com.google.common.collect.Sets;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class DestroyEnvironmentTaskTest
{
    @Mock
    EnvironmentManagerImpl environmentManager;
    @Mock
    EnvironmentImpl environment;
    @Mock
    Set<Throwable> throwables;
    @Mock
    ResultHolder<EnvironmentDestructionException> resultHolder;
    @Mock
    LocalPeer localPeer;
    @Mock
    Semaphore semaphore;
    @Mock
    TrackerOperation op;
    @Mock
    ExceptionUtil exceptionUtil;
    @Mock
    EnvironmentContainerImpl environmentContainer;
    @Mock
    ManagementHost managementHost;
    @Mock
    Peer peer;
    @Mock
    ExecutorService executor;
    @Mock
    Future<ContainersDestructionResult> future;
    @Mock
    ContainersDestructionResult result;
    @Mock
    PeerException peerException;
    @Mock
    ExecutionException executionException;
    @Mock
    EnvironmentNotFoundException environmentNotFoundException;


    DestroyEnvironmentTask task;


    @Before
    public void setUp() throws Exception
    {
        task = spy( new DestroyEnvironmentTask( environmentManager, environment, throwables, resultHolder, false,
                localPeer, op ) );
        task.exceptionUtil = exceptionUtil;
        task.semaphore = semaphore;
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
        when( environmentContainer.getPeer() ).thenReturn( peer );
        when( environment.getId() ).thenReturn( TestUtil.ENV_ID );
        when( task.getExecutor( 1 ) ).thenReturn( executor );
        when( executor.submit( any( Callable.class ) ) ).thenReturn( future );
        when( future.get() ).thenReturn( result );
        when( environmentContainer.getPeerId() ).thenReturn( TestUtil.PEER_ID.toString() );
        when( result.peerId() ).thenReturn( TestUtil.PEER_ID );
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
        when( environment.getStatus() ).thenReturn( EnvironmentStatus.EMPTY );

        task.run();

        verify( environmentManager ).removeEnvironment( any( String.class ), anyBoolean() );

        when( environment.getStatus() ).thenReturn( EnvironmentStatus.HEALTHY );
        when( environment.getContainerHosts() ).thenReturn( Sets.<ContainerHost>newHashSet( environmentContainer ) );
        when( result.getException() ).thenReturn( DestroyEnvironmentTask.CONTAINER_GROUP_NOT_FOUND );
        doThrow( peerException ).when( managementHost ).cleanupEnvironmentNetworkSettings( any( String.class ) );

        task.run();

        verify( managementHost ).cleanupEnvironmentNetworkSettings( any( String.class ) );
        verify( peerException ).printStackTrace( any( PrintStream.class ) );


        doThrow( executionException ).when( future ).get();
        when( exceptionUtil.getRootCause( executionException ) ).thenReturn( executionException );

        task.run();

        verify( exceptionUtil ).getRootCause( executionException );

        when( result.getException() ).thenReturn( TestUtil.ERR_MSG );
        reset( future );
        when( future.get() ).thenReturn( result );
        when( result.getDestroyedContainersIds() ).thenReturn( Sets.newHashSet( TestUtil.CONTAINER_ID ) );
        when( exceptionUtil.getRootCause( peerException ) ).thenReturn( peerException );
        when( peerException.getMessage() ).thenReturn( TestUtil.ERR_MSG );
        when( peer.getName() ).thenReturn( TestUtil.PEER_NAME );
        task.forceMetadataRemoval = true;

        task.run();

        verify( throwables ).add( isA( EnvironmentDestructionException.class ) );


        doThrow( environmentNotFoundException ).when( environmentManager ).removeEnvironment( TestUtil.ENV_ID, false );

        task.run();

        verify( op ).addLogFailed( anyString() );

    }
}
