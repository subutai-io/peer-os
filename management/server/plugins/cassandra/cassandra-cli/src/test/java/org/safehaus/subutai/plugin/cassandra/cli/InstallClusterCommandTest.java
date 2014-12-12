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
public class InstallClusterCommandTest
{
    private InstallClusterCommand installClusterCommand;
    @Mock
    Cassandra cassandra;
    @Mock
    Tracker tracker;

    @Before
    public void setUp() 
    {
        installClusterCommand = new InstallClusterCommand();
    }

    @Test
    public void testGetCassandraManager() 
    {
        installClusterCommand.setCassandraManager(cassandra);
        installClusterCommand.getCassandraManager();

        // assertions
        assertNotNull(installClusterCommand.getCassandraManager());
        assertEquals(cassandra,installClusterCommand.getCassandraManager());

    }

    @Test
    public void testSetCassandraManager() 
    {
        installClusterCommand.setCassandraManager(cassandra);
        installClusterCommand.getCassandraManager();

        // assertions
        assertNotNull(installClusterCommand.getCassandraManager());
        assertEquals(cassandra,installClusterCommand.getCassandraManager());

    }

    @Test
    public void testGetTracker() 
    {
        installClusterCommand.setTracker(tracker);
        installClusterCommand.getTracker();

        // assertions
        assertNotNull(installClusterCommand.getTracker());
        assertEquals(tracker,installClusterCommand.getTracker());

    }

    @Test
    public void testSetTracker() 
    {
        installClusterCommand.setTracker(tracker);
        installClusterCommand.getTracker();

        // assertions
        assertNotNull(installClusterCommand.getTracker());
        assertEquals(tracker,installClusterCommand.getTracker());

    }
}