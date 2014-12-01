package org.safehaus.subutai.plugin.shark.rest;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.plugin.shark.api.Shark;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;

import java.util.List;
import java.util.UUID;

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
    public void setUp() throws Exception
    {

        restService = new RestServiceImpl(shark);
    }

    @Test
    public void testListClusters() throws Exception
    {
        List<SharkClusterConfig> myList = Lists.newArrayList();
        myList.add(sharkClusterConfig);
        when(shark.getClusters()).thenReturn(myList);
        when(sharkClusterConfig.getClusterName()).thenReturn("test");

        restService.listClusters();
    }

    @Test
    public void testGetCluster() throws Exception
    {
        SharkClusterConfig sharkClusterConfig1 = new SharkClusterConfig();
        sharkClusterConfig1.setClusterName("test");
        when(shark.getCluster(anyString())).thenReturn(sharkClusterConfig1);

        restService.getCluster("test");

    }

    @Test
    public void testInstallCluster() throws Exception
    {
        when(shark.installCluster(any(SharkClusterConfig.class))).thenReturn(UUID.randomUUID());
        restService.installCluster("test");
    }

    @Test
    public void testUninstallCluster() throws Exception
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
    public void testActualizeMasterIP() throws Exception
    {

    }
}