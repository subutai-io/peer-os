package org.safehaus.subutai.core.network.impl;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for Commands
 */
public class CommandsTest
{
    private CommandDispatcher commandDispatcher;
    private Commands commands;
    private List<Agent> agents;
    private Set<Container> containers;
    private static final UUID AGENT_ID = UUID.randomUUID();
    private static final String HOSTNAME = "hostname";
    private static final String DOMAIN_NAME = "intra.lan";
    private static final String IP = "127.0.0.1";


    @Before
    public void setUp()
    {
        commandDispatcher = mock( CommandDispatcher.class );
        commands = new Commands( commandDispatcher );
        agents = Lists.newArrayList( MockUtils.getAgent( AGENT_ID, HOSTNAME, IP ) );
        containers = Sets.newHashSet( MockUtils.getContainer( AGENT_ID, HOSTNAME, IP ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNUllCommandRunner()
    {
        new Commands( null );
    }


    @Test
    public void testCreateSSHCommand()
    {

        commands.getCreateSSHCommand( agents );


        verify( commandDispatcher ).createCommand( any( RequestBuilder.class ), anySet() );
    }


    @Test
    public void testCreateSSHCommand2()
    {

        commands.getCreateSSHCommand( containers );


        verify( commandDispatcher ).createContainerCommand( any( RequestBuilder.class ), anySet() );
    }


    @Test
    public void testReadSSHCommand()
    {

        commands.getReadSSHCommand( agents );


        verify( commandDispatcher ).createCommand( any( RequestBuilder.class ), anySet() );
    }


    @Test
    public void testReadSSHCommand2()
    {

        commands.getReadSSHCommand( containers );


        verify( commandDispatcher ).createContainerCommand( any( RequestBuilder.class ), anySet() );
    }


    @Test
    public void testWriteSSHCommand()
    {

        commands.getWriteSSHCommand( agents, "" );


        verify( commandDispatcher ).createCommand( any( RequestBuilder.class ), anySet() );
    }


    @Test
    public void testWriteSSHCommand2()
    {

        commands.getWriteSSHCommand( containers, "" );


        verify( commandDispatcher ).createContainerCommand( any( RequestBuilder.class ), anySet() );
    }


    @Test
    public void testConfigSSHCommand()
    {

        commands.getConfigSSHCommand( agents );


        verify( commandDispatcher ).createCommand( any( RequestBuilder.class ), anySet() );
    }


    @Test
    public void testConfigSSHCommand2()
    {

        commands.getConfigSSHCommand( containers );


        verify( commandDispatcher ).createContainerCommand( any( RequestBuilder.class ), anySet() );
    }


    @Test
    public void testEtcHostsCommand()
    {

        commands.getAddIpHostToEtcHostsCommand( DOMAIN_NAME, Sets.newHashSet( agents ) );


        verify( commandDispatcher )
                .createCommand( eq( "Add ip-host pair to /etc/hosts" ), any( RequestBuilder.class ), anySet() );
    }


    @Test
    public void testEtcHostsCommand2()
    {

        commands.getEtcHostsCommand( DOMAIN_NAME, containers );


        verify( commandDispatcher ).createContainerCommand( any( RequestBuilder.class ), anySet() );
    }
}
