package org.safehaus.subutai.core.network.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandException;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for NetworkManagerImpl
 */
public class NetworkManagerImplTest
{

    private static final String DOMAIN = "domain";
    private static final String ERR_MSG = "oops";
    private static final String KEY = "key";
    private CommandRunner commandRunner;
    private NetworkManagerImpl networkManager;
    private Command command;
    private Agent agent1, agent2;


    @Before
    public void setUp()
    {

        agent1 = MockUtils.getAgent( UUID.randomUUID(), "hostname1", "127.0.0.1" );
        agent2 = MockUtils.getAgent( UUID.randomUUID(), "hostname2", "127.0.0.2" );
        Map<UUID, AgentResult> results = new HashMap<>();
        AgentResult agentResult = mock( AgentResult.class );
        when( agentResult.getStdOut() ).thenReturn( KEY );
        results.put( agent1.getUuid(), agentResult );
        results.put( agent2.getUuid(), agentResult );
        commandRunner = mock( CommandRunner.class );
        command = mock( Command.class );
        when( command.hasSucceeded() ).thenReturn( true );
        when( command.hasCompleted() ).thenReturn( true );
        when( command.getResults() ).thenReturn( results );
        when( commandRunner.createCommand( any( RequestBuilder.class ), anySet() ) ).thenReturn( command );
        when( commandRunner.createCommand( anyString(), any( RequestBuilder.class ), anySet() ) ).thenReturn( command );
        networkManager = new NetworkManagerImpl( commandRunner );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullCommandRunner()
    {

        new NetworkManagerImpl( null );
    }


    @Test
    public void shouldSucceedSsh()
    {

        boolean result = networkManager.configSshOnAgents( Lists.newArrayList( agent1 ) );

        assertTrue( result );
    }


    @Test
    public void shouldSucceedSsh2()
    {

        boolean result = networkManager.configSshOnAgents( Lists.newArrayList( agent1 ), agent2 );

        assertTrue( result );
    }


    @Test( expected = IllegalArgumentException.class )
    public void shouldFailOnEmptyAgents()
    {

        networkManager.configSshOnAgents( Lists.<Agent>newArrayList() );
    }


    @Test( expected = IllegalArgumentException.class )
    public void shouldFailOnEmptyAgents2()
    {

        networkManager.configSshOnAgents( Lists.<Agent>newArrayList(), agent2 );
    }


    @Test
    public void shouldFailOnCommandExceptionSsh() throws CommandException
    {

        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( command ).execute();
        boolean result = networkManager.configSshOnAgents( Lists.newArrayList( agent1 ) );
        boolean result2 = networkManager.configSshOnAgents( Lists.newArrayList( agent1 ), agent2 );

        assertFalse( result );
        assertFalse( result2 );
    }


    @Test
    public void shouldSucceedHosts()
    {

        boolean result = networkManager.configHostsOnAgents( Lists.newArrayList( agent1 ), DOMAIN );

        assertTrue( result );
    }


    @Test
    public void shouldSucceedHosts2()
    {

        boolean result = networkManager.configHostsOnAgents( Lists.newArrayList( agent1 ), agent2, DOMAIN );

        assertTrue( result );
    }


    @Test
    public void shouldFailOnCommandExceptionHosts() throws CommandException
    {
        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( command ).execute();

        boolean result = networkManager.configHostsOnAgents( Lists.newArrayList( agent1 ), agent2, DOMAIN );

        assertFalse( result );
    }


    @Test
    public void shouldFailOnCreateSshCommand() throws CommandException
    {
        Command errCommand = mock( Command.class );
        when( commandRunner.createCommand( eq( new RequestBuilder( "rm -Rf /root/.ssh && " +
                "mkdir -p /root/.ssh && " +
                "chmod 700 /root/.ssh && " +
                "ssh-keygen -t dsa -P '' -f /root/.ssh/id_dsa" ) ), anySet() ) ).thenReturn( errCommand );

        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( errCommand ).execute();

        networkManager.configSshOnAgents( Lists.newArrayList( agent1 ) );
    }


    @Test
    public void shouldFailOnReadSshCommand() throws CommandException
    {
        Command errCommand = mock( Command.class );
        when( commandRunner.createCommand( eq( new RequestBuilder( "cat /root/.ssh/id_dsa.pub" ) ), anySet() ) )
                .thenReturn( errCommand );

        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( errCommand ).execute();

        networkManager.configSshOnAgents( Lists.newArrayList( agent1 ) );
    }


    @Test
    public void shouldFailOnWriteSshCommand() throws CommandException
    {
        Command errCommand = mock( Command.class );
        when( commandRunner.createCommand( eq( new RequestBuilder( String.format( "mkdir -p /root/.ssh && " +
                "chmod 700 /root/.ssh && " +
                "echo '%s' > /root/.ssh/authorized_keys && " +
                "chmod 644 /root/.ssh/authorized_keys", KEY ) ) ), anySet() ) ).thenReturn( errCommand );

        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( errCommand ).execute();

        networkManager.configSshOnAgents( Lists.newArrayList( agent1 ) );
    }


    @Test
    public void shouldFailOnConfigSshCommand() throws CommandException
    {
        Command errCommand = mock( Command.class );
        when( commandRunner.createCommand( eq( new RequestBuilder( "echo 'Host *' > /root/.ssh/config && " +
                "echo '    StrictHostKeyChecking no' >> /root/.ssh/config && " +
                "chmod 644 /root/.ssh/config" ) ), anySet() ) ).thenReturn( errCommand );

        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( errCommand ).execute();

        networkManager.configSshOnAgents( Lists.newArrayList( agent1 ) );
    }
}
