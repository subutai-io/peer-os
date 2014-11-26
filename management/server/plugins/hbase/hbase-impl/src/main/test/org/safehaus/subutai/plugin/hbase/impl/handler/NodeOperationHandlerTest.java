package org.safehaus.subutai.plugin.hbase.impl.handler;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.OperationType;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodeOperationHandlerTest {
    NodeOperationHandler nodeOperationHandler;
    HBaseImpl hBaseImpl;
    HBaseConfig hBaseConfig;
    UUID uuid;
    Tracker tracker;
    TrackerOperation trackerOperation;
    @Before
    public void setUp() throws Exception {
        hBaseImpl = mock(HBaseImpl.class);
        hBaseConfig = mock(HBaseConfig.class);
        uuid = new UUID(50,50);
        tracker = mock(Tracker.class);
        trackerOperation = mock(TrackerOperation.class);
    }

    @Test
    public void testConstructor() {
        nodeOperationHandler = new NodeOperationHandler(hBaseImpl,hBaseConfig);
    }

    @Test
    public void testConstructor2 () {
        nodeOperationHandler = new NodeOperationHandler(hBaseImpl,hBaseConfig,uuid, OperationType.INSTALL);
    }

    @Test
    public void testConstructor3 () {
        when(hBaseImpl.getTracker()).thenReturn(tracker);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        nodeOperationHandler = new NodeOperationHandler(hBaseImpl,hBaseConfig,"test", OperationType.INSTALL);

        assertEquals(tracker, hBaseImpl.getTracker());
        nodeOperationHandler.run();
    }



    @Test
    public void testRun() throws Exception {

    }

    @Test
    public void testExecuteCommand() throws Exception {

    }

    @Test
    public void testExecuteCommand1() throws Exception {

    }
}