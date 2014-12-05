package org.safehaus.subutai.plugin.hbase.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;

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
    }

    @Test
    public void testGetHbaseManager() throws Exception
    {
        restService.setHbaseManager(hBase);
        restService.getHbaseManager();

        // assertions
        assertNotNull(restService.getHbaseManager());
        assertEquals(hBase,restService.getHbaseManager());

    }

    @Test
    public void testSetHbaseManager() throws Exception
    {
        restService.setHbaseManager(hBase);
        restService.getHbaseManager();

        // assertions
        assertNotNull(restService.getHbaseManager());
        assertEquals(hBase,restService.getHbaseManager());
    }

    @Test
    public void testCreateCluster() throws Exception
    {
        restService.setHbaseManager(hBase);

        when(hBase.installCluster(any(HBaseConfig.class))).thenReturn(UUID.randomUUID());

//        restService.createCluster("test");
    }

    @Test
    public void testDestroyCluster() throws Exception
    {
        restService.setHbaseManager(hBase);
        when(hBase.uninstallCluster(anyString())).thenReturn(UUID.randomUUID());
        restService.destroyCluster("test");
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
    public void testDestroyNode() throws Exception
    {

    }

    @Test
    public void testCheckNode() throws Exception
    {

    }

    @Test
    public void testAddNode() throws Exception
    {

    }
}