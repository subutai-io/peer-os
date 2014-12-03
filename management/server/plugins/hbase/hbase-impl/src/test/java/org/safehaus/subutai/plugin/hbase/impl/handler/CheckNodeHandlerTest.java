package org.safehaus.subutai.plugin.hbase.impl.handler;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CheckNodeHandlerTest {
    CheckNodeHandler checkNodeHandler;
    HBaseImpl hBaseImpl;
    Tracker tracker;

    @Before
    public void setUp() throws Exception {
        tracker = mock(Tracker.class);
        hBaseImpl = mock(HBaseImpl.class);
        when(hBaseImpl.getTracker()).thenReturn(tracker);

        checkNodeHandler = new CheckNodeHandler(hBaseImpl,"test","test");

        assertEquals(tracker,hBaseImpl.getTracker());
    }

    @Test
    public void testRun() throws Exception {
        checkNodeHandler.run();
    }
}