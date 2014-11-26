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

public class RemoveNodeOperationHandlerTest {
    RemoveNodeOperationHandler removeNodeOperationHandler;
    TrackerOperation trackerOperation;
    UUID uuid;
    DataSource dataSource;
    ExecutorService executorService;


    @Before
    public void setUp() {
        dataSource = mock(DataSource.class);
        executorService = mock(ExecutorService.class);
        trackerOperation = mock(TrackerOperation.class);
        uuid = new UUID(50, 50);
        Tracker tracker = mock(Tracker.class);
        String clusterName = "test";
        String lxcHostName = "test";
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);

        removeNodeOperationHandler = new RemoveNodeOperationHandler(hadoop, clusterName, lxcHostName);

        assertEquals(uuid,trackerOperation.getId());
        assertEquals(tracker,hadoop.getTracker());
        assertEquals(executorService,hadoop.getExecutor());

    }

    @Test
    public void testRun() {
        Tracker tracker = mock(Tracker.class);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        HadoopImpl hadoop = new HadoopImpl(dataSource);
        when(trackerOperation.getId()).thenReturn(uuid);
        when(tracker.createTrackerOperation(anyString(), anyString())).thenReturn(trackerOperation);
        hadoop.setTracker(tracker);
        hadoop.setExecutor(executorService);
        removeNodeOperationHandler.run();

        assertEquals(uuid,trackerOperation.getId());
        assertEquals(tracker,hadoop.getTracker());
        assertEquals(executorService,hadoop.getExecutor());

    }

}