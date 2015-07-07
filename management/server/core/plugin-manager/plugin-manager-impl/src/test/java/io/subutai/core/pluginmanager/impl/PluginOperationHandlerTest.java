package io.subutai.core.pluginmanager.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.tracker.TrackerOperation;
import io.subutai.core.pluginmanager.impl.Commands;
import io.subutai.core.pluginmanager.impl.ManagerHelper;
import io.subutai.core.pluginmanager.impl.PluginManagerImpl;
import io.subutai.core.pluginmanager.impl.PluginOperationHandler;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.core.pluginmanager.api.OperationType;
import io.subutai.core.pluginmanager.api.PluginManagerException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class PluginOperationHandlerTest
{
    private PluginOperationHandler pluginOperationHandler;
    private PluginOperationHandler pluginOperationHandler2;
    private PluginOperationHandler pluginOperationHandler3;

    @Mock
    PluginManagerImpl pluginManager;
    @Mock
    ManagerHelper managerHelper;
    @Mock
    Tracker tracker;
    @Mock
    TrackerOperation trackerOperation;
    @Mock
    Commands commands;


    @Before
    public void setUp() throws Exception
    {
        // mock constructor
        when( pluginManager.getTracker() ).thenReturn( tracker );
        when( tracker.createTrackerOperation( anyString(), anyString() ) ).thenReturn( trackerOperation );
        when( trackerOperation.getId() ).thenReturn( UUID.randomUUID() );

        pluginOperationHandler =
                new PluginOperationHandler( pluginManager, managerHelper, "testPluginName", OperationType.INSTALL );
        pluginOperationHandler2 =
                new PluginOperationHandler( pluginManager, managerHelper, "testPluginName", OperationType.UPGRADE );
        pluginOperationHandler3 =
                new PluginOperationHandler( pluginManager, managerHelper, "testPluginName", OperationType.REMOVE );

        when( pluginManager.getCommands() ).thenReturn( commands );
    }


    @Test
    public void testGetTrackerId() throws Exception
    {
        assertNotNull( pluginOperationHandler.getTrackerId() );
    }


    @Test
    public void testRunOperationTypeInstallException() throws Exception
    {
        pluginOperationHandler.run();
    }


    @Test
    public void testRunOperationTypeInstallFailed() throws Exception
    {
        when( managerHelper.execute( any( RequestBuilder.class ) ) ).thenReturn( "test" );

        pluginOperationHandler.run();

        // asserts
        verify( trackerOperation ).addLogFailed( "Installation failed" );
        assertFalse( pluginManager.isInstalled( "testPluginName" ) );
    }


    @Test
    public void testRunOperationTypeInstall() throws Exception
    {
        when( managerHelper.execute( any( RequestBuilder.class ) ) ).thenReturn( "test" );
        when( pluginManager.isInstalled( anyString() ) ).thenReturn( true );

        pluginOperationHandler.run();

        // asserts
        verify( trackerOperation ).addLogDone( "Plugin is installed successfully." );
        assertTrue( pluginManager.isInstalled( "testPluginName" ) );
    }


    @Test
    public void testRunOperationTypeUpgrade() throws Exception
    {
        pluginOperationHandler2.run();

        // asserts
        verify( trackerOperation ).addLogDone( "Plugin is upgraded successfully." );
    }


    @Test
    public void testRunOperationTypeUpgradeException() throws Exception
    {
        when( managerHelper.execute( any(RequestBuilder.class) ) ).thenThrow( PluginManagerException.class );
        pluginOperationHandler2.run();
    }


    @Test
    public void testRemovePluginException() throws Exception
    {
        pluginOperationHandler3.removePlugin();
    }


    @Test
    public void testRemovePlugin() throws Exception
    {
        when( managerHelper.execute( any( RequestBuilder.class ) ) ).thenReturn( "test" );

        pluginOperationHandler3.run();

        //asserts
        verify( trackerOperation ).addLogDone( "Plugin is removed successfully." );
    }
}