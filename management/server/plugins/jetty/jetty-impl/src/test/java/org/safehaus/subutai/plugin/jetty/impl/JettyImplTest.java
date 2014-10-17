package org.safehaus.subutai.plugin.jetty.impl;


import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.common.mock.AgentManagerMock;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;
import org.safehaus.subutai.plugin.jetty.impl.handler.CheckClusterHandler;
import org.safehaus.subutai.plugin.jetty.impl.handler.CheckServiceHandler;
import org.safehaus.subutai.plugin.jetty.impl.handler.InstallOperationHandler;
import org.safehaus.subutai.plugin.jetty.impl.handler.InstallOperationHandlerTest;
import org.safehaus.subutai.plugin.jetty.impl.handler.StartServiceHandler;
import org.safehaus.subutai.plugin.jetty.impl.handler.StopClusterHandler;
import org.safehaus.subutai.plugin.jetty.impl.handler.StopServiceHandler;
import org.safehaus.subutai.plugin.jetty.impl.handler.UninstallOperationHandler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class JettyImplTest
{
    private DbManager dbManager = mock( DbManager.class );
    private CommandRunner commandRunner = mock( CommandRunner.class );
    private ExecutorService executor = mock( ExecutorService.class );
    private PluginDAO pluginDAO = mock( PluginDAO.class );
    private Tracker tracker = new TrackerMock();
    private AgentManager agentManager = new AgentManagerMock();
    private ContainerManager containerManager = mock( ContainerManager.class );

    private JettyImpl jettyManager;


    @Before
    public void setUp()
    {
        jettyManager = new JettyImpl();
        jettyManager.setExecutor( executor );
        jettyManager.setTracker( tracker );
        jettyManager.setAgentManager( agentManager );
        jettyManager.setPluginDAO( pluginDAO );
        jettyManager.setContainerManager( containerManager );
    }


    @Test
    public void installingClusterTest()
    {
        JettyConfig jettyConfig = new JettyConfig();
        jettyConfig.setClusterName( "testJettyCluster" );

        doAnswer( new Answer<Object>()
        {
            @Override
            public Object answer( final InvocationOnMock invocationOnMock ) throws Throwable
            {
                ( ( Runnable ) invocationOnMock.getArguments()[0] ).run();
                return null;
            }
        } ).when( executor ).execute( isA( Runnable.class ) );

        jettyManager.installCluster( jettyConfig );

        verify( executor ).execute( isA( InstallOperationHandler.class ) );
    }


    @Test
    public void uninstallClusterTest()
    {
        JettyConfig jettyConfig = new JettyConfig();
        jettyConfig.setClusterName( "testJettyCluster" );

        try
        {
            doAnswer( new Answer<Void>()
            {
                @Override
                public Void answer( final InvocationOnMock invocationOnMock ) throws Throwable
                {
                    return null;
                }
            } ).when( containerManager ).clonesDestroy( anySet() );
        }
        catch ( LxcDestroyException ex )
        {
        }

        doAnswer( new Answer<Object>()
        {
            @Override
            public Object answer( final InvocationOnMock invocationOnMock ) throws Throwable
            {
                ( ( Runnable ) invocationOnMock.getArguments()[0] ).run();
                return null;
            }
        } ).when( executor ).execute( isA( Runnable.class ) );

        when( pluginDAO.getInfo( JettyConfig.PRODUCT_KEY, jettyConfig.getClusterName(), JettyConfig.class ) )
                .thenReturn( jettyConfig );

        jettyManager.uninstallCluster( jettyConfig.getClusterName() );

        verify( executor ).execute( isA( UninstallOperationHandler.class ) );
    }


    @Test
    public void checkClusterTest()
    {
        JettyConfig jettyConfig = new JettyConfig();
        jettyConfig.setClusterName( "testClusterName" );

        jettyManager.checkCluster( jettyConfig.getClusterName() );
        verify( executor ).execute( isA( CheckClusterHandler.class ) );
    }


    @Test
    public void addNodeTest()
    {
        UUID id = jettyManager.addNode( "testClusterName", "testHostName" );
        assertThat( "id is null", id == null );
    }


    @Test
    public void stopClusterTest()
    {
        jettyManager.stopCluster( "testClusterName" );
        verify( executor ).execute( isA( StopClusterHandler.class ) );
    }


    @Test
    public void startServiceTest()
    {
        jettyManager.startService( "testClusterName", "testHostName" );
        verify( executor ).execute( isA( StartServiceHandler.class ) );
    }


    @Test
    public void stopServiceTest()
    {
        jettyManager.stopService( "testClusterName", "testHostName" );
        verify( executor ).execute( isA( StopServiceHandler.class ) );
    }


    @Test
    public void statusServiceTest()
    {
        jettyManager.statusService( "testClusterName", "testHostName" );
        verify( executor ).execute( isA( CheckServiceHandler.class ) );
    }
}
