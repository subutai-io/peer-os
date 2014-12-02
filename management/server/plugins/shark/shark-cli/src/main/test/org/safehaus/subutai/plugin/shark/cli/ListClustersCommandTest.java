package org.safehaus.subutai.plugin.shark.cli;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.plugin.shark.api.Shark;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ListClustersCommandTest
{
    private ListClustersCommand listClustersCommand;
    private Shark shark;
    private SharkClusterConfig sharkClusterConfig;

    @Before
    public void setUp() throws Exception
    {
        sharkClusterConfig = mock(SharkClusterConfig.class);
        shark = mock(Shark.class);
        listClustersCommand = new ListClustersCommand();
    }

    @Test
    public void testGetSharkManager() throws Exception
    {
        listClustersCommand.setSharkManager(shark);
        listClustersCommand.getSharkManager();

        // assertions
        assertNotNull(listClustersCommand.getSharkManager());
        assertEquals(shark,listClustersCommand.getSharkManager());
    }

    @Test
    public void testSetSharkManager() throws Exception
    {
        listClustersCommand.setSharkManager(shark);

        // assertions
        assertNotNull(listClustersCommand.getSharkManager());
        assertEquals(shark,listClustersCommand.getSharkManager());
    }

    @Test
    public void testDoExecutePrintClusterName() throws Exception
    {
        List<SharkClusterConfig> myList = mock(List.class);
        myList.add(sharkClusterConfig);
        when(shark.getClusters()).thenReturn(myList);
        when(myList.isEmpty()).thenReturn(false);
        Iterator<SharkClusterConfig> iterator = mock(Iterator.class);
        when(myList.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(sharkClusterConfig);
        when(sharkClusterConfig.getClusterName()).thenReturn("test");

        listClustersCommand.setSharkManager(shark);
        listClustersCommand.doExecute();

        // assertions
        assertFalse(myList.isEmpty());
        assertEquals("test",sharkClusterConfig.getClusterName());
    }

    @Test
    public void testDoExecutePrintNoSharkCluster() throws Exception
    {
        List<SharkClusterConfig> myList = mock(List.class);
        myList.add(sharkClusterConfig);
        when(shark.getClusters()).thenReturn(myList);
        when(myList.isEmpty()).thenReturn(true);

        listClustersCommand.setSharkManager(shark);
        listClustersCommand.doExecute();

        // assertions
        assertTrue(myList.isEmpty());
    }

}