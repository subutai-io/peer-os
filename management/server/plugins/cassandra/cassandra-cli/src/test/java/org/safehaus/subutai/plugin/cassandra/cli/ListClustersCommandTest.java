package org.safehaus.subutai.plugin.cassandra.cli;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ListClustersCommandTest
{
    private ListClustersCommand listClustersCommand;
    @Mock
    Cassandra cassandra;
    @Mock
    Tracker tracker;
    @Mock
    CassandraClusterConfig cassandraClusterConfig;

    @Before
    public void setUp() 
    {
        listClustersCommand = new ListClustersCommand();
    }

    @Test
    public void testGetCassandraManager() 
    {
        listClustersCommand.setCassandraManager(cassandra);
        listClustersCommand.getCassandraManager();

        // assertions
        assertNotNull(listClustersCommand.getCassandraManager());
        assertEquals(cassandra,listClustersCommand.getCassandraManager());
    }

    @Test
    public void testSetCassandraManager() 
    {
        listClustersCommand.setCassandraManager(cassandra);
        listClustersCommand.getCassandraManager();

        // assertions
        assertNotNull(listClustersCommand.getCassandraManager());
        assertEquals(cassandra,listClustersCommand.getCassandraManager());
    }

    @Test
    public void testGetTracker() 
    {
        listClustersCommand.setTracker(tracker);
        listClustersCommand.getTracker();

        // assertions
        assertNotNull(listClustersCommand.getTracker());
        assertEquals(tracker,listClustersCommand.getTracker());

    }

    @Test
    public void testSetTracker() 
    {
        listClustersCommand.setTracker(tracker);
        listClustersCommand.getTracker();

        // assertions
        assertNotNull(listClustersCommand.getTracker());
        assertEquals(tracker,listClustersCommand.getTracker());

    }

    @Test
    public void testDoExecute() 
    {
        List<CassandraClusterConfig> myList = new ArrayList<>();
        myList.add(cassandraClusterConfig);

        listClustersCommand.setCassandraManager(cassandra);
        when(cassandra.getClusters()).thenReturn(myList);
        listClustersCommand.doExecute();

        // asserts
        assertNotNull(cassandra.getClusters());
        verify(cassandraClusterConfig).getClusterName();
    }

    @Test
    public void testDoExecuteWhenListOfCassandraCluster()
    {
        List<CassandraClusterConfig> myList = new ArrayList<>();
        listClustersCommand.setCassandraManager(cassandra);
        when(cassandra.getClusters()).thenReturn(myList);
        listClustersCommand.doExecute();
    }
}