package org.safehaus.subutai.plugin.storm.impl;


import org.junit.BeforeClass;
import org.junit.Test;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.common.mock.TrackerMock;
import org.safehaus.subutai.plugin.common.mock.TrackerOperationMock;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.storm.impl.handler.StormClusterOperationHandler;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class InstallOperationTest
{

    static StormClusterConfiguration config;


    @BeforeClass
    public static void init()
    {
        config = mock( StormClusterConfiguration.class );
    }

    @Test
    public void testWithExistingCluster()
    {
        StormImpl stormMock = mock( StormImpl.class );
        when( stormMock.getZookeeperManager() ).thenReturn( mock( Zookeeper.class ) );
        when( stormMock.getTracker() ).thenReturn( new TrackerMock() );
        when( stormMock.getEnvironmentManager() ).thenReturn( mock( EnvironmentManager.class ) );
        when( config.getClusterName() ).thenReturn( "test" );
        when( stormMock.getCluster( anyString() ) ).thenReturn( config );
        AbstractOperationHandler operationHandler = new StormClusterOperationHandler( stormMock, config,
                ClusterOperationType.INSTALL );
        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "already exists" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }

    @Test
    public void testWithMalformedConfiguration()
    {
        when( config.getClusterName() ).thenReturn( null );
        when( config.isExternalZookeeper() ).thenReturn( false );
        when( config.getSupervisorsCount() ).thenReturn( 0 );

        StormImpl stormMock = mock( StormImpl.class );
        when( stormMock.getTracker() ).thenReturn( new TrackerMock() );
        when( stormMock.getEnvironmentManager() ).thenReturn( mock( EnvironmentManager.class ) );
        when( stormMock.getCluster( anyString() ) ).thenReturn( new StormClusterConfiguration() );
        AbstractOperationHandler operationHandler = new StormClusterOperationHandler( stormMock, config,
                ClusterOperationType.INSTALL );
        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "Malformed configuration" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.FAILED );
    }


    @Test
    public void testExternalZookeeper() throws EnvironmentBuildException, ClusterSetupException
    {
        when( config.getClusterName() ).thenReturn( "test" );
        when( config.isExternalZookeeper() ).thenReturn( true );

        StormImpl stormMock = mock( StormImpl.class );
        Zookeeper zookeeperMock = mock( Zookeeper.class );
        Environment environment = mock( Environment.class );
        TrackerOperationMock trackerOperationMock = mock( TrackerOperationMock.class );
        StormSetupStrategyDefault stormSetupStrategyDefault = mock( StormSetupStrategyDefault.class );

        when( stormMock.getZookeeperManager() ).thenReturn( zookeeperMock );
        when( stormMock.getTracker() ).thenReturn( new TrackerMock() );
        when( stormMock.getEnvironmentManager() ).thenReturn( mock( EnvironmentManager.class ) );
        when( stormMock.getClusterSetupStrategy( environment, config, trackerOperationMock ) )
                .thenReturn( mock( StormSetupStrategyDefault.class ) );
        when( stormMock.getCluster( anyString() ) ).thenReturn( null );
        when( stormMock.getClusterSetupStrategy( any( Environment.class ), any( StormClusterConfiguration.class ),
                any( TrackerOperation.class ) ) ).thenReturn( stormSetupStrategyDefault );
        when( stormMock.getEnvironmentManager().buildEnvironment( stormMock.getDefaultEnvironmentBlueprint( config ) ) )
                .thenReturn( mock( Environment.class ) );


        AbstractOperationHandler operationHandler = new StormClusterOperationHandler( stormMock, config,
                ClusterOperationType.INSTALL );
        operationHandler.run();

        assertTrue( operationHandler.getTrackerOperation().getLog().contains( "Cluster test set up successfully" ) );
        assertEquals( operationHandler.getTrackerOperation().getState(), OperationState.SUCCEEDED );
    }
}
