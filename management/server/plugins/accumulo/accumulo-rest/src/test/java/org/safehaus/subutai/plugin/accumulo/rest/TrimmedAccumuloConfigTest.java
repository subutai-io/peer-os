package org.safehaus.subutai.plugin.accumulo.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TrimmedAccumuloConfigTest
{
    private TrimmedAccumuloConfig trimmedAccumuloConfig;

    @Before
    public void setUp() throws Exception
    {
        trimmedAccumuloConfig = new TrimmedAccumuloConfig();
    }

    @Test
    public void testGetClusterName() throws Exception
    {
        trimmedAccumuloConfig.getClusterName();
    }

    @Test
    public void testGetInstanceName() throws Exception
    {
        trimmedAccumuloConfig.getInstanceName();
    }

    @Test
    public void testGetPassword() throws Exception
    {
        trimmedAccumuloConfig.getPassword();
    }

    @Test
    public void testGetMasterNode() throws Exception
    {
        trimmedAccumuloConfig.getMasterNode();
    }

    @Test
    public void testGetGcNode() throws Exception
    {
        trimmedAccumuloConfig.getGcNode();
    }

    @Test
    public void testGetMonitor() throws Exception
    {
        trimmedAccumuloConfig.getMonitor();
    }

    @Test
    public void testGetTracers() throws Exception
    {
        trimmedAccumuloConfig.getTracers();
    }

    @Test
    public void testGetSlaves() throws Exception
    {
        trimmedAccumuloConfig.getSlaves();
    }

    @Test
    public void testGetHadoopClusterName() throws Exception
    {
        trimmedAccumuloConfig.getHadoopClusterName();
    }
}