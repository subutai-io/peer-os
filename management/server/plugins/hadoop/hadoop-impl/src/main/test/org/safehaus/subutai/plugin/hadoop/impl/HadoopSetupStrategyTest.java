package org.safehaus.subutai.plugin.hadoop.impl;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import static org.mockito.Mockito.mock;

public class HadoopSetupStrategyTest {
    HadoopSetupStrategy hadoopSetupStrategy;
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSetup() throws Exception {
        Environment environment = mock(Environment.class);
        HadoopClusterConfig hadoopClusterConfig = mock(HadoopClusterConfig.class);
        TrackerOperation trackerOperation = mock(TrackerOperation.class);
        HadoopImpl hadoopImpl = mock(HadoopImpl.class);
        //environment = null;
        hadoopSetupStrategy = new HadoopSetupStrategy(environment,hadoopClusterConfig,trackerOperation,hadoopImpl);
        //hadoopSetupStrategy.setup();

    }
}