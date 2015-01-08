package org.safehaus.subutai.plugin.elasticsearch.rest;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RestServiceImplTest
{
    private RestServiceImpl restService;
    @Mock
    Elasticsearch elasticsearch;
    @Mock
    ElasticsearchClusterConfiguration elasticsearchClusterConfiguration;

    @Before
    public void setUp() throws Exception
    {
        when(elasticsearch.getCluster("test")).thenReturn(elasticsearchClusterConfiguration);

        restService = new RestServiceImpl();
        restService.setElasticsearch(elasticsearch);
    }

    @Test
    public void testListClusters() throws Exception
    {
        List<ElasticsearchClusterConfiguration> myList = Lists.newArrayList();
        myList.add(elasticsearchClusterConfiguration);
        when(elasticsearch.getClusters()).thenReturn(myList);
        when(elasticsearchClusterConfiguration.getClusterName()).thenReturn("test");

        Response response = restService.listClusters();

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testInstallCluster() throws Exception
    {
        when(elasticsearch.installCluster(elasticsearchClusterConfiguration)).thenReturn(UUID.randomUUID());
        Response response = restService.installCluster("test", 5);

        // assertions
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    }

    @Test
    public void testUninstallCluster() throws Exception
    {
        when(elasticsearch.uninstallCluster("test")).thenReturn(UUID.randomUUID());

        Response response = restService.uninstallCluster("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCheckAllNodes() throws Exception
    {
        when(elasticsearch.checkAllNodes(elasticsearchClusterConfiguration)).thenReturn(UUID.randomUUID());

        Response response = restService.checkAllNodes("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testStartAllNodes() throws Exception
    {
        when(elasticsearch.startAllNodes(elasticsearchClusterConfiguration)).thenReturn(UUID.randomUUID());

        Response response = restService.startAllNodes("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testStopAllNodes() throws Exception
    {
        when(elasticsearch.stopAllNodes(elasticsearchClusterConfiguration)).thenReturn(UUID.randomUUID());

        Response response = restService.stopAllNodes("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testAddNode() throws Exception
    {
        when(elasticsearch.addNode(anyString(), anyString())).thenReturn(UUID.randomUUID());

        Response response = restService.addNode("test", "test");

        // assertions
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDestroyNode() throws Exception
    {
        when(elasticsearch.destroyNode(anyString(), anyString())).thenReturn(UUID.randomUUID());

        Response response = restService.destroyNode("test", "test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }
}