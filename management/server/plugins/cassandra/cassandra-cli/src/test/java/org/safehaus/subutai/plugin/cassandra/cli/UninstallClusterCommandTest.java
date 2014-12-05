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
public class UninstallClusterCommandTest
{
    private UninstallClusterCommand uninstallClusterCommand;
    @Mock
    Cassandra cassandra;
    @Mock
    Tracker tracker;

    @Before
    public void setUp() 
    {
        uninstallClusterCommand = new UninstallClusterCommand();
    }

    @Test
    public void testGetTracker() 
    {
        uninstallClusterCommand.setTracker(tracker);
        uninstallClusterCommand.getTracker();

        // assertions
        assertNotNull(uninstallClusterCommand.getTracker());
        assertEquals(tracker, uninstallClusterCommand.getTracker());

    }

    @Test
    public void testSetTracker() 
    {
        uninstallClusterCommand.setTracker(tracker);
        uninstallClusterCommand.getTracker();

        // assertions
        assertNotNull(uninstallClusterCommand.getTracker());
        assertEquals(tracker, uninstallClusterCommand.getTracker());

    }

    @Test
    public void testGetCassandraManager() 
    {
        uninstallClusterCommand.setCassandraManager(cassandra);
        uninstallClusterCommand.getCassandraManager();

        // assertions
        assertNotNull(uninstallClusterCommand.getCassandraManager());
        assertEquals(cassandra, uninstallClusterCommand.getCassandraManager());

    }

    @Test
    public void testSetCassandraManager() 
    {
        uninstallClusterCommand.setCassandraManager(cassandra);
        uninstallClusterCommand.getCassandraManager();

        // assertions
        assertNotNull(uninstallClusterCommand.getCassandraManager());
        assertEquals(cassandra, uninstallClusterCommand.getCassandraManager());

    }
}