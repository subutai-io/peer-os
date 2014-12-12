package org.safehaus.subutai.plugin.cassandra.impl.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstallClusterHandlerTest
{
    private InstallClusterHandler installClusterHandler;
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
    ClusterSetupStrategy clusterSetupStrategy;

    @Before
    public void setUp() 
    {
        uuid = new UUID(50,50);
        when(cassandraImpl.getTracker()).thenReturn(tracker);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);
        when(cassandraClusterConfig.getClusterName()).thenReturn("test");

        installClusterHandler = new InstallClusterHandler(cassandraImpl,cassandraClusterConfig);
    }

    @Test
    public void testGetTrackerId() 
    {
        when(trackerOperation.getId()).thenReturn(uuid);

        installClusterHandler.getTrackerId();

        // asserts
        assertNotNull(installClusterHandler.getTrackerId());
        assertEquals(uuid,installClusterHandler.getTrackerId());

    }

    @Test
    public void testRun() throws EnvironmentBuildException
    {
        // mock run method
        when(cassandraImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.buildEnvironment(any(EnvironmentBlueprint.class))).thenReturn(environment);
        when(cassandraImpl.getClusterSetupStrategy(environment,cassandraClusterConfig,trackerOperation)).thenReturn(clusterSetupStrategy);

        installClusterHandler.run();

        // asserts
        verify(trackerOperation).addLog("Building environment...");
        assertEquals(environment,environmentManager.buildEnvironment(any(EnvironmentBlueprint.class)));
        assertEquals(clusterSetupStrategy, cassandraImpl.getClusterSetupStrategy(environment, cassandraClusterConfig,
                trackerOperation));

    }
}