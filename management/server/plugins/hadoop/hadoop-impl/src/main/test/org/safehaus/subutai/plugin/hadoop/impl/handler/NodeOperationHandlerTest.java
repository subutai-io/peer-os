package org.safehaus.subutai.plugin.hadoop.impl.handler;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;

import static org.mockito.Mockito.*;

public class NodeOperationHandlerTest {
    NodeOperationHandler nodeOperationHandler;
    HadoopImpl hadoopImpl;
    @Before
    public void setUp() throws Exception {
        Tracker tracker = mock(Tracker.class);
        hadoopImpl = mock(HadoopImpl.class);
        when(hadoopImpl.getTracker()).thenReturn(tracker);
        nodeOperationHandler = new NodeOperationHandler(hadoopImpl,"test","test", NodeOperationType.INSTALL, NodeType.NAMENODE);

        verify(hadoopImpl).getTracker();
    }

    @Test
    public void testRun() throws Exception {
//        ContainerHost containerHost = mock(ContainerHost.class);
//        ContainerHost containerHost2 = mock(ContainerHost.class);
//        Set<ContainerHost> mySet = mock(Set.class);
//        mySet.add(containerHost);
//        mySet.add(containerHost2);
//
//        Environment environment = mock(Environment.class);
//
//        Iterator<ContainerHost> iterator = mock(Iterator.class);
//        when(environment.getContainers().iterator()).thenReturn(mySet.iterator());
//        when(mySet.iterator()).thenReturn(iterator);
//        when(iterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
//        when(iterator.next()).thenReturn(containerHost).thenReturn(containerHost2);
//
//        EnvironmentManager environmentManager = mock(EnvironmentManager.class);
//        HadoopClusterConfig hadoopClusterConfig = mock(HadoopClusterConfig.class);
//        when(hadoopImpl.getCluster("test")).thenReturn(hadoopClusterConfig);
//        when(hadoopImpl.getEnvironmentManager()).thenReturn(environmentManager);
//        nodeOperationHandler.run();
    }
}