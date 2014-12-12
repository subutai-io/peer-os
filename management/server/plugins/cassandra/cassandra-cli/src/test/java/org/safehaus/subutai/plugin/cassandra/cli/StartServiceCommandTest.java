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
public class StartServiceCommandTest
{
    private StartServiceCommand startServiceCommand;
    @Mock
    Cassandra cassandra;
    @Mock
    Tracker tracker;

    @Before
    public void setUp() 
    {
        startServiceCommand = new StartServiceCommand();
    }

    @Test
    public void testGetCassandraManager() 
    {
        startServiceCommand.setCassandraManager(cassandra);
        startServiceCommand.getCassandraManager();

        // assertions
        assertNotNull(startServiceCommand.getCassandraManager());
        assertEquals(cassandra, startServiceCommand.getCassandraManager());

    }

    @Test
    public void testSetCassandraManager() 
    {
        startServiceCommand.setCassandraManager(cassandra);
        startServiceCommand.getCassandraManager();

        // assertions
        assertNotNull(startServiceCommand.getCassandraManager());
        assertEquals(cassandra, startServiceCommand.getCassandraManager());

    }

    @Test
    public void testGetTracker() 
    {
        startServiceCommand.setTracker(tracker);
        startServiceCommand.getTracker();

        // assertions
        assertNotNull(startServiceCommand.getTracker());
        assertEquals(tracker, startServiceCommand.getTracker());

    }

    @Test
    public void testSetTracker() 
    {
        startServiceCommand.setTracker(tracker);
        startServiceCommand.getTracker();

        // assertions
        assertNotNull(startServiceCommand.getTracker());
        assertEquals(tracker, startServiceCommand.getTracker());
    }
}