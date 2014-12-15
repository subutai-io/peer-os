package org.safehaus.subutai.plugin.accumulo.cli;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ListClustersCommandTest
{
    private ListClustersCommand listClustersCommand;
    @Mock
    Accumulo accumulo;
    @Mock
    AccumuloClusterConfig accumuloClusterConfig;

    @Before
    public void setUp() throws Exception
    {
        listClustersCommand = new ListClustersCommand();
        listClustersCommand.setAccumuloManager(accumulo);
    }

    @Test
    public void testGetAccumuloManager() throws Exception
    {
        Accumulo ac = listClustersCommand.getAccumuloManager();

        // assertions
        assertNotNull(ac);
        assertEquals(accumulo, ac);
    }

    @Test
    public void testDoExecute() throws Exception
    {
        List<AccumuloClusterConfig> myList = new ArrayList<>();
        myList.add(accumuloClusterConfig);
        when(accumulo.getClusters()).thenReturn(myList);
        when(accumuloClusterConfig.getClusterName()).thenReturn("test");

        listClustersCommand.doExecute();

        // assertions
        assertNotNull(accumulo.getClusters());
        verify(accumuloClusterConfig).getClusterName();
    }

    @Test
    public void testDoExecuteWhenNoAccumuloCluster()
    {
        List<AccumuloClusterConfig> myList = new ArrayList<>();
        when(accumulo.getClusters()).thenReturn(myList);

        listClustersCommand.doExecute();

        // assertions
        verify(accumulo).getClusters();
    }
}