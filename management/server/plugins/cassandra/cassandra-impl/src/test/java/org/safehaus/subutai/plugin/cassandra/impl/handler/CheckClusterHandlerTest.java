package org.safehaus.subutai.plugin.cassandra.impl.handler;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CheckClusterHandlerTest
{
    private CheckClusterHandler checkClusterHandler;
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

    @Before
    public void setup() throws CommandException
    {
        // mock run method
        when(cassandraImpl.getCluster("test")).thenReturn(cassandraClusterConfig);
        when(cassandraImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(environment.getContainerHosts()).thenReturn(mySet);
        when(mySet.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(containerHost);
        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);


        when(cassandraImpl.getTracker()).thenReturn(tracker);
        when(tracker.createTrackerOperation(anyString(),anyString())).thenReturn(trackerOperation);

        checkClusterHandler = new CheckClusterHandler(cassandraImpl,"test");
    }


    @Test
    public void testRun() throws CommandException
    {
        // mock run method
        when(commandResult.getStdOut()).thenReturn("running...");

        checkClusterHandler.run();

        // asserts
        assertNotNull(cassandraImpl.getCluster("test"));
        assertEquals(environment,environmentManager.getEnvironmentByUUID(any(UUID.class)));
        assertTrue(commandResult.hasSucceeded());
        assertTrue(commandResult.getStdOut().contains("running..."));

    }

    @Test
    public void testRunWhenClusterDoesNotExist()
    {
        when(cassandraImpl.getCluster("test")).thenReturn(null);

        checkClusterHandler.run();
    }

    @Test
    public void testRunWhenCommandResultNotSucceeded() throws CommandException
    {
        // mock run method
        when(commandResult.hasSucceeded()).thenReturn(false);

        checkClusterHandler.run();
    }

    @Test
    public void testRunWhenCommandResultDoesNotContainRunning() throws CommandException
    {
        // mock run method
        when(commandResult.getStdOut()).thenReturn("test");

        checkClusterHandler.run();

    }


}
