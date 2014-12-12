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
    private String config2 =
            "{\"clusterName\": \"my-accumulo-cluster\",\"instanceName\": \"instance-name\",\"password\": " +
                    "\"password\",\"masterNode\": \"master-node-hostname\",\"gcNode\": \"gc-node-hostname\"," +
                    "\"monitor\": \"monitor-node-hostname\",\"tracers\": [\"lxc-2\",\"lxc-1\"],\"slaves\": " +
                    "[\"lxc-3\",\"lxc-4\"]}";

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
        CassandraClusterConfig cassandraClusterConfig1 = new CassandraClusterConfig();
        when(cassandra.getCluster(anyString())).thenReturn(cassandraClusterConfig1);

        Response response = restService.getCluster("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateCluster() throws Exception
    {
        when(cassandra.installCluster(any(CassandraClusterConfig.class))).thenReturn(UUID.randomUUID());
        Response response = restService.createCluster(config2);

        // assertions
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
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
        Response response = restService.addNode("test", "test");

        // assertions
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    }

    @Test
    public void testCheckNode() throws Exception
    {
        when(cassandra.checkNode(anyString(), anyString())).thenReturn(UUID.randomUUID());

        Response response = restService.checkNode("test", "test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDestroyNode() throws Exception
    {
        when(cassandra.destroyNode(anyString(), anyString())).thenReturn(UUID.randomUUID());

        Response response = restService.destroyNode("test", "test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

}