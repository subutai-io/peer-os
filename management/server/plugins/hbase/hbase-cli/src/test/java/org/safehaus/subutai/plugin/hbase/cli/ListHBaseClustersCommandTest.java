package org.safehaus.subutai.plugin.hbase.cli;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListHBaseClustersCommandTest
{
    private ListHBaseClustersCommand listHBaseClustersCommand;
    private HBase hBase;
    private HBaseConfig hBaseConfig;

    @Before
    public void setUp() throws Exception
    {
        hBaseConfig = mock(HBaseConfig.class);
        hBase = mock(HBase.class);

        listHBaseClustersCommand = new ListHBaseClustersCommand();
    }

    @Test
    public void testGetHbaseManager() throws Exception
    {
        listHBaseClustersCommand.setHbaseManager(hBase);
        listHBaseClustersCommand.getHbaseManager();

        // assertions
        assertNotNull(listHBaseClustersCommand.getHbaseManager());
        assertEquals(hBase,listHBaseClustersCommand.getHbaseManager());
    }

    @Test
    public void testSetHbaseManager() throws Exception
    {
        listHBaseClustersCommand.setHbaseManager(hBase);
        listHBaseClustersCommand.getHbaseManager();

        // assertions
        assertNotNull(listHBaseClustersCommand.getHbaseManager());
        assertEquals(hBase,listHBaseClustersCommand.getHbaseManager());

    }

    @Test
    public void testDoExecute() throws Exception
    {
        listHBaseClustersCommand.setHbaseManager(hBase);
        List<HBaseConfig> myList = new ArrayList<>();
        myList.add(hBaseConfig);

        when(hBase.getClusters()).thenReturn(myList);

        listHBaseClustersCommand.doExecute();

        // assertions
        verify(hBase).getClusters();
        verify(hBaseConfig).getClusterName();
    }
}