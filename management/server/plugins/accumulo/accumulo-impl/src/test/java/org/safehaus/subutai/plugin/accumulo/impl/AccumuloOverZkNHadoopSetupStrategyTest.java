package org.safehaus.subutai.plugin.accumulo.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccumuloOverZkNHadoopSetupStrategyTest
{
    private AccumuloOverZkNHadoopSetupStrategy accumuloOverZkNHadoopSetupStrategy;
    private UUID uuid;
    @Mock
    AccumuloImpl accumuloImpl;
    @Mock
    AccumuloClusterConfig accumuloClusterConfig;
    @Mock
    Tracker tracker;
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    TrackerOperation trackerOperation;
    @Mock
    Environment environment;
    @Mock
    ContainerHost containerHost;
    @Mock
    CommandResult commandResult;
    @Mock
    ClusterSetupStrategy clusterSetupStrategy;
    @Mock
    HadoopClusterConfig hadoopClusterConfig;
    @Mock
    ZookeeperClusterConfig zookeeperClusterConfig;
    @Mock
    Hadoop hadoop;
    @Mock
    Zookeeper zookeeper;
    @Mock
    PluginDAO pluginDAO;

    @Before
    public void setUp() throws Exception
    {
        when(accumuloClusterConfig.getMasterNode()).thenReturn(UUID.randomUUID());
        when(accumuloClusterConfig.getGcNode()).thenReturn(UUID.randomUUID());
        when(accumuloClusterConfig.getMonitor()).thenReturn(UUID.randomUUID());
        when(accumuloClusterConfig.getClusterName()).thenReturn("test-cluster");
        when(accumuloClusterConfig.getInstanceName()).thenReturn("test-instance");
        when(accumuloClusterConfig.getPassword()).thenReturn("test-password");

        when(accumuloImpl.getHadoopManager()).thenReturn(hadoop);

        uuid = UUID.randomUUID();
        accumuloOverZkNHadoopSetupStrategy = new AccumuloOverZkNHadoopSetupStrategy(environment,
                accumuloClusterConfig, hadoopClusterConfig, trackerOperation, accumuloImpl);
    }

    @Test
    public void testSetup() throws Exception
    {
        List<UUID> myList = new ArrayList<>();
        myList.add(uuid);

        Set<UUID> myUUID = new HashSet<>();
        myUUID.add(uuid);
        when(accumuloClusterConfig.getTracers()).thenReturn(myUUID);
        when(accumuloClusterConfig.getSlaves()).thenReturn(myUUID);

        when(hadoop.getCluster(anyString())).thenReturn(hadoopClusterConfig);
        when(accumuloImpl.getZkManager()).thenReturn(zookeeper);
        when(zookeeper.getCluster(anyString())).thenReturn(zookeeperClusterConfig);
        when(zookeeperClusterConfig.getNodes()).thenReturn(myUUID);
        when(zookeeperClusterConfig.getClusterName()).thenReturn("testClusterName");
        when(environment.getContainerHostById(any(UUID.class))).thenReturn(containerHost);
        when(containerHost.getHostname()).thenReturn("testHostName");
        when(zookeeper.startNode(anyString(), anyString())).thenReturn(uuid);

        when(hadoopClusterConfig.getAllNodes()).thenReturn(myList);
        when(accumuloClusterConfig.getAllNodes()).thenReturn(myUUID);
        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);
        when(commandResult.getStdOut()).thenReturn("Hadoop");


        // mock clusterConfiguration
        when(accumuloImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(zookeeperClusterConfig.getEnvironmentId()).thenReturn(uuid);
        when(environmentManager.getEnvironmentByUUID(uuid)).thenReturn(environment);
        when(accumuloImpl.getPluginDAO()).thenReturn(pluginDAO);

        accumuloOverZkNHadoopSetupStrategy.setup();

        // assertions
        assertNotNull(accumuloImpl.getHadoopManager().getCluster(anyString()));
        assertNotNull(accumuloImpl.getZkManager().getCluster(anyString()));
        verify(containerHost).execute(new RequestBuilder(
                Commands.installCommand + Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_KEY
                        .toLowerCase()).withTimeout(1800));
        verify(trackerOperation).addLog(
                AccumuloClusterConfig.PRODUCT_KEY + " is installed on node " + containerHost.getHostname());
        assertNotNull(accumuloOverZkNHadoopSetupStrategy.setup());
        assertEquals(accumuloClusterConfig,accumuloOverZkNHadoopSetupStrategy.setup());
    }

    @Test(expected = ClusterSetupException.class)
    public void testSetupWhenMalformedConfiguration() throws ClusterSetupException
    {
        accumuloOverZkNHadoopSetupStrategy.setup();
    }

    @Test(expected = ClusterSetupException.class)
    public void testSetupWhenClusterNameExists() throws ClusterSetupException
    {
        Set<UUID> myUUID = new HashSet<>();
        myUUID.add(uuid);
        when(accumuloClusterConfig.getTracers()).thenReturn(myUUID);
        when(accumuloClusterConfig.getSlaves()).thenReturn(myUUID);
        when(accumuloImpl.getCluster(anyString())).thenReturn(accumuloClusterConfig);
        accumuloOverZkNHadoopSetupStrategy.setup();
    }

    @Test(expected = ClusterSetupException.class)
    public void testSetupWhenHadoopClusterConfigIsNull() throws ClusterSetupException
    {
        Set<UUID> myUUID = new HashSet<>();
        myUUID.add(uuid);
        when(accumuloClusterConfig.getTracers()).thenReturn(myUUID);
        when(accumuloClusterConfig.getSlaves()).thenReturn(myUUID);
        when(accumuloImpl.getHadoopManager()).thenReturn(hadoop);
        when(hadoop.getCluster(anyString())).thenReturn(null);

        accumuloOverZkNHadoopSetupStrategy.setup();
    }

    @Test(expected = ClusterSetupException.class)
    public void testSetupWhenZookeperClusterConfigIsNull() throws ClusterSetupException
    {
        Set<UUID> myUUID = new HashSet<>();
        myUUID.add(uuid);
        when(accumuloClusterConfig.getTracers()).thenReturn(myUUID);
        when(accumuloClusterConfig.getSlaves()).thenReturn(myUUID);
        when(hadoop.getCluster(anyString())).thenReturn(hadoopClusterConfig);
        when(accumuloImpl.getZkManager()).thenReturn(zookeeper);
        when(zookeeper.getCluster(anyString())).thenReturn(null);


        accumuloOverZkNHadoopSetupStrategy.setup();
    }

    @Test(expected = ClusterSetupException.class)
    public void testSetupWhenNodesNotBelongToHadoopCluster() throws ClusterSetupException
    {
        Set<UUID> myUUID2 = new HashSet<>();
        myUUID2.add(UUID.randomUUID());
        List<UUID> mylist2 = new ArrayList<>();
        mylist2.add(UUID.randomUUID());
        when(accumuloClusterConfig.getTracers()).thenReturn(myUUID2);
        when(accumuloClusterConfig.getSlaves()).thenReturn(myUUID2);
        when(hadoop.getCluster(anyString())).thenReturn(hadoopClusterConfig);
        when(accumuloImpl.getZkManager()).thenReturn(zookeeper);
        when(zookeeper.getCluster(anyString())).thenReturn(zookeeperClusterConfig);
        when(hadoopClusterConfig.getAllNodes()).thenReturn(mylist2);
        when(accumuloClusterConfig.getAllNodes()).thenReturn(myUUID2);

        accumuloOverZkNHadoopSetupStrategy.setup();
    }

    @Test
    public void testSetupWhenCommandResultNotSucceded() throws CommandException, ClusterSetupException
    {
        List<UUID> myList = new ArrayList<>();
        myList.add(uuid);

        Set<UUID> myUUID = new HashSet<>();
        myUUID.add(uuid);
        when(accumuloClusterConfig.getTracers()).thenReturn(myUUID);
        when(accumuloClusterConfig.getSlaves()).thenReturn(myUUID);

        when(hadoop.getCluster(anyString())).thenReturn(hadoopClusterConfig);
        when(accumuloImpl.getZkManager()).thenReturn(zookeeper);
        when(zookeeper.getCluster(anyString())).thenReturn(zookeeperClusterConfig);
        when(zookeeperClusterConfig.getNodes()).thenReturn(myUUID);
        when(zookeeperClusterConfig.getClusterName()).thenReturn("testClusterName");
        when(environment.getContainerHostById(any(UUID.class))).thenReturn(containerHost);
        when(containerHost.getHostname()).thenReturn("testHostName");
        when(zookeeper.startNode(anyString(), anyString())).thenReturn(uuid);

        when(hadoopClusterConfig.getAllNodes()).thenReturn(myList);
        when(accumuloClusterConfig.getAllNodes()).thenReturn(myUUID);
        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(commandResult.getStdOut()).thenReturn("Hadoop");
        when(commandResult.hasSucceeded()).thenReturn(false);


        // mock clusterConfiguration
        when(accumuloImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(zookeeperClusterConfig.getEnvironmentId()).thenReturn(uuid);
        when(environmentManager.getEnvironmentByUUID(uuid)).thenReturn(environment);
        when(accumuloImpl.getPluginDAO()).thenReturn(pluginDAO);

        accumuloOverZkNHadoopSetupStrategy.setup();

    }

    @Test(expected = ClusterSetupException.class)
    public void testSetupWhenCommandResultNotSucceded2() throws CommandException, ClusterSetupException
    {
        List<UUID> myList = new ArrayList<>();
        myList.add(uuid);

        Set<UUID> myUUID = new HashSet<>();
        myUUID.add(uuid);
        when(accumuloClusterConfig.getTracers()).thenReturn(myUUID);
        when(accumuloClusterConfig.getSlaves()).thenReturn(myUUID);

        when(hadoop.getCluster(anyString())).thenReturn(hadoopClusterConfig);
        when(accumuloImpl.getZkManager()).thenReturn(zookeeper);
        when(zookeeper.getCluster(anyString())).thenReturn(zookeeperClusterConfig);
        when(zookeeperClusterConfig.getNodes()).thenReturn(myUUID);
        when(zookeeperClusterConfig.getClusterName()).thenReturn("testClusterName");
        when(environment.getContainerHostById(any(UUID.class))).thenReturn(containerHost);
        when(containerHost.getHostname()).thenReturn("testHostName");
        when(zookeeper.startNode(anyString(), anyString())).thenReturn(uuid);

        when(hadoopClusterConfig.getAllNodes()).thenReturn(myList);
        when(accumuloClusterConfig.getAllNodes()).thenReturn(myUUID);
        when(containerHost.execute(any(RequestBuilder.class))).thenThrow(CommandException.class);

        accumuloOverZkNHadoopSetupStrategy.setup();
    }

    @Test(expected = ClusterSetupException.class)
    public void testSetupShouldThrowsClusterSetupException() throws Exception
    {
        List<UUID> myList = new ArrayList<>();
        myList.add(uuid);

        Set<UUID> myUUID = new HashSet<>();
        myUUID.add(uuid);
        when(accumuloClusterConfig.getTracers()).thenReturn(myUUID);
        when(accumuloClusterConfig.getSlaves()).thenReturn(myUUID);

        when(hadoop.getCluster(anyString())).thenReturn(hadoopClusterConfig);
        when(accumuloImpl.getZkManager()).thenReturn(zookeeper);
        when(zookeeper.getCluster(anyString())).thenReturn(zookeeperClusterConfig);
        when(zookeeperClusterConfig.getNodes()).thenReturn(myUUID);
        when(zookeeperClusterConfig.getClusterName()).thenReturn("testClusterName");
        when(environment.getContainerHostById(any(UUID.class))).thenReturn(containerHost);
        when(containerHost.getHostname()).thenReturn("testHostName");
        when(zookeeper.startNode(anyString(), anyString())).thenReturn(uuid);

        when(hadoopClusterConfig.getAllNodes()).thenReturn(myList);
        when(accumuloClusterConfig.getAllNodes()).thenReturn(myUUID);
        when(containerHost.execute(any(RequestBuilder.class))).thenReturn(commandResult);
        when(commandResult.hasSucceeded()).thenReturn(true);
        when(commandResult.getStdOut()).thenReturn("Hadoop");


        // mock clusterConfiguration
        when(accumuloImpl.getEnvironmentManager()).thenReturn(environmentManager);
        when(zookeeperClusterConfig.getEnvironmentId()).thenReturn(uuid);
        when(environmentManager.getEnvironmentByUUID(uuid)).thenReturn(environment);
        when(accumuloImpl.getPluginDAO()).thenThrow(ClusterConfigurationException.class);

        accumuloOverZkNHadoopSetupStrategy.setup();
    }


}