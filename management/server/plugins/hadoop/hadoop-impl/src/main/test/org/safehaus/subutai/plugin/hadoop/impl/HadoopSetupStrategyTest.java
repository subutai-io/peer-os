package org.safehaus.subutai.plugin.hadoop.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

public class HadoopSetupStrategyTest {
    HadoopSetupStrategy hadoopSetupStrategy;
    Environment environment;
    HadoopClusterConfig hadoopClusterConfig;
    TrackerOperation trackerOperation;
    HadoopImpl hadoopImpl;
    Agent agent;
    Iterator<ContainerHost> iterator;

    ContainerHost containerHost;
    ContainerHost containerHost2;
    ContainerHost containerHost3;
    ContainerHost containerHost4;

    @Before
    public void setUp() throws Exception {
        PluginDAO pluginDAO = mock(PluginDAO.class);
        environment = mock(Environment.class);
        hadoopClusterConfig = mock(HadoopClusterConfig.class);
        trackerOperation = mock(TrackerOperation.class);
        hadoopImpl = mock(HadoopImpl.class);
        when(hadoopImpl.getPluginDAO()).thenReturn(pluginDAO);
        agent = mock(Agent.class);
        iterator = mock(Iterator.class);

        containerHost = mock(ContainerHost.class);
        containerHost2 = mock(ContainerHost.class);
        containerHost3 = mock(ContainerHost.class);
        containerHost4 = mock(ContainerHost.class);

    }

    @Test
    public void testSetup() throws Exception {
        when(environment.getContainerHostByUUID(hadoopClusterConfig.getNameNode())).thenReturn(containerHost);
        when(environment.getContainerHostByUUID(hadoopClusterConfig.getJobTracker())).thenReturn(containerHost);
        when(environment.getContainerHostByUUID(hadoopClusterConfig.getSecondaryNameNode())).thenReturn(containerHost);

        Set<UUID> myUUID = mock(Set.class);
        myUUID.add(UUID.randomUUID());
        myUUID.add(UUID.randomUUID());
        myUUID.add(UUID.randomUUID());

        Agent agent = mock(Agent.class);
        ContainerHost containerHost = mock(ContainerHost.class);
        ContainerHost containerHost2 = mock(ContainerHost.class);
        ContainerHost containerHost3 = mock(ContainerHost.class);
        ContainerHost containerHost4 = mock(ContainerHost.class);
        Set<ContainerHost> mySet = mock(Set.class);
        mySet.add(containerHost);
        mySet.add(containerHost2);
        mySet.add(containerHost3);
        mySet.add(containerHost4);

        when(environment.getContainers()).thenReturn(mySet).thenReturn(mySet);
        when(hadoopClusterConfig.getAllMasterNodes()).thenReturn(myUUID);
        Iterator<ContainerHost> iterator = mock(Iterator.class);
        when(mySet.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(containerHost).thenReturn(containerHost2).thenReturn(containerHost3).thenReturn(containerHost4).thenReturn(containerHost).thenReturn(containerHost2).thenReturn(containerHost3).thenReturn(containerHost4);
        when(mySet.size()).thenReturn(5);
        when(containerHost.getAgent()).thenReturn(agent);
        when(containerHost2.getAgent()).thenReturn(agent);
        when(containerHost3.getAgent()).thenReturn(agent);
        when(containerHost4.getAgent()).thenReturn(agent);
        when(agent.getUuid()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID());
        hadoopSetupStrategy = new HadoopSetupStrategy(environment,hadoopClusterConfig,trackerOperation,hadoopImpl);

        assertEquals(hadoopClusterConfig,hadoopSetupStrategy.setup());
    }

    @Test
    public void testSetMasterNodes () throws Exception {
        Set<ContainerHost> mySet = mock(Set.class);
        mySet.add(containerHost);
        mySet.add(containerHost2);
        mySet.add(containerHost3);
        mySet.add(containerHost4);
        UUID uuid = new UUID(50,50);
        UUID uuid2 = new UUID(55,50);
        UUID uuid3 = new UUID(56,50);
        UUID uuid4 = new UUID(58,50);

        when(environment.getContainers()).thenReturn(mySet);
        when(mySet.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(containerHost).thenReturn(containerHost2).thenReturn(containerHost3).thenReturn(containerHost4);
        when(mySet.size()).thenReturn(5);
        when(containerHost.getAgent()).thenReturn(agent);
        when(containerHost2.getAgent()).thenReturn(agent);
        when(containerHost3.getAgent()).thenReturn(agent);
        when(containerHost4.getAgent()).thenReturn(agent);
        when(agent.getUuid()).thenReturn(uuid).thenReturn(uuid2).thenReturn(uuid3).thenReturn(uuid4).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID());

        hadoopSetupStrategy = new HadoopSetupStrategy(environment,hadoopClusterConfig,trackerOperation,hadoopImpl);
        hadoopSetupStrategy.setMasterNodes();

        assertEquals(uuid4,agent.getUuid());
        verify(containerHost).getAgent();
        verify(containerHost2).getAgent();
        verify(containerHost3).getAgent();
        verify(environment).getContainers();
        verify(hadoopClusterConfig).setNameNode(uuid);
        verify(hadoopClusterConfig).setJobTracker(uuid2);
        verify(hadoopClusterConfig).setSecondaryNameNode(uuid3);
    }

    @Test
    public void testSetSlaveNodes() throws Exception {
        UUID uuid = new UUID(50,50);
        Set<UUID> myUUID = mock(Set.class);
        myUUID.add(uuid);
        myUUID.add(UUID.randomUUID());
        myUUID.add(UUID.randomUUID());
        Set<ContainerHost> mySet = mock(Set.class);
        mySet.add(containerHost);
        mySet.add(containerHost2);

        when(environment.getContainers()).thenReturn(mySet);
        when(hadoopClusterConfig.getAllMasterNodes()).thenReturn(myUUID);
        when(mySet.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(containerHost);
        when(containerHost.getAgent()).thenReturn(agent);
        when(containerHost2.getAgent()).thenReturn(agent);
        when(agent.getUuid()).thenReturn(uuid);
        hadoopSetupStrategy = new HadoopSetupStrategy(environment, hadoopClusterConfig, trackerOperation, hadoopImpl);
        hadoopSetupStrategy.setSlaveNodes();

        assertEquals(uuid,agent.getUuid());
        verify(environment).getContainers();
        verify(hadoopClusterConfig).setDataNodes(anyList());
        verify(hadoopClusterConfig).setTaskTrackers(anyList());
    }


    @Test(expected = ClusterSetupException.class)
    public void testexception() throws Exception {
        Set<ContainerHost> mySet = mock(Set.class);
        mySet.add(containerHost);
        mySet.add(containerHost2);
        mySet.add(containerHost3);
        mySet.add(containerHost4);

        when(environment.getContainers()).thenReturn(mySet);
        when(mySet.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(containerHost).thenReturn(containerHost2).thenReturn(containerHost3).thenReturn(containerHost4);
        when(containerHost.getAgent()).thenReturn(agent);
        when(agent.getUuid()).thenReturn(UUID.randomUUID());
        when(containerHost2.getAgent()).thenReturn(agent);
        when(agent.getUuid()).thenReturn(UUID.randomUUID());
        when(containerHost3.getAgent()).thenReturn(agent);
        when(agent.getUuid()).thenReturn(UUID.randomUUID());
        when(containerHost4.getAgent()).thenReturn(agent);
        when(agent.getUuid()).thenReturn(UUID.randomUUID());

        hadoopSetupStrategy = new HadoopSetupStrategy(environment, hadoopClusterConfig, trackerOperation, hadoopImpl);
        hadoopSetupStrategy.setup();
    }

    @Test(expected = ClusterSetupException.class)
    public void testexception2() throws Exception {
        Set<ContainerHost> mySet = mock(Set.class);
        mySet.add(containerHost);
        mySet.add(containerHost2);
        mySet.add(containerHost3);
        mySet.add(containerHost4);

        when(environment.getContainers()).thenReturn(mySet);
        when(mySet.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(containerHost).thenReturn(containerHost2).thenReturn(containerHost3).thenReturn(containerHost4);
        when(mySet.size()).thenReturn(5);
        when(containerHost.getAgent()).thenReturn(agent);
        when(containerHost2.getAgent()).thenReturn(agent);
        when(containerHost3.getAgent()).thenReturn(agent);
        when(containerHost4.getAgent()).thenReturn(agent);
        when(agent.getUuid()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID()).thenReturn(UUID.randomUUID());

        hadoopSetupStrategy = new HadoopSetupStrategy(environment, hadoopClusterConfig, trackerOperation, hadoopImpl);
        hadoopSetupStrategy.setup();
    }
}