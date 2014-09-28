/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.impl;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.core.command.api.command.AbstractCommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentRequestBuilder;
import org.safehaus.subutai.core.command.api.command.CommandException;
import org.safehaus.subutai.core.command.api.command.CommandStatus;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;

import com.jayway.awaitility.Awaitility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.internal.matchers.StringContains.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for CommandImpl class
 */
public class CommandImplTest
{

    private final static String SOME_DUMMY_OUTPUT = "some dummy output";
    private final static String ERR_MSG = "some error message";
    private final static String DESCRIPTION = "some description";

    private final UUID agentUUID = UUID.randomUUID();
    private CommandImpl command;
    private static final int MAX_TIMEOUT = 100;
    private static final int REQUESTS_COUNT = 3;


    @Before
    public void beforeMethod()
    {
        Set<Agent> agents = MockUtils.getAgents( agentUUID );
        RequestBuilder requestBuilder = MockUtils.getRequestBuilder( "pwd", 1, agents );
        command = new CommandImpl( DESCRIPTION, requestBuilder, agents, mock( AbstractCommandRunner.class ) );
    }


    @Test(expected = NullPointerException.class)
    public void constructorShouldFailNullBuilder()
    {
        new CommandImpl( null, mock( Set.class ), mock( AbstractCommandRunner.class ) );
    }


    @Test(expected = NullPointerException.class)
    public void constructorShouldFailNullBuilderBroadcast()
    {
        new CommandImpl( null, 1, mock( AbstractCommandRunner.class ) );
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructorShouldFailZeroRequestsCountBroadcast()
    {
        new CommandImpl( mock( RequestBuilder.class ), 0, mock( AbstractCommandRunner.class ) );
    }


    @Test
    public void shoudlReturnRequestsCount()
    {
        command = new CommandImpl( mock( RequestBuilder.class ), REQUESTS_COUNT, mock( AbstractCommandRunner.class ) );
        assertEquals( REQUESTS_COUNT, command.getRequestsCount() );
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructorShouldFailNullAgentBuilder()
    {
        new CommandImpl( null, null, mock( AbstractCommandRunner.class ) );
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructorShouldFailEmptyAgentBuilder()
    {
        Set<AgentRequestBuilder> ag = new HashSet<>();
        new CommandImpl( null, ag, mock( AbstractCommandRunner.class ) );
    }


    @Test(expected = IllegalArgumentException.class)
    public void constructorShouldFailNullAgents()
    {
        new CommandImpl( null, mock( RequestBuilder.class ), null, mock( AbstractCommandRunner.class ) );
    }


    @Test
    public void shouldReturnSameNumberOfRequestAsAgents()
    {

        assertEquals( 1, command.getRequests().size() );
    }


    @Test
    public void shouldCompleteCommand()
    {

        command.appendResult( MockUtils.getTimedOutResponse( agentUUID, command.getCommandUUID() ) );

        assertTrue( command.hasCompleted() );
    }


    @Test
    public void shouldReturnCompletedRequestsCount()
    {

        command.appendResult( MockUtils.getTimedOutResponse( agentUUID, command.getCommandUUID() ) );

        assertEquals( 1, command.getRequestsCompleted() );
    }


    @Test(expected = CommandException.class)
    public void shouldThrowCommandException() throws CommandException
    {

        command.setCommandStatus( CommandStatus.RUNNING );

        command.execute();
    }


    @Test
    public void shouldSucceedCommandStatus()
    {

        command.appendResult( MockUtils.getSucceededResponse( agentUUID, command.getCommandUUID() ) );

        assertEquals( CommandStatus.SUCCEEDED, command.getCommandStatus() );
        assertTrue( command.hasSucceeded() );
    }


    @Test
    public void shouldReturnAfterCompletion()
    {

        Thread t = new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep( 500 );
                    command.notifyWaitingThreads();
                }
                catch ( InterruptedException e )
                {
                }
            }
        } );
        t.start();

        Awaitility.await().atMost( 1, TimeUnit.SECONDS ).with().pollInterval( 50, TimeUnit.MILLISECONDS ).and()
                  .pollDelay( 100, TimeUnit.MILLISECONDS ).until( new Callable<Boolean>()
        {

            public Boolean call() throws Exception
            {

                command.waitCompletion();
                return true;
            }
        } );
    }


    @Test
    public void shouldReleaseUpdateLock() throws InterruptedException
    {

        Thread t = new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    command.getUpdateLock();
                    Thread.sleep( 500 );
                    command.releaseUpdateLock();
                }
                catch ( InterruptedException e )
                {
                }
            }
        } );
        t.start();
        Thread.sleep( 100 );

        Awaitility.await().atMost( 1, TimeUnit.SECONDS ).with().pollInterval( 50, TimeUnit.MILLISECONDS ).and()
                  .pollDelay( 100, TimeUnit.MILLISECONDS ).until( new Callable<Boolean>()
        {

            public Boolean call() throws Exception
            {

                command.getUpdateLock();
                return true;
            }
        } );
    }


    @Test
    public void shouldReturnSucceedRequestsCount()
    {

        command.appendResult( MockUtils.getSucceededResponse( agentUUID, command.getCommandUUID() ) );

        assertEquals( 1, command.getRequestsSucceeded() );
    }


    @Test
    public void shouldFailCommandStatus()
    {

        command.appendResult( MockUtils.getFailedResponse( agentUUID, command.getCommandUUID(), ERR_MSG ) );

        assertEquals( CommandStatus.FAILED, command.getCommandStatus() );
    }


    @Test
    public void shouldReturnErrMessage()
    {

        command.appendResult( MockUtils.getFailedResponse( agentUUID, command.getCommandUUID(), ERR_MSG ) );

        assertThat( command.getAllErrors(), containsString( ERR_MSG ) );
    }


    @Test
    public void shouldReturnDescription()
    {

        command.appendResult( MockUtils.getFailedResponse( agentUUID, command.getCommandUUID(), ERR_MSG ) );

        assertEquals( DESCRIPTION, command.getDescription() );
    }


    @Test
    public void shouldReturnMaxTimeout()
    {

        Set<AgentRequestBuilder> ag = new HashSet<>();
        ag.add( ( AgentRequestBuilder ) new AgentRequestBuilder( MockUtils.getAgent( UUID.randomUUID() ), "cmd" )
                .withTimeout( MAX_TIMEOUT - 1 ) );
        ag.add( ( AgentRequestBuilder ) new AgentRequestBuilder( MockUtils.getAgent( UUID.randomUUID() ), "cmd" )
                .withTimeout( MAX_TIMEOUT ) );
        command = new CommandImpl( null, ag, mock( AbstractCommandRunner.class ) );

        assertEquals( MAX_TIMEOUT, command.getTimeout() );
    }


    @Test
    public void shouldCollectCommandOutput()
    {

        Response response = MockUtils.getIntermediateResponse( agentUUID, command.getCommandUUID() );
        when( response.getStdOut() ).thenReturn( SOME_DUMMY_OUTPUT );

        command.appendResult( response );

        assertEquals( SOME_DUMMY_OUTPUT, command.getResults().get( agentUUID ).getStdOut() );
    }


    @Test
    public void shouldCollectAllCommandOutput()
    {

        Response response = MockUtils.getIntermediateResponse( agentUUID, command.getCommandUUID() );
        when( response.getStdOut() ).thenReturn( SOME_DUMMY_OUTPUT );

        command.appendResult( response );
        command.appendResult( response );

        assertEquals( SOME_DUMMY_OUTPUT + SOME_DUMMY_OUTPUT, command.getResults().get( agentUUID ).getStdOut() );
    }
}
