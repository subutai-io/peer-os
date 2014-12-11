package org.safehaus.subutai.plugin.hadoop.cli;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ListClustersCommandTest
{
    private ListClustersCommand listClustersCommand;
    @Mock
    Hadoop hadoop;
    @Mock
    HadoopClusterConfig hadoopClusterConfig;

    @Before
    public void setUp() throws Exception
    {
        listClustersCommand = new ListClustersCommand();
        listClustersCommand.setHadoopManager(hadoop);
    }

    @Test
    public void testGetHadoopManager() throws Exception
    {
        listClustersCommand.getHadoopManager();

        // assertions
        assertNotNull(listClustersCommand.getHadoopManager());
        assertEquals(hadoop, listClustersCommand.getHadoopManager());
    }

    @Test
    public void testSetHadoopManager() throws Exception
    {
        listClustersCommand.setHadoopManager(hadoop);

        // assertions
        assertNotNull(listClustersCommand.getHadoopManager());
    }

    @Test
    public void testDoExecute() throws Exception
    {
        List<HadoopClusterConfig> myList = new ArrayList<>();
        myList.add(hadoopClusterConfig);
        when(hadoopClusterConfig.getClusterName()).thenReturn("test");
        when(hadoop.getClusters()).thenReturn(myList);

        listClustersCommand.doExecute();

        // assertions
        verify(hadoop).getClusters();
    }

    @Test
    public void testDoExecuteWhenNoHadoopCluster() throws Exception
    {
        List<HadoopClusterConfig> myList = new ArrayList<>();
        when(hadoop.getClusters()).thenReturn(myList);

        listClustersCommand.doExecute();

        // assertions
        verify(hadoop).getClusters();
        assertTrue(myList.isEmpty());
    }
}