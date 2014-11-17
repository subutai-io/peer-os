package org.safehaus.subutai.plugin.hadoop.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import static org.mockito.Mockito.mock;

public class ClusterConfigurationTest {
    ClusterConfiguration clusterConfiguration;
    TrackerOperation trackerOperation;
    HadoopImpl hadoopImpl;
    ConfigBase configBase;
    Environment environment;
    @Before
    public void setUp() throws Exception {
        trackerOperation = mock(TrackerOperation.class);
        hadoopImpl = mock(HadoopImpl.class);
        configBase = mock(HadoopClusterConfig.class);
        environment = mock(Environment.class);
        clusterConfiguration = new ClusterConfiguration(trackerOperation,hadoopImpl);
    }


    @Test
    public void testConfigureCluster() throws Exception {
        clusterConfiguration.configureCluster(configBase,environment);
    }

    @Test
    public void testConstructorConfigureCluster() throws  Exception {
        clusterConfiguration = new ClusterConfiguration(trackerOperation,hadoopImpl);
    }
}