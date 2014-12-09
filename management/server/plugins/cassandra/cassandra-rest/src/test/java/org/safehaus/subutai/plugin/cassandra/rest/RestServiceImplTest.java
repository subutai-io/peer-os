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

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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

        restService.listClusters();
    }

    @Test
    public void testGetCluster() throws Exception
    {
        when(cassandra.getCluster(anyString())).thenReturn(cassandraClusterConfig);

//        restService.getCluster("test");
    }

    @Test
    public void testCreateCluster() throws Exception
    {
        when(cassandra.installCluster(any(CassandraClusterConfig.class))).thenReturn(UUID.randomUUID());
//        restService.createCluster("BEGIN_OBJECT");
    }

    @Test
    public void testDestroyCluster() throws Exception
    {
        when(cassandra.uninstallCluster(anyString())).thenReturn(UUID.randomUUID());
        Response response = restService.destroyCluster("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testStartCluster() throws Exception
    {
        when(cassandra.startCluster(anyString())).thenReturn(UUID.randomUUID());
        Response response = restService.startCluster("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testStopCluster() throws Exception
    {
        when(cassandra.stopCluster(anyString())).thenReturn(UUID.randomUUID());
        Response response = restService.stopCluster("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testAddNode() throws Exception
    {
        when(cassandra.addNode(anyString(), anyString())).thenReturn(UUID.randomUUID());
        Response response = restService.addNode("test","test");

        // assertions
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    }

    @Test
    public void testCheckNode() throws Exception
    {

    }
}