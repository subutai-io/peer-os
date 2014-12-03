package org.safehaus.subutai.plugin.hbase.cli;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hbase.api.HBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class InstallHBaseClusterCommandTest
{
    private InstallHBaseClusterCommand installHBaseClusterCommand;
    private Tracker tracker;
    private HBase hBase;

    @Before
    public void setUp() throws Exception
    {
        tracker = mock(Tracker.class);
        hBase = mock(HBase.class);

        installHBaseClusterCommand = new InstallHBaseClusterCommand();
    }

    @Test
    public void testGetTracker() throws Exception
    {
        installHBaseClusterCommand.setTracker(tracker);
        installHBaseClusterCommand.getTracker();

        // assertions
        assertNotNull(installHBaseClusterCommand.getTracker());
        assertEquals(tracker, installHBaseClusterCommand.getTracker());
    }

    @Test
    public void testSetTracker() throws Exception
    {
        installHBaseClusterCommand.setTracker(tracker);
        installHBaseClusterCommand.getTracker();

        // assertions
        assertNotNull(installHBaseClusterCommand.getTracker());
        assertEquals(tracker, installHBaseClusterCommand.getTracker());
    }

    @Test
    public void testGetHbaseManager() throws Exception
    {
        installHBaseClusterCommand.setHbaseManager(hBase);
        installHBaseClusterCommand.getHbaseManager();

        // assertions
        assertNotNull(installHBaseClusterCommand.getHbaseManager());
        assertEquals(hBase,installHBaseClusterCommand.getHbaseManager());

    }

    @Test
    public void testSetHbaseManager() throws Exception
    {
        installHBaseClusterCommand.setHbaseManager(hBase);
        installHBaseClusterCommand.getHbaseManager();

        // assertions
        assertNotNull(installHBaseClusterCommand.getHbaseManager());
        assertEquals(hBase,installHBaseClusterCommand.getHbaseManager());

    }

    @Test
    public void testDoExecute() throws Exception
    {
        installHBaseClusterCommand.doExecute();
    }
}