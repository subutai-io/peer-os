package org.safehaus.subutai.plugin.elasticsearch.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ESSetupStrategyTest
{
    private ESSetupStrategy esSetupStrategy;
    @Mock
    ElasticsearchImpl elasticsearchImpl;
    @Mock
    ElasticsearchClusterConfiguration clusterConfiguration;
    @Mock
    Tracker tracker;
    @Mock
    TrackerOperation trackerOperation;
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
    public void setUp() throws Exception
    {
        when(clusterConfiguration.getClusterName()).thenReturn("test");
        when(clusterConfiguration.getTemplateName()).thenReturn("test");
        when(clusterConfiguration.getNumberOfNodes()).thenReturn(1);

        esSetupStrategy = new ESSetupStrategy(environment, clusterConfiguration, trackerOperation, elasticsearchImpl);
    }

    @Test
    public void testSetup() throws Exception
    {

        Set<ContainerHost> mySet = new HashSet<>();
        mySet.add(containerHost);
        mySet.add(containerHost);
        when(environment.getContainerHosts()).thenReturn(mySet);

        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(elasticsearchImpl.getPluginDAO()).thenReturn(pluginDAO);

        esSetupStrategy.setup();

        // assertions
        assertNotNull(esSetupStrategy.setup());
    }

    @Test (expected = ClusterSetupException.class)
    public void testSetupWhenMalformedConfiguration() throws ClusterSetupException
    {
        esSetupStrategy.setup();
    }

    @Test (expected = ClusterSetupException.class)
    public void testSetupWhenClusterAlreadyExist() throws ClusterSetupException
    {

        when(elasticsearchImpl.getCluster(anyString())).thenReturn(clusterConfiguration);
        esSetupStrategy.setup();
    }

    @Test (expected = ClusterSetupException.class)
    public void testSetupWhenNotEnoughNodes() throws ClusterSetupException
    {
        Set<ContainerHost> mySet = mock(Set.class);
        when(environment.getContainerHosts()).thenReturn(mySet);
        when(mySet.size()).thenReturn(0);

        esSetupStrategy.setup();
    }

}