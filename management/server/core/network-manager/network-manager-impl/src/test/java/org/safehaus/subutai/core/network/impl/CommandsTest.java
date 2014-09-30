package org.safehaus.subutai.core.network.impl;


import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.impl.CommandImpl;
import org.safehaus.subutai.core.command.impl.CommandRunnerImpl;
import org.safehaus.subutai.core.communication.api.CommunicationManager;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


/**
 * Test for Commands
 */
public class CommandsTest
{
    @Test
    public void testEtcHostsCommand()
    {
        CommandRunner commandRunner =
                new CommandRunnerImpl( mock( CommunicationManager.class ), mock( AgentManager.class ) );
        Commands commands = new Commands( commandRunner );
        Agent agent1 = MockUtils.getAgent( UUID.randomUUID(), "hostname1", "127.0.0.1" );
        Agent agent2 = MockUtils.getAgent( UUID.randomUUID(), "hostname2", "127.0.0.2" );

        CommandImpl command = ( CommandImpl ) commands
                .getAddIpHostToEtcHostsCommand( "intra.lan", Sets.newHashSet( agent1, agent2 ) );


        assertEquals( 2, command.getRequestsCount() );
    }
}
