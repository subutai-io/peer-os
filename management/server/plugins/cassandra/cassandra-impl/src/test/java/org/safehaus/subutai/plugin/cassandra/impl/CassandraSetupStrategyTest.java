package org.safehaus.subutai.plugin.cassandra.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.dao.PluginDAO;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CassandraSetupStrategyTest
{
    private CassandraSetupStrategy cassandraSetupStrategy;
    private UUID uuid;
    @Mock
    CassandraImpl cassandraImpl;
    @Mock
    Tracker tracker;
    @Mock
    TrackerOperation trackerOperation;
    @Mock
    CassandraClusterConfig cassandraClusterConfig;
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    Environment environment;
    @Mock
    ContainerHost containerHost;
    @Mock
    CommandResult commandResult;
    @Mock
    PluginDAO pluginDAO;


    @Before
    public void setUp() 
    {
        uuid = new UUID(50,50);

        cassandraSetupStrategy = new CassandraSetupStrategy(environment,cassandraClusterConfig,trackerOperation,cassandraImpl);
    }

    @Test
    public void testSetup() throws CommandException, ClusterSetupException
    {
        Set<ContainerHost> mySet = new HashSet<>();
        mySet.add(containerHost);

        // mock setup method
        when(cassandraClusterConfig.getClusterName()).thenReturn("test");
        when(cassandraClusterConfig.getCommitLogDirectory()).thenReturn("test");
        when(cassandraClusterConfig.getDataDirectory()).thenReturn("test");
        when(cassandraClusterConfig.getSavedCachesDirectory()).thenReturn("test");
        when(cassandraClusterConfig.getDomainName()).thenReturn("test");
        when(cassandraClusterConfig.getProductName()).thenReturn("test");
        when(cassandraClusterConfig.getTemplateName()).thenReturn("test");

        when(cassandraImpl.getCluster(anyString())).thenReturn(null);
        when(environment.getContainerHosts()).thenReturn(mySet);
        when(containerHost.getId()).thenReturn(uuid);

        when(cassandraClusterConfig.getNumberOfSeeds()).thenReturn(1);

        // mock ClusterConfiguration (cofigureCluster method)
        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(cassandraImpl.getPluginDAO()).thenReturn(pluginDAO);
        when(pluginDAO.saveInfo(anyString(),anyString(),any())).thenReturn(true);
        when(environment.getId()).thenReturn(uuid);

        cassandraSetupStrategy.setup();

        // asserts
        assertNull(cassandraImpl.getCluster(anyString()));
        assertNotNull(environment.getContainerHosts());
        verify(cassandraClusterConfig).setEnvironmentId(uuid);
        assertTrue(pluginDAO.saveInfo(anyString(),anyString(),any()));
    }

    @Test (expected = ClusterSetupException.class)
    public void testRunWhenClusterDoesNotExist() throws ClusterSetupException
    {
        when(cassandraClusterConfig.getClusterName()).thenReturn("test");
        when(cassandraClusterConfig.getCommitLogDirectory()).thenReturn("test");
        when(cassandraClusterConfig.getDataDirectory()).thenReturn("test");
        when(cassandraClusterConfig.getSavedCachesDirectory()).thenReturn("test");
        when(cassandraClusterConfig.getDomainName()).thenReturn("test");
        when(cassandraClusterConfig.getProductName()).thenReturn("test");
        when(cassandraClusterConfig.getTemplateName()).thenReturn("test");

        when(cassandraImpl.getCluster("test")).thenReturn(cassandraClusterConfig);

        cassandraSetupStrategy.setup();
    }

    @Test (expected = ClusterSetupException.class)
    public void testRunWhenMalformedClusterConfiguration() throws ClusterSetupException
    {
        cassandraSetupStrategy.setup();
    }

}