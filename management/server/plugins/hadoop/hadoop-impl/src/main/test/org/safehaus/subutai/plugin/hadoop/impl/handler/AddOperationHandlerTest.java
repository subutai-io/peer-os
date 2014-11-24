package org.safehaus.subutai.plugin.hadoop.impl.handler;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;

import javax.sql.DataSource;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AddOperationHandlerTest {
    AddOperationHandler addOperationHandler;
    TrackerOperation trackerOperation;
    UUID uuid;
    DataSource dataSource;
    ExecutorService executorService;
    Tracker tracker;
    @Before
    public void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        executorService = mock(ExecutorService.class);
        trackerOperation = mock(TrackerOperation.class);
        uuid = new UUID(50, 50);
        tracker = mock(Tracker.class);

        String clusterName = "test";
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        addOperationHandler = new AddOperationHandler(hadoop, clusterName, 5);

        assertEquals(uuid,trackerOperation.getId());
        assertEquals(tracker,hadoop.getTracker());
        assertEquals(executorService,hadoop.getExecutor());

    }

    @Test
    public void testRun() throws Exception {
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        addOperationHandler.run();

        assertEquals(uuid,trackerOperation.getId());
        assertEquals(tracker,hadoop.getTracker());
        assertEquals(executorService,hadoop.getExecutor());
    }
}