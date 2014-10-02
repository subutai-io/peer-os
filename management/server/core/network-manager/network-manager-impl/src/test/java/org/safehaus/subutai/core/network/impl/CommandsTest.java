package org.safehaus.subutai.core.network.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;

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
    private CommandRunner commandRunner;
    private Commands commands;
    private Agent agent;


    @Before
    public void setUp()
    {
        commandRunner = mock( CommandRunner.class );
        commands = new Commands( commandRunner );
        agent = MockUtils.getAgent( UUID.randomUUID(), "hostname1", "127.0.0.1" );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNUllCommandRunner()
    {
        new Commands( null );
    }


    @Test
    public void testCreateSSHCommand()
    {

        commands.getCreateSSHCommand( Lists.newArrayList( agent ) );


        verify( commandRunner ).createCommand( any( RequestBuilder.class ), anySet() );
    }


    @Test
    public void testReadSSHCommand()
    {

        commands.getReadSSHCommand( Lists.newArrayList( agent ) );


        verify( commandRunner ).createCommand( any( RequestBuilder.class ), anySet() );
    }


    @Test
    public void testWriteSSHCommand()
    {

        commands.getWriteSSHCommand( Lists.newArrayList( agent ), "" );


        verify( commandRunner ).createCommand( any( RequestBuilder.class ), anySet() );
    }


    @Test
    public void testConfigSSHCommand()
    {

        commands.getConfigSSHCommand( Lists.newArrayList( agent ) );


        verify( commandRunner ).createCommand( any( RequestBuilder.class ), anySet() );
    }


    @Test
    public void testEtcHostsCommand()
    {

        commands.getAddIpHostToEtcHostsCommand( "intra.lan", Sets.newHashSet( agent ) );


        verify( commandRunner )
                .createCommand( eq( "Add ip-host pair to /etc/hosts" ), any( RequestBuilder.class ), anySet() );
    }
}
