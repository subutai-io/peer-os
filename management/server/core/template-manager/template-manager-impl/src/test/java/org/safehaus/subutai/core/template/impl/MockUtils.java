package org.safehaus.subutai.core.template.impl;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.template.api.ActionType;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by timur on 10/6/14.
 */
public class MockUtils
{
    final static UUID physicalUUID = UUID.randomUUID();
    final static UUID lxcUUID = UUID.randomUUID();
    final static String MASTER_TEMPLATE_NAME = "master";
    public static final String PHYSICAL_HOSTNAME = "py_host";
    public static final String LXC_HOSTNAME = "lxc_host";


    public static Agent getPhysicalAgent()
    {
        Agent agent = mock( Agent.class );
        when( agent.getHostname() ).thenReturn( PHYSICAL_HOSTNAME );
        when( agent.getUuid() ).thenReturn( physicalUUID );
        when( agent.isLXC() ).thenReturn( false );
        when( agent.getParentHostName() ).thenReturn( null );
        when( agent.compareTo( agent ) ).thenCallRealMethod();
        return agent;
    }


    public static Agent getLxcAgent()
    {
        Agent agent = mock( Agent.class );
        when( agent.getHostname() ).thenReturn( LXC_HOSTNAME );
        when( agent.getUuid() ).thenReturn( lxcUUID );
        when( agent.isLXC() ).thenReturn( true );
        when( agent.getParentHostName() ).thenReturn( PHYSICAL_HOSTNAME );
        when( agent.compareTo( agent ) ).thenCallRealMethod();
        return agent;
    }


    public static Command getCommand( boolean completed, boolean succeeded, Integer exitCode, String stdOut,
                                      String stdErr )
    {
        Command command = mock( Command.class );
        when( command.hasCompleted() ).thenReturn( completed );
        when( command.hasSucceeded() ).thenReturn( succeeded );
        AgentResult agentResult = mock( AgentResult.class );
        when( agentResult.getExitCode() ).thenReturn( exitCode );
        when( agentResult.getStdOut() ).thenReturn( stdOut );
        when( agentResult.getStdErr() ).thenReturn( stdErr );
        when( agentResult.getAgentUUID() ).thenReturn( physicalUUID );
        Map<UUID, AgentResult> results = new HashMap<>();
        results.put( physicalUUID, agentResult );
        when( command.getResults() ).thenReturn( results );
        return command;
    }


    public static CommandRunner getHardCodedCommandRunner( boolean completed, boolean succeeded, Integer exitCode,
                                                           String stdOut, String stdErr )
    {
        CommandRunner commandRunner = mock( CommandRunner.class );
        Command command = MockUtils.getCommand( completed, succeeded, exitCode, stdOut, stdErr );
        when( commandRunner.createCommand( any( RequestBuilder.class ), any( Set.class ) ) ).thenReturn( command );

        return commandRunner;
    }


    private static Command getTemplateListCommand( boolean completed, boolean succeeded, Integer exitCode,
                                                   String stdOut, String stdErr )
    {

        return getCommand( true, succeeded, exitCode, stdOut, stdErr );
    }


    public static CommandRunner getHardCodedCloneCommandRunner( boolean completed, boolean succeeded, Integer exitCode,
                                                                String stdOut, String stdErr )
    {
        CommandRunner commandRunner = mock( CommandRunner.class );
        Command listCommand = MockUtils.getCommand( completed, succeeded, exitCode, stdOut, stdErr );
        RequestBuilder requestBuilder = new RequestBuilder( ActionType.LIST_TEMPLATES.buildCommand( "master" ) );
        //        requestBuilder.withTimeout( ( int ) TimeUnit.toSeconds( 1l) );
        when( commandRunner.createCommand( eq( requestBuilder ), anySet() ) ).thenReturn( listCommand );

        return commandRunner;
    }


    public static List<Template> getParentTemplates()
    {
        //        Template master = new Template( Template.ARCH_AMD64, "master", "/", "master", "", "", "", "" );
        Template tmock = mock( Template.class );
        //        when( tmock.getTemplateName() ).thenReturn( "master" );
        return Arrays.asList( tmock );
    }
}
