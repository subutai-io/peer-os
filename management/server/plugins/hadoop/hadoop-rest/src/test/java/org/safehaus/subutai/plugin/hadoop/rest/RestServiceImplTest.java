package org.safehaus.subutai.plugin.hadoop.rest;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RestServiceImplTest
{
    private RestServiceImpl restService;
    @Mock
    Hadoop hadoop;
    @Mock
    HadoopClusterConfig hadoopClusterConfig;

    @Before
    public void setUp() throws Exception
    {
        restService = new RestServiceImpl();
        restService.setHadoopManager(hadoop);
    }

    @Test
    public void testGetHadoopManager() throws Exception
    {
        restService.getHadoopManager();

        // assertions
        assertNotNull(restService.getHadoopManager());
        assertEquals(hadoop, restService.getHadoopManager());
    }

    @Test
    public void testListClusters() throws Exception
    {
        List<HadoopClusterConfig> myList = Lists.newArrayList();
        myList.add(hadoopClusterConfig);
        when(hadoop.getClusters()).thenReturn(myList);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");

        Response response = restService.listClusters();

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetCluster() throws Exception
    {
        when(hadoop.getCluster("test")).thenReturn(hadoopClusterConfig);

//        Response response = restService.getCluster("test");

        // assertions
//        assertEquals(Response.Status.OK.getStatusCode(),response.getStatus());
    }

    @Test
    public void testInstallCluster() throws Exception
    {
        when(hadoop.installCluster(hadoopClusterConfig)).thenReturn(UUID.randomUUID());
        Response response = restService.installCluster("test", 5, 5);

        // assertions
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    }

    @Test
    public void testUninstallCluster() throws Exception
    {
        when(hadoop.uninstallCluster("test")).thenReturn(UUID.randomUUID());

        Response response = restService.uninstallCluster("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testStartNameNode() throws Exception
    {
        when(hadoop.getCluster("test")).thenReturn(hadoopClusterConfig);
        when(hadoop.startNameNode(hadoopClusterConfig)).thenReturn(UUID.randomUUID());

        Response response = restService.startNameNode("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testStopNameNode() throws Exception
    {
        when(hadoop.getCluster("test")).thenReturn(hadoopClusterConfig);
        when(hadoop.stopNameNode(hadoopClusterConfig)).thenReturn(UUID.randomUUID());

        Response response = restService.stopNameNode("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testStatusNameNode() throws Exception
    {
        when(hadoop.getCluster("test")).thenReturn(hadoopClusterConfig);
        when(hadoop.statusNameNode(hadoopClusterConfig)).thenReturn(UUID.randomUUID());

        Response response = restService.statusNameNode("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testStatusSecondaryNameNode() throws Exception
    {
        when(hadoop.getCluster("test")).thenReturn(hadoopClusterConfig);
        when(hadoop.statusSecondaryNameNode(hadoopClusterConfig)).thenReturn(UUID.randomUUID());

        Response response = restService.statusSecondaryNameNode("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testStartJobTracker() throws Exception
    {
        when(hadoop.getCluster("test")).thenReturn(hadoopClusterConfig);
        when(hadoop.startJobTracker(hadoopClusterConfig)).thenReturn(UUID.randomUUID());

        Response response = restService.startJobTracker("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testStopJobTracker() throws Exception
    {
        when(hadoop.getCluster("test")).thenReturn(hadoopClusterConfig);
        when(hadoop.stopJobTracker(hadoopClusterConfig)).thenReturn(UUID.randomUUID());

        Response response = restService.stopJobTracker("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testStatusJobTracker() throws Exception
    {
        when(hadoop.getCluster("test")).thenReturn(hadoopClusterConfig);
        when(hadoop.statusJobTracker(hadoopClusterConfig)).thenReturn(UUID.randomUUID());

        Response response = restService.statusJobTracker("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testAddNode() throws Exception
    {
        when(hadoop.addNode("test")).thenReturn(UUID.randomUUID());

        Response response = restService.addNode("test");

        // assertions
        assertEquals(Response.Status.CREATED.getStatusCode(),response.getStatus());
    }

    @Test
    public void testStatusDataNode() throws Exception
    {
        when(hadoop.getCluster("test")).thenReturn(hadoopClusterConfig);

        Response response = restService.statusDataNode("test","test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(),response.getStatus());
    }

    @Test
    public void testStatusTaskTracker() throws Exception
    {
        when(hadoop.getCluster("test")).thenReturn(hadoopClusterConfig);

        Response response = restService.statusTaskTracker("test","test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(),response.getStatus());
    }
}