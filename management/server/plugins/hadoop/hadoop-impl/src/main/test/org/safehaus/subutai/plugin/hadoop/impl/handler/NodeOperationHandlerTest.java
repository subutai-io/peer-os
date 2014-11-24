package org.safehaus.subutai.plugin.hadoop.impl.handler;

import org.junit.Before;
import org.mockito.Mockito;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class NodeOperationHandlerTest {
    NodeOperationHandler nodeOperationHandler;
    HadoopImpl hadoopImpl;
    @Before
    public void setUp() throws Exception {
        Tracker tracker = mock(Tracker.class);
        hadoopImpl = mock(HadoopImpl.class);
        Mockito.when(hadoopImpl.getTracker()).thenReturn(tracker);
        nodeOperationHandler = new NodeOperationHandler(hadoopImpl,"test","test", NodeOperationType.INSTALL, NodeType.NAMENODE);

        verify(hadoopImpl).getTracker();
    }
}