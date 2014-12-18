package org.safehaus.subutai.plugin.cassandra.cli;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

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
        stopServiceCommand.setCassandraManager(cassandra);
        stopServiceCommand.setTracker(tracker);
    }

    @Test
    public void testGetCassandraManager() 
    {
        stopServiceCommand.getCassandraManager();

        // assertions
        assertNotNull(stopServiceCommand.getCassandraManager());
        assertEquals(cassandra, stopServiceCommand.getCassandraManager());

    }

    @Test
    public void testGetTracker() 
    {
        stopServiceCommand.getTracker();

        // assertions
        assertNotNull(stopServiceCommand.getTracker());
        assertEquals(tracker, stopServiceCommand.getTracker());

    }

    @Test
    public void testDoExecute() throws IOException
    {
        when(cassandra.stopService(null, null)).thenReturn(UUID.randomUUID());

        stopServiceCommand.doExecute();
    }
}