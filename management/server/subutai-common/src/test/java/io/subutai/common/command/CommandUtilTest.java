package io.subutai.common.command;


import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.command.Response;
import io.subutai.common.peer.Host;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CommandUtilTest
{
    private CommandUtil commandUtil;
    private Set<Host> mySet;
    @Mock
    RequestBuilder requestBuilder;
    @Mock
    Host host;
    @Mock
    CommandCallback commandCallback;
    @Mock
    CommandResult commandResult;
    @Mock
    CommandUtil.StoppableCallback stoppableCallback;
    @Mock
    Response response;

    @Before
    public void setUp() throws Exception
    {
        mySet = new HashSet<>();
        mySet.add( host );

        commandUtil = new CommandUtil();
    }


    @Test
    public void testExecute() throws Exception
    {
        when( host.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( commandResult.hasSucceeded() ).thenReturn( true );

        commandUtil.execute( requestBuilder, host );
    }


    @Test( expected = CommandException.class )
    public void testExecuteException() throws Exception
    {
        when( host.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( commandResult.hasSucceeded() ).thenReturn( false );

        commandUtil.execute( requestBuilder, host );
    }


    @Test
    public void testExecuteAsync() throws Exception
    {
        commandUtil.executeAsync( requestBuilder, host, stoppableCallback );
    }


    @Test
    public void testExecuteAsync1() throws Exception
    {
        commandUtil.executeAsync( requestBuilder, mySet, commandCallback );
    }


    @Test
    public void testExecuteSequential() throws Exception
    {
        when( host.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );

        commandUtil.executeSequential( requestBuilder, mySet );
    }


    @Test
    public void testExecuteParallel() throws Exception
    {
        when( host.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        commandUtil.executeParallel( requestBuilder, mySet );
    }
}