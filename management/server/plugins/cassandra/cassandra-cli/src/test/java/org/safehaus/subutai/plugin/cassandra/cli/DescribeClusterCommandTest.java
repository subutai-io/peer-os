package org.safehaus.subutai.plugin.cassandra.cli;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DescribeClusterCommandTest
{
    private DescribeClusterCommand describeClusterCommand;
    @Mock
    Cassandra cassandra;
    @Mock
    CassandraClusterConfig cassandraClusterConfig;

    @Before
    public void setUp() 
    {
        describeClusterCommand = new DescribeClusterCommand();
    }

    @Test
    public void testGetCassandraManager() 
    {
        describeClusterCommand.setCassandraManager(cassandra);
        describeClusterCommand.getCassandraManager();

        // assertions
        assertNotNull(describeClusterCommand.getCassandraManager());
        assertEquals(cassandra, describeClusterCommand.getCassandraManager());

    }

    @Test
    public void testSetCassandraManager() 
    {
        describeClusterCommand.setCassandraManager(cassandra);
        describeClusterCommand.getCassandraManager();

        // assertions
        assertNotNull(describeClusterCommand.getCassandraManager());
        assertEquals(cassandra, describeClusterCommand.getCassandraManager());
    }

    @Test
    public void testDoExecute() 
    {
        UUID uuid = new UUID(50, 50);
        Set<UUID> mySet = new HashSet<>();
        mySet.add(uuid);

        when(cassandra.getCluster(anyString())).thenReturn(cassandraClusterConfig);
        when(cassandraClusterConfig.getNodes()).thenReturn(mySet);
        when(cassandraClusterConfig.getSeedNodes()).thenReturn(mySet);

        describeClusterCommand.setCassandraManager(cassandra);
        describeClusterCommand.doExecute();

        // assertions
        assertNotNull(cassandra.getCluster(anyString()));
        verify(cassandraClusterConfig).getClusterName();
        verify(cassandraClusterConfig).getNodes();
        verify(cassandraClusterConfig).getSeedNodes();
        verify(cassandraClusterConfig).getDataDirectory();
        verify(cassandraClusterConfig).getCommitLogDirectory();
        verify(cassandraClusterConfig).getSavedCachesDirectory();
        verify(cassandraClusterConfig).getDomainName();
    }

    @Test
    public void testDoExecuteWhenCassandraClusterConfigIsNull()
    {
        when(cassandra.getCluster(anyString())).thenReturn(null);

        describeClusterCommand.setCassandraManager(cassandra);
        describeClusterCommand.doExecute();

        assertNull(cassandra.getCluster(anyString()));
    }
}