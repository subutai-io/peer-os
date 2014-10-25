package org.safehaus.subutai.core.metric.impl;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.MetricListener;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerInterface;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for MonitorImpl
 */
@RunWith( MockitoJUnitRunner.class )
public class MonitorImplTest
{
    private static final String SUBSCRIBER_ID = "subscriber";
    private static final UUID ENVIRONMENT_ID = UUID.randomUUID();
    private static final UUID LOCAL_PEER_ID = UUID.randomUUID();
    private static final UUID REMOTE_PEER_ID = UUID.randomUUID();
    private static final String HOST = "test";
    private static final String METRIC_JSON = " {\"host\":\"test\", \"totalRam\":\"123\"," +
            "\"availableRam\":\"123\", \"usedRam\":\"123\", \"cpuLoad5\":\"123\","
            + "  \"availableDisk\" : \"123\", \"usedDisk\" : \"123\", \"totalDisk\" : \"123\"}";
    @Mock
    DataSource dataSource;
    @Mock
    PeerManager peerManager;
    @Mock
    MonitorDao monitorDao;
    @Mock
    ContainerHostMetric containerHostMetric;
    @Mock
    MetricListener metricListener;
    Set<MetricListener> metricListeners;
    MonitorImplExt monitor;
    @Mock
    ExecutorService notificationService;
    @Mock
    Environment environment;


    static class MonitorImplExt extends MonitorImpl
    {
        public MonitorImplExt( final DataSource dataSource, final PeerManager peerManager ) throws DaoException
        {
            super( dataSource, peerManager );
        }


        public void setMonitorDao( MonitorDao monitorDao ) {this.monitorDao = monitorDao;}


        public void setNotificationExecutor( ExecutorService executor ) {this.notificationExecutor = executor;}


        public void setMetricListeners( Set<MetricListener> metricListeners ) {this.metricListeners = metricListeners;}
    }


    @Before
    public void setUp() throws Exception
    {
        Connection connection = mock( Connection.class );
        PreparedStatement preparedStatement = mock( PreparedStatement.class );
        when( connection.prepareStatement( anyString() ) ).thenReturn( preparedStatement );
        when( dataSource.getConnection() ).thenReturn( connection );
        monitor = new MonitorImplExt( dataSource, peerManager );
        monitor.setMonitorDao( monitorDao );

        containerHostMetric = mock( ContainerHostMetric.class );
        when( containerHostMetric.getEnvironmentId() ).thenReturn( ENVIRONMENT_ID );
        when( metricListener.getSubscriberId() ).thenReturn( SUBSCRIBER_ID );
        metricListeners = Sets.newHashSet( metricListener );
        monitor.setMetricListeners( metricListeners );
        monitor.setNotificationExecutor( notificationService );
        when( monitorDao.getEnvironmentSubscribersIds( ENVIRONMENT_ID ) )
                .thenReturn( Sets.newHashSet( SUBSCRIBER_ID ) );
        when( environment.getId() ).thenReturn( ENVIRONMENT_ID );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullDataSource() throws Exception
    {
        new MonitorImpl( null, peerManager );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullPeerManager() throws Exception
    {
        new MonitorImpl( dataSource, null );
    }


    @Test
    public void testDestroy() throws Exception
    {

        monitor.destroy();

        verify( notificationService ).shutdown();
    }


    @Test
    public void testAddMetricListener() throws Exception
    {
        Set<MetricListener> metricListeners = Sets.newHashSet();
        monitor.setMetricListeners( metricListeners );

        monitor.addMetricListener( metricListener );

        assertTrue( metricListeners.contains( metricListener ) );
    }


    @Test
    public void testRemoveMetricListener() throws Exception
    {
        monitor.removeMetricListener( metricListener );

        assertFalse( metricListeners.contains( metricListener ) );
    }


    @Test
    public void testNotify() throws Exception
    {

        ArgumentCaptor<AlertNotifier> alertNotifierArgumentCaptor = ArgumentCaptor.forClass( AlertNotifier.class );

        monitor.notifyListener( containerHostMetric, SUBSCRIBER_ID );

        verify( notificationService ).execute( alertNotifierArgumentCaptor.capture() );
        assertEquals( metricListener, alertNotifierArgumentCaptor.getValue().listener );
        assertEquals( containerHostMetric, alertNotifierArgumentCaptor.getValue().metric );
    }


    @Test
    public void testAlertThresholdExcess() throws Exception
    {

        monitor.alertThresholdExcess( containerHostMetric );

        verify( containerHostMetric ).getEnvironmentId();
    }


    @Test( expected = MonitorException.class )
    public void testAlertThresholdExcessException() throws Exception
    {
        doThrow( new DaoException( null ) ).when( monitorDao ).getEnvironmentSubscribersIds( ENVIRONMENT_ID );

        monitor.alertThresholdExcess( containerHostMetric );
    }


    @Test
    public void testAlertThresholdExcessLocalPeer() throws Exception
    {
        ContainerHost containerHost = mock( ContainerHost.class );
        when( containerHost.getEnvironmentId() ).thenReturn( ENVIRONMENT_ID );
        //set owner id as local peer
        when( containerHost.getOwnerPeerId() ).thenReturn( LOCAL_PEER_ID );
        LocalPeer localPeer = mock( LocalPeer.class );
        when( localPeer.getId() ).thenReturn( LOCAL_PEER_ID );
        when( localPeer.getContainerHostByName( HOST ) ).thenReturn( containerHost );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        PeerInterface ownerPeer = mock( PeerInterface.class );
        when( peerManager.getPeer( LOCAL_PEER_ID ) ).thenReturn( ownerPeer );
        when( ownerPeer.getId() ).thenReturn( LOCAL_PEER_ID );


        monitor.alertThresholdExcess( METRIC_JSON );

        verify( monitorDao ).getEnvironmentSubscribersIds( ENVIRONMENT_ID );
    }


    @Test
    public void testAlertThresholdExcessRemotePeer() throws Exception
    {
        ContainerHost containerHost = mock( ContainerHost.class );
        when( containerHost.getEnvironmentId() ).thenReturn( ENVIRONMENT_ID );
        //set owner id as local peer
        when( containerHost.getOwnerPeerId() ).thenReturn( REMOTE_PEER_ID );
        LocalPeer localPeer = mock( LocalPeer.class );
        when( localPeer.getId() ).thenReturn( LOCAL_PEER_ID );
        when( localPeer.getContainerHostByName( HOST ) ).thenReturn( containerHost );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        PeerInterface ownerPeer = mock( PeerInterface.class );
        when( peerManager.getPeer( REMOTE_PEER_ID ) ).thenReturn( ownerPeer );
        when( ownerPeer.getId() ).thenReturn( REMOTE_PEER_ID );


        monitor.alertThresholdExcess( METRIC_JSON );

        //todo complete the test when message queue willbe implemented

    }


    @Test( expected = MonitorException.class )
    public void testAlertThresholdExcessException2() throws Exception
    {

        LocalPeer localPeer = mock( LocalPeer.class );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getContainerHostByName( HOST ) ).thenThrow( new PeerException( "" ) );

        monitor.alertThresholdExcess( METRIC_JSON );
    }


    @Test
    public void testStopMonitoring() throws Exception
    {

        String longSubscriberId = StringUtils.repeat( "s", 101 );
        String subscriberId = StringUtils.repeat( "s", 100 );
        when( metricListener.getSubscriberId() ).thenReturn( longSubscriberId );

        monitor.stopMonitoring( metricListener, environment );

        verify( monitorDao ).removeSubscription( ENVIRONMENT_ID, subscriberId );
    }
}
