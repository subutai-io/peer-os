package org.safehaus.subutai.plugin.cassandra.rest;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RestServiceImplTest
{
    private RestServiceImpl restService;
    @Mock
    Cassandra cassandra;
    @Mock
    Tracker tracker;
    @Mock
    CassandraClusterConfig cassandraClusterConfig;


    @Before
    public void setUp() throws Exception
    {
        restService = new RestServiceImpl();
        restService.setCassandraManager(cassandra);

    }

    @Test
    public void testGetCassandraManager() throws Exception
    {
        Cassandra cas = restService.getCassandraManager();

        // asserts
        assertEquals(cassandra,cas);
    }

    @Test
    public void testSetCassandraManager() throws Exception
    {
        restService.setCassandraManager(cassandra);
        Cassandra cas = restService.getCassandraManager();

        // asserts
        assertEquals(cassandra, cas);
    }

    @Test
    public void testListClusters() throws Exception
    {
        List<CassandraClusterConfig> myList = Lists.newArrayList();
        myList.add(cassandraClusterConfig);
        when(cassandra.getClusters()).thenReturn(myList);
        when(cassandraClusterConfig.getClusterName()).thenReturn("test");

//        restService.listClusters();
    }

    @Test
    public void testGetCluster() throws Exception
    {

    }

    @Test
    public void testCreateCluster() throws Exception
    {

    }

    @Test
    public void testDestroyCluster() throws Exception
    {

    }

    @Test
    public void testStartCluster() throws Exception
    {

    }

    @Test
    public void testStopCluster() throws Exception
    {

    }

    @Test
    public void testAddNode() throws Exception
    {

    }

    @Test
    public void testDestroyNode() throws Exception
    {

    }

    @Test
    public void testCheckNode() throws Exception
    {

    }
}