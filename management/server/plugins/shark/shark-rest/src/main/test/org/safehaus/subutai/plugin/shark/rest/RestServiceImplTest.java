package org.safehaus.subutai.plugin.shark.rest;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.plugin.shark.api.Shark;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;

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
    Shark shark;
    @Mock
    SharkClusterConfig sharkClusterConfig;


    @Before
    public void setUp() 
    {

        restService = new RestServiceImpl(shark);
    }

    @Test
    public void testListClusters() 
    {
        List<SharkClusterConfig> myList = Lists.newArrayList();
        myList.add(sharkClusterConfig);
        when(shark.getClusters()).thenReturn(myList);
        when(sharkClusterConfig.getClusterName()).thenReturn("test");

        restService.listClusters();
    }

    @Test
    public void testGetCluster() 
    {
        SharkClusterConfig sharkClusterConfig1 = new SharkClusterConfig();
        sharkClusterConfig1.setClusterName("test");
        when(shark.getCluster(anyString())).thenReturn(sharkClusterConfig1);
        restService.getCluster("test");

        Response response = restService.getCluster("test");

        // assertions
        assertEquals("test",sharkClusterConfig1.getClusterName());
        assertEquals(sharkClusterConfig1,shark.getCluster("test"));
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }

    @Test
    public void testInstallCluster() 
    {
        when(shark.installCluster(any(SharkClusterConfig.class))).thenReturn(UUID.randomUUID());
        restService.installCluster("test");

        Response response = restService.installCluster("test");;

        // assertions
        assertEquals( Response.Status.CREATED.getStatusCode(), response.getStatus() );
    }

    @Test
    public void testUninstallCluster() 
    {
        when(shark.uninstallCluster(anyString())).thenReturn(UUID.randomUUID());
        restService.uninstallCluster("test");
        Response response = restService.uninstallCluster("test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    @Test
    public void testAddNode() 
    {
        when(shark.addNode(anyString(), anyString())).thenReturn(UUID.randomUUID());
        restService.addNode("test","test");
        Response response = restService.addNode("test","test");

        // assertions
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDestroyNode() 
    {
        when(shark.destroyNode(anyString(), anyString())).thenReturn(UUID.randomUUID());
        restService.destroyNode("test","test");
        Response response = restService.destroyNode("test","test");

        // assertions
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testActualizeMasterIP() 
    {
        when(shark.actualizeMasterIP(anyString())).thenReturn(UUID.randomUUID());
        restService.actualizeMasterIP("test");
        Response response = restService.actualizeMasterIP("test");

        // assertions
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    }
}