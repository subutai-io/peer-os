package org.safehaus.subutai.plugin.hbase.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;

import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class RestServiceImplTest
{
    private RestServiceImpl restService;
    @Mock
    HBase hBase;
    @Mock
    HBaseConfig hBaseConfig;


    @Before
    public void setUp() throws Exception
    {
        restService = new RestServiceImpl();
        restService.setHbaseManager(hBase);

    }

    @Test
    public void testGetHbaseManager() throws Exception
    {
        restService.getHbaseManager();

        // assertions
        assertNotNull(restService.getHbaseManager());
        assertEquals(hBase, restService.getHbaseManager());

    }


    @Test
    public void testCreateCluster() throws Exception
    {
        when(hBase.installCluster(any(HBaseConfig.class))).thenReturn(UUID.randomUUID());
        Response response = restService.createCluster("test");

        // assertions
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDestroyCluster() throws Exception
    {
        when(hBase.uninstallCluster(anyString())).thenReturn(UUID.randomUUID());
        Response response = restService.destroyCluster("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testStartCluster() throws Exception
    {
        when(hBase.startCluster(anyString())).thenReturn(UUID.randomUUID());
        Response response = restService.startCluster("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testStopCluster() throws Exception
    {
        when(hBase.stopCluster(anyString())).thenReturn(UUID.randomUUID());
        Response response = restService.stopCluster("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testDestroyNode() throws Exception
    {
        when(hBase.destroyNode(anyString(), anyString())).thenReturn(UUID.randomUUID());
        Response response = restService.destroyNode("test", "test", "test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testAddNode() throws Exception
    {
        when(hBase.addNode(anyString(), anyString())).thenReturn(UUID.randomUUID());
        Response response = restService.addNode("test","test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

}