package org.safehaus.subutai.plugin.jetty.impl.handler;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.mock.AgentManagerMock;
import org.safehaus.subutai.plugin.common.mock.CommandMock;
import org.safehaus.subutai.plugin.common.mock.ProductOperationMock;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;
import org.safehaus.subutai.plugin.jetty.impl.Commands;
import org.safehaus.subutai.plugin.jetty.impl.JettyImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class InstallOperationHandlerTest
{
    private String clusterName = "testClusterName";
    JettyImpl manager = new JettyImpl();
    JettyConfig config = new JettyConfig();
    InstallOperationHandler handler;


    @Before
    public void setUp()
    {
        manager.setTracker( mock( Tracker.class ) );
        manager.setCommandRunner( mock( CommandRunner.class ) );
        manager.setPluginDAO( mock( PluginDAO.class ) );
        manager.setCommands( new Commands( manager.getCommandRunner() ) );
        manager.setAgentManager( new AgentManagerMock() );

        config.setClusterName( clusterName );

        doReturn( new ProductOperationMock() ).when( manager.getTracker() )
                                              .createProductOperation( anyString(), any( String.class ) );

        handler = new InstallOperationHandler( manager, config );

        assertThat( "handler not null", handler != null );
    }


    @Test
    public void testRun()
    {
        JettyConfig jettyConfig = new JettyConfig();
        jettyConfig.setClusterName( clusterName );

        CommandMock checkCommand = new CommandMock();
        checkCommand.setSucceeded( true );
        when( manager.getCommandRunner().createCommand( any( RequestBuilder.class ), anySet() ) )
                .thenReturn( checkCommand );

        when( manager.getPluginDAO().getInfo( JettyConfig.PRODUCT_KEY.toLowerCase(), jettyConfig.getClusterName(),
                JettyConfig.class ) ).thenReturn( jettyConfig );

        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute( handler );
    }
}
