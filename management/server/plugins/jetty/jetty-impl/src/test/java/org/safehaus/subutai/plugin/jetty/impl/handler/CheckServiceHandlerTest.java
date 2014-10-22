package org.safehaus.subutai.plugin.jetty.impl.handler;


import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.mock.CommandMock;
import org.safehaus.subutai.plugin.common.mock.TrackerOperationMock;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;
import org.safehaus.subutai.plugin.jetty.impl.Commands;
import org.safehaus.subutai.plugin.jetty.impl.JettyImpl;

import com.google.common.collect.Sets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CheckServiceHandlerTest
{
    JettyImpl manager = new JettyImpl();
    CheckServiceHandler handler;
    private String clusterName = "testClusterName";
    private String hostName = "testHostName";
    private Agent testAgent;
    private JettyConfig config;


    @Before
    public void setUp()
    {
        testAgent =
                new Agent( UUID.randomUUID(), hostName, "", "", new ArrayList<String>(), true, "", UUID.randomUUID(),
                        UUID.randomUUID() );

        config = new JettyConfig();
        config.setClusterName( clusterName );
        config.setNodes( Sets.newHashSet( testAgent ) );

        manager.setTracker( mock( Tracker.class ) );
        manager.setCommandRunner( mock( CommandRunner.class ) );
        manager.setPluginDAO( mock( PluginDAO.class ) );
        manager.setAgentManager( mock( AgentManager.class ) );

        doReturn( new TrackerOperationMock() ).when( manager.getTracker() )
                                              .createTrackerOperation( anyString(), any( String.class ) );
        handler = new CheckServiceHandler( manager, clusterName, hostName );

        assertThat( "handler not null", handler != null );
    }


    @Test()
    public void testRun() throws InterruptedException
    {
        manager.setCommands( mock( Commands.class ) );

        when( manager.getPluginDAO().getInfo( JettyConfig.PRODUCT_KEY, config.getClusterName(), JettyConfig.class ) )
                .thenReturn( config );

        doReturn( testAgent ).when( manager.getAgentManager() ).getAgentByHostname( hostName );

        doReturn( new TrackerOperationMock() ).when( manager.getTracker() )
                                              .createTrackerOperation( anyString(), any( String.class ) );

        CommandMock checkCommand = new CommandMock();
        checkCommand.setSucceeded( false );

        when( manager.getCommands().getStatusCommand( anySet() ) ).thenReturn( checkCommand );

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute( handler );

        Thread.sleep( 3000 );
        verify( manager.getCommandRunner() ).runCommand( isA( CommandMock.class ) );
    }
}
