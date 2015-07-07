package io.subutai.pluginmanager.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.pluginmanager.api.OperationType;
import io.subutai.pluginmanager.api.PluginManagerException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class PluginManagerImplTest
{
    private PluginManagerImpl pluginManager;

    @Mock
    PeerManager peerManager;
    @Mock
    Tracker tracker;
    @Mock
    TrackerOperation trackerOperation;
    @Mock
    LocalPeer localPeer;
    @Mock
    ManagementHost managementHost;
    @Mock
    CommandResult commandResult;
    @Mock
    ManagerHelper managerHelper;


    @Before
    public void setUp() throws Exception
    {
        // mock constructor
        when( tracker.createTrackerOperation( anyString(), anyString() ) ).thenReturn( trackerOperation );
        when( trackerOperation.getId() ).thenReturn( UUID.randomUUID() );

        pluginManager = new PluginManagerImpl( peerManager, tracker );

        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
        when( managementHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( commandResult.getStdOut() ).thenReturn( "hadoop-subutai-plugin	2.0.4" );
    }


    @Test
    public void testDestroy() throws Exception
    {
        pluginManager.destroy();
    }


    @Test
    public void testGetTracker() throws Exception
    {
        assertNotNull( pluginManager.getTracker() );
    }


    @Test
    public void testGetCommands() throws Exception
    {
        assertNotNull( pluginManager.getCommands() );
    }


    @Test
    public void testInstallPlugin() throws Exception
    {
        assertNotNull( pluginManager.installPlugin( "testPluginName" ) );
    }


    @Test
    public void testRemovePlugin() throws Exception
    {
        assertNotNull( pluginManager.removePlugin( "testPluginName" ) );
    }


    @Test
    public void testUpgradePlugin() throws Exception
    {
        assertNotNull( pluginManager.upgradePlugin( "testPluginName" ) );
    }


    @Test
    public void testGetInstalledPlugins() throws Exception
    {
        assertNotNull( pluginManager.getInstalledPlugins() );
    }


    @Test
    public void testGetAvailablePlugins() throws Exception
    {
        assertNotNull( pluginManager.getAvailablePlugins() );
    }


    @Test
    public void testGetAvailablePluginNames() throws Exception
    {
        assertNotNull( pluginManager.getAvailablePluginNames() );
    }


    @Test
    public void testGetAvaileblePluginVersions() throws Exception
    {
        pluginManager.getAvaileblePluginVersions();
    }


    @Test
    public void testGetInstalledPluginVersions() throws Exception
    {
        assertNotNull( pluginManager.getInstalledPluginVersions() );
    }


    @Test
    public void testGetInstalledPluginNames() throws Exception
    {
        assertNotNull( pluginManager.getInstalledPluginNames() );
    }


    @Test
    public void testGetPluginVersion() throws Exception
    {
        pluginManager.getPluginVersion( "hadoop" );
    }


    @Test
    public void testIsUpgradeAvailable() throws Exception
    {
        pluginManager.isUpgradeAvailable( "hadoop" );
    }


    @Test
    public void testGetProductKey() throws Exception
    {
        pluginManager.getProductKey();
    }


    @Test
    public void testIsInstalled() throws Exception
    {
        assertNotNull( pluginManager.isInstalled( "hadoop" ) );
    }


    @Test
    public void testIsInstalledException() throws Exception
    {
        when( managerHelper.execute( any( RequestBuilder.class ) ) ).thenThrow( PluginManagerException.class );
        pluginManager.isInstalled( "hadoop" );
    }


    @Test
    public void testIsInstalledYes() throws Exception
    {
        when( commandResult.getStdOut() ).thenReturn( "install ok installed" );
        assertNotNull( pluginManager.isInstalled( "hadoop" ) );
    }


    @Test
    public void testOperationSuccessfulInstall() throws Exception
    {
        pluginManager.operationSuccessful( OperationType.INSTALL );
    }


    @Test
    public void testOperationSuccessfulRemove() throws Exception
    {
        pluginManager.operationSuccessful( OperationType.REMOVE );
    }
}