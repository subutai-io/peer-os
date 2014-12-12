package org.safehaus.subutai.plugin.accumulo.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
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
    private String config2 =
            "{\"clusterName\": \"my-accumulo-cluster\",\"instanceName\": \"instance-name\",\"password\": " +
                    "\"password\",\"masterNode\": \"master-node-hostname\",\"gcNode\": \"gc-node-hostname\"," +
                    "\"monitor\": \"monitor-node-hostname\",\"tracers\": [\"lxc-2\",\"lxc-1\"],\"slaves\": " +
                    "[\"lxc-3\",\"lxc-4\"]}";
    @Mock
    Accumulo accumulo;
    @Mock
    Hadoop hadoop;
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    HadoopClusterConfig hadoopClusterConfig;
    @Mock
    AccumuloClusterConfig accumuloClusterConfig;
    @Mock
    Environment environment;
    @Mock
    ContainerHost containerHost;

    @Before
    public void setUp() throws Exception
    {
        restService = new RestServiceImpl();
        restService.setHadoop(hadoop);
        restService.setEnvironmentManager(environmentManager);
        restService.setAccumuloManager(accumulo);
    }

    @Test
    public void testListClusters() throws Exception
    {
        List<AccumuloClusterConfig> myList = new ArrayList<>();
        myList.add(accumuloClusterConfig);
        when(accumulo.getClusters()).thenReturn(myList);
        when(accumuloClusterConfig.getClusterName()).thenReturn("test");


        Response response = restService.listClusters();

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testGetCluster() throws Exception
    {
        AccumuloClusterConfig accumuloClusterConfig1 = new AccumuloClusterConfig();
        when(accumulo.getCluster(anyString())).thenReturn(accumuloClusterConfig1);

        restService.getCluster("test");
    }

    @Test
    public void testCreateCluster() throws Exception
    {
        when(environmentManager.getEnvironmentByUUID(any(UUID.class))).thenReturn(environment);
        when(hadoop.getCluster(anyString())).thenReturn(hadoopClusterConfig);
        when(hadoopClusterConfig.getEnvironmentId()).thenReturn(UUID.randomUUID());
        when(environment.getContainerHostByHostname(anyString())).thenReturn(containerHost);
        when(accumulo.installCluster(accumuloClusterConfig)).thenReturn(UUID.randomUUID());

        restService.createCluster(config2);

    }

    @Test
    public void testDestroyCluster() throws Exception
    {
        when(accumulo.uninstallCluster(anyString())).thenReturn(UUID.randomUUID());
        Response response = restService.destroyCluster("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testStartCluster() throws Exception
    {
        when(accumulo.startCluster(anyString())).thenReturn(UUID.randomUUID());
        Response response = restService.startCluster("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testStopCluster() throws Exception
    {
        when(accumulo.stopCluster(anyString())).thenReturn(UUID.randomUUID());
        Response response = restService.stopCluster("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testAddNode() throws Exception
    {
        when(accumulo.addNode(anyString(), anyString(), any(NodeType.class))).thenReturn(UUID.randomUUID());
        Response response = restService.addNode("test", "test", "MASTER_NODE");

        // assertions
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    }

    @Test
    public void testDestroyNode() throws Exception
    {
        when(accumulo.destroyNode(anyString(), anyString(), any(NodeType.class))).thenReturn(UUID.randomUUID());
        Response response = restService.destroyNode("test", "test", "MASTER_NODE");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testCheckNode() throws Exception
    {
        when(accumulo.checkNode(anyString(), anyString())).thenReturn(UUID.randomUUID());
        Response response = restService.checkNode("test", "test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testGetAccumuloManager() throws Exception
    {
        restService.getAccumuloManager();
    }

    @Test
    public void testGetHadoop() throws Exception
    {
        restService.getHadoop();
    }

    @Test
    public void testGetEnvironmentManager() throws Exception
    {
        restService.getEnvironmentManager();
    }
}