package org.safehaus.subutai.core.apt.impl;


import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by dilshat on 9/27/14.
 */
public class MockUtils
{

    public static CommandRunner getCommandRunner( Command command )
    {
        CommandRunner commandRunner = mock( CommandRunner.class );
        when( commandRunner.createCommand( any( RequestBuilder.class ), anySet() ) ).thenReturn( command );
        when( commandRunner.createBroadcastCommand( any( RequestBuilder.class ) ) ).thenReturn( command );

        return commandRunner;
    }


    public static Command getCommand( boolean completed, boolean succeeded, UUID agentId, String stdOut, String stdErr,
                                      Integer exitCode )
    {
        Command command = mock( Command.class );

        when( command.hasCompleted() ).thenReturn( completed );
        when( command.hasSucceeded() ).thenReturn( succeeded );

        AgentResult agentResult = mock( AgentResult.class );

        when( agentResult.getStdOut() ).thenReturn( stdOut );
        when( agentResult.getStdErr() ).thenReturn( stdErr );
        when( agentResult.getExitCode() ).thenReturn( exitCode );
        when( agentResult.getAgentUUID() ).thenReturn( agentId );

        Map<UUID, AgentResult> results = mock( Map.class );
        when( command.getResults() ).thenReturn( results );
        when( results.get( agentId ) ).thenReturn( agentResult );

        return command;
    }


    public static Agent getAgent( UUID agentId )
    {
        Agent agent = mock( Agent.class );
        when( agent.getUuid() ).thenReturn( agentId );
        return agent;
    }
}
