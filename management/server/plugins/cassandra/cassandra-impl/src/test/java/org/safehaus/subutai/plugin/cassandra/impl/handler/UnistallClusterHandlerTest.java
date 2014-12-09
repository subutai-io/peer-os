package org.safehaus.subutai.plugin.cassandra.impl.handler;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.plugin.cassandra.impl.dao.PluginDAO;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UnistallClusterHandlerTest
{
    private UninstallClusterHandler uninstallClusterHandler;
    private UUID uuid;
    @Mock
    CassandraImpl cassandraImpl;
    @Mock
    Tracker tracker;
    @Mock
    TrackerOperation trackerOperation;
    @Mock
    CassandraClusterConfig cassandraClusterConfig;
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    Environment environment;
    @Mock
    ContainerHost containerHost;
    @Mock
    Iterator<ContainerHost> iterator;
    @Mock
    Set<ContainerHost> mySet;
    @Mock
    CommandResult commandResult;
    @Mock
    PluginDAO pluginDAO;

    @Before
    public void setup()
    {
        uuid = new UUID(50,50);
        when(cassandraImpl.getTracker()).thenReturn(tracker);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        when(cassandraClusterConfig.getClusterName()).thenReturn("test");

        uninstallClusterHandler = new UninstallClusterHandler(cassandraImpl,"test");
    }


    @Test
    public void testRun() throws EnvironmentDestroyException
    {
        // mock run method
        when(cassandraImpl.getCluster(anyString())).thenReturn(cassandraClusterConfig);
        when(cassandraImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.destroyEnvironment(any(UUID.class))).thenReturn(true);
        when(cassandraImpl.getPluginDAO()).thenReturn(pluginDAO);
        when(pluginDAO.deleteInfo(anyString(),anyString())).thenReturn(true);

        uninstallClusterHandler.run();

        // asserts
        verify(trackerOperation).addLog("Destroying environment...");
        assertTrue(environmentManager.destroyEnvironment(any(UUID.class)));
        assertTrue(pluginDAO.deleteInfo(anyString(),anyString()));
        verify(trackerOperation).addLogDone("Cluster destroyed");

    }

    @Test
    public void testRunWhenCassandraClusterConfigIsNull() {
        when(cassandraImpl.getCluster(anyString())).thenReturn(null);

        uninstallClusterHandler.run();
    }
}
