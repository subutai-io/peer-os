/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.monitor.api.MetricType;
import org.safehaus.subutai.core.strategy.api.ContainerPlacementStrategy;
import org.safehaus.subutai.core.strategy.api.ServerMetric;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Mocking utilities
 */
public class MockUtils
{

    private static final UUID physicalUUID_1 = UUID.randomUUID();
    private static final UUID physicalUUID_2 = UUID.randomUUID();
    private static final UUID physicalUUID_3 = UUID.randomUUID();
    private static final UUID lxcUUID_1 = UUID.randomUUID();
    private static final UUID lxcUUID_2 = UUID.randomUUID();
    private static final UUID lxcUUID_3 = UUID.randomUUID();
    private static final String PHYSICAL_HOSTNAME_1 = "py111";
    private static final String PHYSICAL_HOSTNAME_2 = "py222";
    private static final String PHYSICAL_HOSTNAME_3 = "py333";
    private static final String LXC_HOSTNAME_1 = "py111-lxc-111";
    private static final String LXC_HOSTNAME_2 = "py111-lxc-222";
    private static final String LXC_HOSTNAME_3 = "py111-lxc-333";


    public static Agent getPhysicalAgent()
    {
        Agent agent = mock( Agent.class );
        when( agent.getHostname() ).thenReturn( PHYSICAL_HOSTNAME_1 );
        when( agent.getUuid() ).thenReturn( physicalUUID_1 );
        when( agent.isLXC() ).thenReturn( false );
        when( agent.getParentHostName() ).thenReturn( null );
        when( agent.compareTo( agent ) ).thenCallRealMethod();
        return agent;
    }


    public static ContainerPlacementStrategy getDefaultContainerPlacementStrategy()
    {
        return new DefaultContainerPlacementStrategy();
    }


    public static ContainerPlacementStrategy getRoundRobinPlacementStrategy()
    {
        return new RoundRobinStrategy();
    }


    public static ContainerPlacementStrategy getBestServerPlacementStrategy()
    {
        return new BestServerStrategy();
    }


    public static List<ServerMetric> getServerMetrics()
    {
        List<ServerMetric> result = new ArrayList<>();

        Map<MetricType, Double> avgMetric = new HashMap<>();
        avgMetric.put( MetricType.CPU_USER, 1.0 );
        avgMetric.put( MetricType.MEM_FREE, 4096.0 );
        Set<Agent> physicalAgents = getPhysicalAgents();
        int i = 0;
        for ( Agent agent : physicalAgents )
        {
            ServerMetric metric =
                    new ServerMetric( agent.getHostname(), 20000 + ( i++ ), 40000 + ( i++ ), 30 + ( i++ ), 4 + ( i++ ),
                            avgMetric );
            result.add( metric );
        }
        return result;
    }


    public Map<Agent, ServerMetric> getAvgMetrics()
    {
        Map<Agent, ServerMetric> result = new HashMap<>();
        return result;
    }


    public static Set<Agent> getPhysicalAgents()
    {
        Agent agent1 = mock( Agent.class );
        when( agent1.getHostname() ).thenReturn( PHYSICAL_HOSTNAME_1 );
        when( agent1.getUuid() ).thenReturn( physicalUUID_1 );
        when( agent1.isLXC() ).thenReturn( true );
        when( agent1.getParentHostName() ).thenReturn( null );
        when( agent1.compareTo( agent1 ) ).thenCallRealMethod();

        Agent agent2 = mock( Agent.class );
        when( agent2.getHostname() ).thenReturn( PHYSICAL_HOSTNAME_2 );
        when( agent2.getUuid() ).thenReturn( physicalUUID_2 );
        when( agent2.isLXC() ).thenReturn( true );
        when( agent2.getParentHostName() ).thenReturn( null );
        when( agent2.compareTo( agent2 ) ).thenCallRealMethod();

        Agent agent3 = mock( Agent.class );
        when( agent3.getHostname() ).thenReturn( PHYSICAL_HOSTNAME_3 );
        when( agent3.getUuid() ).thenReturn( physicalUUID_3 );
        when( agent3.isLXC() ).thenReturn( true );
        when( agent3.getParentHostName() ).thenReturn( null );
        when( agent3.compareTo( agent3 ) ).thenCallRealMethod();

        Set<Agent> agents = new HashSet<>();
        agents.add( agent1 );
        agents.add( agent2 );
        agents.add( agent3 );
        return agents;
    }

    //
    //    public static Agent getPhysicalAgent()
    //    {
    //        Agent agent = mock( Agent.class );
    //        when( agent.getHostname() ).thenReturn( PHYSICAL_HOSTNAME_1 );
    //        when( agent.getUuid() ).thenReturn( physicalUUID_1 );
    //        when( agent.isLXC() ).thenReturn( false );
    //        when( agent.getParentHostName() ).thenReturn( null );
    //        when( agent.compareTo( agent ) ).thenCallRealMethod();
    //        return agent;
    //    }
    //
    //
    //    public static Agent getLxcAgent()
    //    {
    //        Agent agent = mock( Agent.class );
    //        when( agent.getHostname() ).thenReturn( LXC_HOSTNAME_1 );
    //        when( agent.getUuid() ).thenReturn( lxcUUID_1 );
    //        when( agent.isLXC() ).thenReturn( true );
    //        when( agent.getParentHostName() ).thenReturn( PHYSICAL_HOSTNAME_1 );
    //        when( agent.compareTo( agent ) ).thenCallRealMethod();
    //        return agent;
    //    }
    //
    //
    //    public static CommandRunner getAutoCommandRunner()
    //    {
    //        CommandRunner commandRunner = mock( CommandRunner.class );
    //        when( commandRunner.createCommand( any( RequestBuilder.class ), any( Set.class ) ) )
    //                .thenAnswer( new Answer<Command>()
    //                {
    //
    //                    public Command answer( InvocationOnMock invocation ) throws Throwable
    //                    {
    //                        Object[] arguments = invocation.getArguments();
    //                        RequestBuilder requestBuilder = ( RequestBuilder ) arguments[0];
    //                        Request request = requestBuilder.build( physicalUUID_1, UUID.randomUUID() );
    //                        if ( request.getProgram().contains( "lxc-info" ) )
    //                        {
    //                            return MockUtils.getCommand( true, true, 0, "RUNNING", null );
    //                        }
    //                        else if ( request.getProgram().contains( "lxc-ls" ) )
    //                        {
    //                            return MockUtils.getCommand( true, true, 0, getLxcListOutput(), null );
    //                        }
    //                        else if ( request.getProgram().contains( "free -m" ) )
    //                        {
    //                            return MockUtils.getCommand( true, true, 0, getMetricsOutput(), null );
    //                        }
    //                        return MockUtils.getCommand( true, true, 0, null, null );
    //                    }
    //                } );
    //
    //        return commandRunner;
    //    }
    //
    //
    //    public static Command getCommand( boolean completed, boolean succeeded, Integer exitCode, String stdOut,
    //                                      String stdErr )
    //    {
    //        Command command = mock( Command.class );
    //        when( command.hasCompleted() ).thenReturn( completed );
    //        when( command.hasSucceeded() ).thenReturn( succeeded );
    //        AgentResult agentResult = mock( AgentResult.class );
    //        when( agentResult.getExitCode() ).thenReturn( exitCode );
    //        when( agentResult.getStdOut() ).thenReturn( stdOut );
    //        when( agentResult.getStdErr() ).thenReturn( stdErr );
    //        when( agentResult.getAgentUUID() ).thenReturn( physicalUUID_1 );
    //        Map<UUID, AgentResult> results = new HashMap<>();
    //        results.put( physicalUUID_1, agentResult );
    //        when( command.getResults() ).thenReturn( results );
    //        return command;
    //    }
    //
    //
    //    public static String getLxcListOutput()
    //    {
    //        return "RUNNING\n" + "  py111-lxc-222\n" + "  py111-lxc-888ba0c7-c559-11e3-acc9-af19ef907de1\n"
    //                + "  py111-lxc-888bc7d8-c559-11e3-acc9-af19ef907de1\n"
    //                + "  py111-lxc-888bc7da-c559-11e3-acc9-af19ef907de1\n"
    //                + "  py111-lxc-99780f42-c573-11e3-8493-59facd645e07\n"
    //                + "  py111-lxc-99783654-c573-11e3-8493-59facd645e07\n"
    //                + "  py111-lxc-99783656-c573-11e3-8493-59facd645e07\n"
    //                + "  py111-lxc-99783658-c573-11e3-8493-59facd645e07\n"
    //                + "  py111-lxc-acda8860-c55d-11e3-acc9-af19ef907de1\n"
    //                + "  py111-lxc-acda8861-c55d-11e3-acc9-af19ef907de1\n"
    //                + "  py111-lxc-acda8863-c55d-11e3-acc9-af19ef907de1\n"
    //                + "  py111-lxc-acda8865-c55d-11e3-acc9-af19ef907de1\n"
    //                + "  py111-lxc-acdaaf77-c55d-11e3-acc9-af19ef907de1\n" + "\n" + "FROZEN\n" + "\n" + "STOPPED\n"
    //                + "  base-container\n" + "  py111-lxc-test1\n" + "  py111-lxc-test2";
    //    }
    //
    //
    //    public static String getMetricsOutput()
    //    {
    //        return "-/+ buffers/cache:       8561       7362\n" + "/dev/sda1      950982348 15033292 887635220   2%
    // /\n"
    //                + " 15:05:11 up 7 days, 21:28,  1 user,  load average: 0.01, 0.09, 0.27\n" + "8";
    //    }
    //
    //
    //    public static CommandRunner getHardCodedCommandRunner( boolean completed, boolean succeeded, Integer exitCode,
    //                                                           String stdOut, String stdErr )
    //    {
    //        CommandRunner commandRunner = mock( CommandRunner.class );
    //        Command command = MockUtils.getCommand( completed, succeeded, exitCode, stdOut, stdErr );
    //        when( commandRunner.createCommand( any( RequestBuilder.class ), any( Set.class ) ) ).thenReturn(
    // command );
    //
    //        return commandRunner;
    //    }
}
