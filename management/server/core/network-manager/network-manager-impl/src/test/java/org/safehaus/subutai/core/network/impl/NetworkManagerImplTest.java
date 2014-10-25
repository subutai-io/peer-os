package org.safehaus.subutai.core.network.impl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
    private CommandDispatcher commandDispatcher;
    private NetworkManagerImpl networkManager;
    private Command command;
    private Agent agent1, agent2;
    private Container container1, container2;
    private List<Agent> agents;
    private Set<Container> containers;
    private static final UUID AGENT_ID1 = UUID.randomUUID();
    private static final UUID AGENT_ID2 = UUID.randomUUID();
    private static final String HOSTNAME1 = "hostname1";
    private static final String HOSTNAME2 = "hostname2";
    private static final String IP1 = "127.0.0.1";
    private static final String IP2 = "127.0.0.2";


    @Before
    public void setUp()
    {

        agent1 = MockUtils.getAgent( AGENT_ID1, HOSTNAME1, IP1 );
        agent2 = MockUtils.getAgent( AGENT_ID2, HOSTNAME2, IP2 );
        container1 = MockUtils.getContainer( AGENT_ID1, HOSTNAME1, IP1 );
        container2 = MockUtils.getContainer( AGENT_ID2, HOSTNAME2, IP2 );
        agents = Lists.newArrayList( agent1 );
        containers = Sets.newHashSet( container1 );
        Map<UUID, AgentResult> results = new HashMap<>();
        AgentResult agentResult = mock( AgentResult.class );
        when( agentResult.getStdOut() ).thenReturn( KEY );
        results.put( agent1.getUuid(), agentResult );
        results.put( agent2.getUuid(), agentResult );
        commandDispatcher = mock( CommandDispatcher.class );
        command = mock( Command.class );
        when( command.hasSucceeded() ).thenReturn( true );
        when( command.hasCompleted() ).thenReturn( true );
        when( command.getResults() ).thenReturn( results );
        when( commandDispatcher.createCommand( any( RequestBuilder.class ), anySet() ) ).thenReturn( command );
        when( commandDispatcher.createContainerCommand( any( RequestBuilder.class ), anySet() ) ).thenReturn( command );
        when( commandDispatcher.createCommand( anyString(), any( RequestBuilder.class ), anySet() ) )
                .thenReturn( command );
        networkManager = new NetworkManagerImpl( commandDispatcher );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullCommandRunner()
    {

        new NetworkManagerImpl( null );
    }


    @Test
    public void shouldSucceedSsh()
    {

        boolean result = networkManager.configSshOnAgents( agents );

        assertTrue( result );
    }


    @Test
    public void shouldSucceedSsh2()
    {

        boolean result = networkManager.configSshOnAgents( agents, agent2 );

        assertTrue( result );
    }


    @Test
    public void shouldSucceedSsh3()
    {

        boolean result = networkManager.configSsh( containers );

        assertTrue( result );
    }


    @Test
    public void shouldSucceedSsh4()
    {

        boolean result = networkManager.configSsh( containers, container2 );

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


    @Test( expected = IllegalArgumentException.class )
    public void shouldFailOnEmptyContainers()
    {

        networkManager.configSsh( Sets.<Container>newHashSet() );
    }


    @Test( expected = IllegalArgumentException.class )
    public void shouldFailOnEmptyContainers2()
    {

        networkManager.configSsh( Sets.<Container>newHashSet(), container2 );
    }


    @Test
    public void shouldFailOnCommandExceptionSsh() throws CommandException
    {

        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( command ).execute();
        boolean result = networkManager.configSshOnAgents( agents );
        boolean result2 = networkManager.configSshOnAgents( agents, agent2 );

        assertFalse( result );
        assertFalse( result2 );
    }


    @Test
    public void shouldFailOnCommandExceptionSsh2() throws CommandException
    {

        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( command ).execute();
        boolean result = networkManager.configSsh( containers );
        boolean result2 = networkManager.configSsh( containers, container2 );

        assertFalse( result );
        assertFalse( result2 );
    }


    @Test
    public void shouldSucceedHosts()
    {

        boolean result = networkManager.configHostsOnAgents( agents, DOMAIN );

        assertTrue( result );
    }


    @Test
    public void shouldSucceedHosts2()
    {

        boolean result = networkManager.configHostsOnAgents( agents, agent2, DOMAIN );

        assertTrue( result );
    }


    @Test
    public void shouldSucceedHosts3()
    {

        boolean result = networkManager.configHosts( DOMAIN, containers );

        assertTrue( result );
    }


    @Test
    public void shouldSucceedHosts4()
    {

        boolean result = networkManager.configHosts( DOMAIN, containers, container2 );

        assertTrue( result );
    }


    @Test
    public void shouldFailOnCommandExceptionHosts() throws CommandException
    {
        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( command ).execute();

        boolean result = networkManager.configHostsOnAgents( agents, agent2, DOMAIN );

        assertFalse( result );
    }


    @Test
    public void shouldFailOnCommandExceptionHosts2() throws CommandException
    {
        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( command ).execute();

        boolean result = networkManager.configHosts( DOMAIN, containers, container2 );

        assertFalse( result );
    }


    @Test
    public void shouldFailOnCreateSshCommand() throws CommandException
    {
        Command errCommand = mock( Command.class );
        when( commandDispatcher.createCommand( eq( new RequestBuilder( "rm -Rf /root/.ssh && " +
                "mkdir -p /root/.ssh && " +
                "chmod 700 /root/.ssh && " +
                "ssh-keygen -t dsa -P '' -f /root/.ssh/id_dsa" ) ), anySet() ) ).thenReturn( errCommand );

        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( errCommand ).execute();

        boolean result = networkManager.configSshOnAgents( agents );

        assertFalse( result );
    }


    @Test
    public void shouldFailOnCreateSshCommand2() throws CommandException
    {
        Command errCommand = mock( Command.class );
        when( commandDispatcher.createContainerCommand( eq( new RequestBuilder( "rm -Rf /root/.ssh && " +
                "mkdir -p /root/.ssh && " +
                "chmod 700 /root/.ssh && " +
                "ssh-keygen -t dsa -P '' -f /root/.ssh/id_dsa" ) ), anySet() ) ).thenReturn( errCommand );

        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( errCommand ).execute();

        boolean result = networkManager.configSsh( containers );

        assertFalse( result );
    }


    @Test
    public void shouldFailOnReadSshCommand() throws CommandException
    {
        Command errCommand = mock( Command.class );
        when( commandDispatcher.createCommand( eq( new RequestBuilder( "cat /root/.ssh/id_dsa.pub" ) ), anySet() ) )
                .thenReturn( errCommand );

        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( errCommand ).execute();

        boolean result = networkManager.configSshOnAgents( agents );

        assertFalse( result );
    }


    @Test
    public void shouldFailOnReadSshCommand2() throws CommandException
    {
        Command errCommand = mock( Command.class );
        when( commandDispatcher
                .createContainerCommand( eq( new RequestBuilder( "cat /root/.ssh/id_dsa.pub" ) ), anySet() ) )
                .thenReturn( errCommand );

        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( errCommand ).execute();

        boolean result = networkManager.configSsh( containers );

        assertFalse( result );
    }


    @Test
    public void shouldFailOnReadSshCommand3() throws CommandException
    {
        Command errCommand = mock( Command.class );
        when( commandDispatcher
                .createContainerCommand( eq( new RequestBuilder( "cat /root/.ssh/id_dsa.pub" ) ), anySet() ) )
                .thenReturn( errCommand );

        when( errCommand.hasCompleted() ).thenReturn( false );

        boolean result = networkManager.configSsh( containers );

        assertFalse( result );
    }


    @Test
    public void shouldFailOnWriteSshCommand() throws CommandException
    {
        Command errCommand = mock( Command.class );
        when( commandDispatcher.createCommand( eq( new RequestBuilder( String.format( "mkdir -p /root/.ssh && " +
                "chmod 700 /root/.ssh && " +
                "echo '%s' > /root/.ssh/authorized_keys && " +
                "chmod 644 /root/.ssh/authorized_keys", KEY + KEY ) ) ), anySet() ) ).thenReturn( errCommand );

        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( errCommand ).execute();

        boolean result = networkManager.configSshOnAgents( agents );

        assertFalse( result );
    }


    @Test
    public void shouldFailOnWriteSshCommand2() throws CommandException
    {
        Command errCommand = mock( Command.class );
        when( commandDispatcher
                .createContainerCommand( eq( new RequestBuilder( String.format( "mkdir -p /root/.ssh && " +
                        "chmod 700 /root/.ssh && " +
                        "echo '%s' > /root/.ssh/authorized_keys && " +
                        "chmod 644 /root/.ssh/authorized_keys", KEY + KEY ) ) ), anySet() ) ).thenReturn( errCommand );

        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( errCommand ).execute();

        boolean result = networkManager.configSsh( containers );

        assertFalse( result );
    }


    @Test
    public void shouldFailOnConfigSshCommand() throws CommandException
    {
        Command errCommand = mock( Command.class );
        when( commandDispatcher.createCommand( eq( new RequestBuilder( "echo 'Host *' > /root/.ssh/config && " +
                "echo '    StrictHostKeyChecking no' >> /root/.ssh/config && " +
                "chmod 644 /root/.ssh/config" ) ), anySet() ) ).thenReturn( errCommand );

        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( errCommand ).execute();

        boolean result = networkManager.configSshOnAgents( agents );

        assertFalse( result );
    }


    @Test
    public void shouldFailOnConfigSshCommand2() throws CommandException
    {
        Command errCommand = mock( Command.class );
        when( commandDispatcher
                .createContainerCommand( eq( new RequestBuilder( "echo 'Host *' > /root/.ssh/config && " +
                        "echo '    StrictHostKeyChecking no' >> /root/.ssh/config && " +
                        "chmod 644 /root/.ssh/config" ) ), anySet() ) ).thenReturn( errCommand );

        Mockito.doThrow( new CommandException( ERR_MSG ) ).when( errCommand ).execute();

        boolean result = networkManager.configSsh( containers );

        assertFalse( result );
    }
}
