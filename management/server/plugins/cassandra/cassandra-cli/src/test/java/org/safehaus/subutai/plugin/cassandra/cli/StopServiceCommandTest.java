package org.safehaus.subutai.plugin.cassandra.cli;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class StopServiceCommandTest
{
    private StopServiceCommand stopServiceCommand;
    @Mock
    Cassandra cassandra;
    @Mock
    Tracker tracker;

    @Before
    public void setUp() 
    {
        stopServiceCommand = new StopServiceCommand();
    }

    @Test
    public void testGetCassandraManager() 
    {
        stopServiceCommand.setCassandraManager(cassandra);
        stopServiceCommand.getCassandraManager();

        // assertions
        assertNotNull(stopServiceCommand.getCassandraManager());
        assertEquals(cassandra, stopServiceCommand.getCassandraManager());

    }

    @Test
    public void testSetCassandraManager() 
    {
        stopServiceCommand.setCassandraManager(cassandra);
        stopServiceCommand.getCassandraManager();

        // assertions
        assertNotNull(stopServiceCommand.getCassandraManager());
        assertEquals(cassandra, stopServiceCommand.getCassandraManager());

    }

    @Test
    public void testGetTracker() 
    {
        stopServiceCommand.setTracker(tracker);
        stopServiceCommand.getTracker();

        // assertions
        assertNotNull(stopServiceCommand.getTracker());
        assertEquals(tracker, stopServiceCommand.getTracker());

    }

    @Test
    public void testSetTracker() 
    {
        stopServiceCommand.setTracker(tracker);
        stopServiceCommand.getTracker();

        // assertions
        assertNotNull(stopServiceCommand.getTracker());
        assertEquals(tracker, stopServiceCommand.getTracker());

    }
}