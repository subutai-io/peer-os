package org.safehaus.subutai.plugin.hadoop.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.PluginDAO;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HadoopSetupStrategyTest
{
    private HadoopSetupStrategy hadoopSetupStrategy;
    private UUID uuid;
    @Mock
    Environment environment;
    @Mock
    HadoopClusterConfig hadoopClusterConfig;
    @Mock
    TrackerOperation trackerOperation;
    @Mock
    HadoopImpl hadoopImpl;
    @Mock
    ContainerHost containerHost;
    @Mock
    ContainerHost containerHost2;
    @Mock
    ContainerHost containerHost3;
    @Mock
    ContainerHost containerHost4;
    @Mock
    PluginDAO pluginDAO;

    @Before
    public void setUp()
    {
        hadoopSetupStrategy = new HadoopSetupStrategy(environment,hadoopClusterConfig,trackerOperation,hadoopImpl);
    }


    @Test
    public void testSetup() throws ClusterSetupException
    {
        Set<ContainerHost> mySet = new HashSet<>();
        mySet.add(containerHost);
        mySet.add(containerHost2);
        mySet.add(containerHost3);
        mySet.add(containerHost4);
        when(environment.getContainerHosts()).thenReturn(mySet);
        when(containerHost.getId()).thenReturn(UUID.randomUUID());
        when(containerHost2.getId()).thenReturn(UUID.randomUUID());
        when(containerHost3.getId()).thenReturn(UUID.randomUUID());
        when(containerHost.getId()).thenReturn(UUID.randomUUID());

        when(environment.getContainerHostById(hadoopClusterConfig.getNameNode())).thenReturn(containerHost);
        when(environment.getContainerHostById(hadoopClusterConfig.getJobTracker())).thenReturn(containerHost);
        when(environment.getContainerHostById(hadoopClusterConfig.getSecondaryNameNode()))
                .thenReturn(containerHost);
        when(hadoopImpl.getPluginDAO()).thenReturn(pluginDAO);
        when(pluginDAO.saveInfo(anyString(),anyString(),any())).thenReturn(true);

        hadoopSetupStrategy.setup();

        // asserts
        verify(hadoopClusterConfig).setNameNode(any(UUID.class));
        verify(hadoopClusterConfig).setJobTracker(any(UUID.class));
        verify(hadoopClusterConfig).setSecondaryNameNode(any(UUID.class));

        verify(hadoopClusterConfig).setDataNodes(anyList());
        verify(hadoopClusterConfig).setTaskTrackers(anyList());
    }
}