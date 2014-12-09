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
public class StatusServiceCommandTest
{
    private StatusServiceCommand statusServiceCommand;
    @Mock
    Cassandra cassandra;
    @Mock
    Tracker tracker;

    @Before
    public void setUp() 
    {
        statusServiceCommand = new StatusServiceCommand();
    }

    @Test
    public void testGetCassandraManager() 
    {
        statusServiceCommand.setCassandraManager(cassandra);
        statusServiceCommand.getCassandraManager();

        // assertions
        assertNotNull(statusServiceCommand.getCassandraManager());
        assertEquals(cassandra, statusServiceCommand.getCassandraManager());

    }

    @Test
    public void testSetCassandraManager() 
    {
        statusServiceCommand.setCassandraManager(cassandra);
        statusServiceCommand.getCassandraManager();

        // assertions
        assertNotNull(statusServiceCommand.getCassandraManager());
        assertEquals(cassandra, statusServiceCommand.getCassandraManager());
        
    }

    @Test
    public void testGetTracker() 
    {
        statusServiceCommand.setTracker(tracker);
        statusServiceCommand.getTracker();

        // assertions
        assertNotNull(statusServiceCommand.getTracker());
        assertEquals(tracker, statusServiceCommand.getTracker());

    }

    @Test
    public void testSetTracker() 
    {
        statusServiceCommand.setTracker(tracker);
        statusServiceCommand.getTracker();

        // assertions
        assertNotNull(statusServiceCommand.getTracker());
        assertEquals(tracker, statusServiceCommand.getTracker());

    }
}