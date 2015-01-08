package org.safehaus.subutai.plugin.hbase.cli;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DescribeClusterCommandTest
{
    private HBase hBase;
    private DescribeClusterCommand describeClusterCommand;
    private HBaseConfig hBaseConfig;

    @Before
    public void setUp() throws Exception
    {
        hBaseConfig = mock(HBaseConfig.class);
        hBase = mock(HBase.class);

        describeClusterCommand = new DescribeClusterCommand();
        describeClusterCommand.setHbaseManager(hBase);
    }

    @Test
    public void testGetHbaseManager() throws Exception
    {
        describeClusterCommand.setHbaseManager(hBase);
        describeClusterCommand.getHbaseManager();

        // assertions
        assertNotNull(describeClusterCommand.getHbaseManager());
        assertEquals(hBase,describeClusterCommand.getHbaseManager());
    }

    @Test
    public void testDoExecute() throws Exception
    {
        UUID uuid = new UUID(50,50);
        Set<UUID> mySet = mock(Set.class);
        mySet.add(uuid);
        Set<UUID> mySet2 = mock(Set.class);
        mySet2.add(uuid);
        describeClusterCommand.setHbaseManager(hBase);
        when(hBase.getCluster(anyString())).thenReturn(hBaseConfig);
        when(hBaseConfig.getRegionServers()).thenReturn(mySet);
        Iterator<UUID> iterator = mock(Iterator.class);
        when(mySet.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(uuid);
        when(hBaseConfig.getQuorumPeers()).thenReturn(mySet2);
        Iterator<UUID> iterator2 = mock(Iterator.class);
        when(mySet2.iterator()).thenReturn(iterator2);
        when(iterator2.hasNext()).thenReturn(true).thenReturn(false);
        when(iterator2.next()).thenReturn(uuid);

        describeClusterCommand.doExecute();

        // assertions
        assertNotNull(hBase.getCluster(anyString()));
        verify(hBaseConfig).getClusterName();
        verify(hBaseConfig).getDomainName();
        verify(hBaseConfig).getHbaseMaster();
        verify(hBaseConfig).getBackupMasters();
    }

    @Test
    public void testDoExecuteWhenHbaseConfigIsNull()
    {
        when(hBase.getCluster(anyString())).thenReturn(null);

        describeClusterCommand.doExecute();
    }
}