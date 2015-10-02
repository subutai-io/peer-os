package io.subutai.core.pluginmanager.impl;


import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.pluginmanager.api.PluginInfo;
import io.subutai.core.pluginmanager.api.PluginManagerException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ManagerHelperTest
{
    private ManagerHelper managerHelper;
    private Set<PluginInfo> mySet;
    @Mock
    PeerManager peerManager;
    @Mock
    LocalPeer localPeer;
    @Mock
    ManagementHost managementHost;
    @Mock
    RequestBuilder requestBuilder;
    @Mock
    CommandResult commandResult;
    @Mock
    PluginInfo pluginInfo;


    @Before
    public void setUp() throws Exception
    {
        managerHelper = new ManagerHelper( peerManager );

        mySet = new HashSet<>();
        mySet.add( pluginInfo );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getManagementHost() ).thenReturn( managementHost );
    }


    @Test
    public void testGetManagementHost() throws Exception
    {
        assertNotNull( managerHelper.getManagementHost() );
    }


    @Test( expected = PluginManagerException.class )
    public void testGetManagementHostException() throws PluginManagerException, HostNotFoundException
    {
        when( peerManager.getLocalPeer() ).thenThrow( HostNotFoundException.class );

        managerHelper.getManagementHost();
    }


    @Test
    public void testExecuteHasSucceeded() throws Exception
    {
        when( managementHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( commandResult.hasSucceeded() ).thenReturn( true );

        managerHelper.execute( requestBuilder );
        assertTrue( commandResult.hasSucceeded() );
    }


    @Test (expected = PluginManagerException.class)
    public void testExecuteHasNotSucceeded() throws Exception
    {
        when( managementHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( commandResult.hasSucceeded() ).thenReturn( false );

        managerHelper.execute( requestBuilder );
        assertFalse( commandResult.hasSucceeded() );
    }


    @Test
    public void testParsePluginNames() throws Exception
    {
        managerHelper.parsePluginNames( "test" );
    }


    @Test
    public void testParseAvailablePluginsNames() throws Exception
    {
        managerHelper.parseAvailablePluginsNames( "test" );
    }


    @Test
    public void testParsePluginNamesAndVersions() throws Exception
    {
        managerHelper.parsePluginNamesAndVersions( "test" );
    }


    @Test
    public void testParseJson() throws Exception
    {

    }


    @Test
    public void testFindRating() throws Exception
    {
        assertNull( managerHelper.findRating( mySet, "testPluginName" ) );
    }


    @Test
    public void testFindRating2() throws Exception
    {
        when( pluginInfo.getPluginName() ).thenReturn( "testPluginName" );

        managerHelper.findRating( mySet, "testPluginName" );
        verify( pluginInfo ).getRating();
    }


    @Test
    public void testFindVersion() throws Exception
    {
        assertNull( managerHelper.findVersion( mySet, "testPluginName" ) );
    }


    @Test
    public void testFindVersion2() throws Exception
    {
        when( pluginInfo.getPluginName() ).thenReturn( "testPluginName" );

        managerHelper.findVersion( mySet, "testPluginName" );
        verify( pluginInfo ).getVersion();
    }


    @Test
    public void testGetDifferenceBetweenPlugins() throws Exception
    {
        assertNotNull( managerHelper.getDifferenceBetweenPlugins( mySet, mySet ) );
    }
}