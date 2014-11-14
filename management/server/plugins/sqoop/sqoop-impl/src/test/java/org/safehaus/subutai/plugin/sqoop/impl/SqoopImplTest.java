package org.safehaus.subutai.plugin.sqoop.impl;


import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.common.mock.TrackerOperationMock;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.sqoop.api.SetupType;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
import org.safehaus.subutai.plugin.sqoop.api.setting.ExportSetting;
import org.safehaus.subutai.plugin.sqoop.api.setting.ImportSetting;


@RunWith( MockitoJUnitRunner.class )
public class SqoopImplTest
{
    @Mock
    private SqoopImpl sqoop;

    @Mock
    private Tracker tracker;

    private TrackerOperation trackOperation;


    @Before
    public void setUp()
    {
        sqoop.executor = Mockito.mock( ExecutorService.class );
        trackOperation = new TrackerOperationMock();

        Mockito.when( sqoop.getTracker() ).thenReturn( tracker );
        Mockito.when( tracker.createTrackerOperation( SqoopConfig.PRODUCT_KEY, "desc" ) )
                .thenReturn( trackOperation );
    }


    @After
    public void tearDown()
    {
    }


    @Test
    public void testInstallCluster_SqoopConfig()
    {
        SqoopConfig config = new SqoopConfig();
        UUID trackId = sqoop.installCluster( config );

        Assert.assertEquals( trackOperation.getId(), trackId );
    }


    @Test
    public void testInstallCluster_SqoopConfig_HadoopClusterConfig()
    {
        SqoopConfig config = new SqoopConfig();
        HadoopClusterConfig hadoopConfig = new HadoopClusterConfig();
        UUID trackId = sqoop.installCluster( config, hadoopConfig );

        Assert.assertEquals( trackOperation.getId(), trackId );
    }


    @Test
    public void testUninstallCluster()
    {
        UUID trackId = sqoop.uninstallCluster( Matchers.any( String.class ) );
        Assert.assertEquals( trackOperation.getId(), trackId );
    }


    @Test
    public void testGetClusters()
    {
    }


    @Test
    public void testGetCluster()
    {
    }


    @Test
    public void testIsInstalled()
    {
        UUID trackId = sqoop.isInstalled( Matchers.any( String.class ), Matchers.any( String.class ) );
        Assert.assertEquals( trackOperation.getId(), trackId );
    }


    @Test
    public void testDestroyNode()
    {
        UUID trackId = sqoop.destroyNode( Matchers.any( String.class ), Matchers.any( String.class ) );
        Assert.assertEquals( trackOperation.getId(), trackId );
    }


    @Test
    public void testAddNode()
    {
        UUID trackId = sqoop.addNode( null, null );
        Assert.assertNull( "Null expected that indicates no action is performed", trackId );
    }


    @Test
    public void testExportData()
    {
        UUID trackId = sqoop.exportData( new ExportSetting() );
        Assert.assertEquals( trackOperation.getId(), trackId );
    }


    @Test
    public void testImportData()
    {
        UUID trackId = sqoop.importData( new ImportSetting() );
        Assert.assertEquals( trackOperation.getId(), trackId );
    }


    @Test
    public void testGetClusterSetupStrategy()
    {
        // init real instance
        sqoop = new SqoopImpl( Mockito.mock( DataSource.class ) );

        SqoopConfig config = new SqoopConfig();
        // no setup type
        ClusterSetupStrategy s = sqoop.getClusterSetupStrategy( new Environment( "environment" ), config, trackOperation );
        Assert.assertNull( s );

        config.setSetupType( SetupType.OVER_HADOOP );
        s = sqoop.getClusterSetupStrategy( new Environment( "environment" ), config, trackOperation );
        Assert.assertTrue( s instanceof SetupStrategyOverHadoop );

        config.setSetupType( SetupType.WITH_HADOOP );
        s = sqoop.getClusterSetupStrategy( new Environment( "environment" ), config, trackOperation );
        Assert.assertTrue( s instanceof SetupStrategyWithHadoop );
    }

}

