package io.subutai.core.metric.impl;


import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Environment;
import io.subutai.common.exception.DaoException;
import io.subutai.common.metric.ContainerHostMetric;
import io.subutai.common.metric.HistoricalMetric;
import io.subutai.common.metric.MetricType;
import io.subutai.common.metric.OwnerResourceUsage;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.RemotePeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.metric.api.AlertListener;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.metric.api.MonitoringSettings;
import io.subutai.core.peer.api.PeerManager;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for MonitorImpl
 */
@Ignore
@RunWith( MockitoJUnitRunner.class )
public class MonitorImplTest
{
    private static final String SUBSCRIBER_ID = "subscriber";
    private static final String ENVIRONMENT_ID = UUID.randomUUID().toString();
    private static final String LOCAL_PEER_ID = UUID.randomUUID().toString();
    private static final String REMOTE_PEER_ID = UUID.randomUUID().toString();
    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final String HOST = "test";
    private static final double METRIC_VALUE = 123;
    private static final String METRIC_JSON =
            "{\"host\":\"test\", \"totalRam\":\"123\", \"availableRam\":\"123\", " + "\"usedRam\":\"123\","
                    + "  \"usedCpu\":\"123\", \"availableDiskRootfs\":\"123\", " + "\"availableDiskVar\":\"123\","
                    + "  \"availableDiskHome\":\"123\", \"availableDiskOpt\":\"123\", " + "\"usedDiskRootfs\":\"123\","
                    + "  \"usedDiskVar\":\"123\", \"usedDiskHome\":\"123\", \"usedDiskOpt\":\"123\", "
                    + "\"totalDiskRootfs\":\"123\"," + "  \"totalDiskVar\":\"123\", \"totalDiskHome\":\"123\", "
                    + "\"totalDiskOpt\":\"123\"}";
    private static final Long USER_ID = 123l;
    private static final int PID = 123;
    private static final String OWNER_ID = UUID.randomUUID().toString();
    private static final String CONTAINER_ID = "con_id";
    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    EntityManager entityManager;
    @Mock
    PeerManager peerManager;
    @Mock
    EnvironmentManager environmentManager;
    @Mock
    MonitorDao monitorDao;
    @Mock
    DaoManager daoManager;
    @Mock
    ContainerHostMetricImpl containerHostMetric;
    @Mock
    AlertListener alertListener;
    Set<AlertListener> alertListeners;
    MonitorImplExt monitor;
    @Mock
    ExecutorService notificationService;
    @Mock
    Environment environment;
    @Mock
    LocalPeer localPeer;
    @Mock
    RemotePeer remotePeer;

    @Mock
    ContainerHost containerHost;
    @Mock
    EnvironmentContainerHost environmentContainerHost;

    @Mock
    ResourceHost resourceHost;

    @Mock
    MonitoringSettings monitoringSettings;

    @Mock
    User user;

    @Mock
    ContainerId containerId;


    static class MonitorImplExt extends MonitorImpl
    {
        public MonitorImplExt( PeerManager peerManager, DaoManager daoManager, EnvironmentManager environmentManager )
                throws MonitorException
        {
            super( peerManager, daoManager, environmentManager );
        }


        public void setMonitorDao( MonitorDao monitorDao ) {this.monitorDao = monitorDao;}


        public void setNotificationExecutor( ExecutorService executor ) {this.notificationExecutor = executor;}


        public void setMetricListeners( Set<AlertListener> alertListeners ) {this.alertListeners = alertListeners;}


        @Override
        public List<HistoricalMetric> getHistoricalMetric( final Host host, final MetricType metricType )
        {
            return null;
        }
    }


    @Before
    public void setUp() throws Exception
    {

        when( containerId.getId() ).thenReturn( CONTAINER_ID );
        when( entityManagerFactory.createEntityManager() ).thenReturn( entityManager );
        when( daoManager.getEntityManagerFactory() ).thenReturn( entityManagerFactory );


        monitor = new MonitorImplExt( peerManager, daoManager, environmentManager );
        monitor.setMonitorDao( monitorDao );


        containerHostMetric = mock( ContainerHostMetricImpl.class );
        when( containerHostMetric.getEnvironmentId() ).thenReturn( ENVIRONMENT_ID );
        when( containerHostMetric.getHostId() ).thenReturn( HOST_ID );
        when( alertListener.getSubscriberId() ).thenReturn( SUBSCRIBER_ID );
        alertListeners = Sets.newHashSet( alertListener );
        monitor.setMetricListeners( alertListeners );
        monitor.setNotificationExecutor( notificationService );
        when( monitorDao.getEnvironmentSubscribersIds( ENVIRONMENT_ID ) )
                .thenReturn( Sets.newHashSet( SUBSCRIBER_ID ) );
        when( environment.getId() ).thenReturn( ENVIRONMENT_ID );
        when( environment.getUserId() ).thenReturn( USER_ID );
        //when( identityManager.getUser( USER_ID ) ).thenReturn( user );
        when( localPeer.getId() ).thenReturn( LOCAL_PEER_ID );
        when( localPeer.isLocal() ).thenReturn( true );
        when( remotePeer.isLocal() ).thenReturn( false );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( environment.getContainerHosts() ).thenReturn( Sets.newHashSet( environmentContainerHost ) );
        when( environment.getContainerHostById( HOST_ID ) ).thenReturn( environmentContainerHost );
        when( environmentContainerHost.getEnvironmentId() ).thenReturn( ENVIRONMENT_ID );
        when( containerHost.getId() ).thenReturn( HOST_ID );
        when( localPeer.getResourceHosts() ).thenReturn( Sets.newHashSet( resourceHost ) );
        when( environmentManager.loadEnvironment( ENVIRONMENT_ID ) ).thenReturn( environment );
        when( resourceHost.getPeer() ).thenReturn( localPeer );
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
        Set<AlertListener> alertListeners = Sets.newHashSet();
        monitor.setMetricListeners( alertListeners );

        monitor.addAlertListener( alertListener );

        assertTrue( alertListeners.contains( alertListener ) );
    }


    @Test
    public void testRemoveMetricListener() throws Exception
    {
        monitor.removeAlertListener( alertListener );

        assertFalse( alertListeners.contains( alertListener ) );
    }


    @Test
    public void testNotify() throws Exception
    {

        ArgumentCaptor<AlertNotifier> alertNotifierArgumentCaptor = ArgumentCaptor.forClass( AlertNotifier.class );

        monitor.notifyListener( containerHostMetric, SUBSCRIBER_ID );

        verify( notificationService ).execute( alertNotifierArgumentCaptor.capture() );
        assertEquals( alertListener, alertNotifierArgumentCaptor.getValue().listener );
        assertEquals( containerHostMetric, alertNotifierArgumentCaptor.getValue().metric );
    }


    @Test
    public void testAlertThresholdExcess() throws Exception
    {

        monitor.notifyOnAlert( containerHostMetric );

        //verify( identityManager ).loginWithToken( anyString() );
    }


    @Test( expected = MonitorException.class )
    public void testAlertThresholdExcessException() throws Exception
    {
        doThrow( new DaoException( "" ) ).when( monitorDao ).getEnvironmentSubscribersIds( ENVIRONMENT_ID );

        monitor.notifyOnAlert( containerHostMetric );
    }


    @Test
    public void testAlertThresholdExcessLocalPeer() throws Exception
    {
        when( localPeer.getContainerHostByName( HOST ) ).thenReturn( containerHost );
        Peer ownerPeer = mock( Peer.class );
        when( peerManager.getPeer( LOCAL_PEER_ID ) ).thenReturn( ownerPeer );
        when( ownerPeer.isLocal() ).thenReturn( true );


        monitor.alert( METRIC_JSON );

        verify( monitorDao ).getEnvironmentSubscribersIds( ENVIRONMENT_ID );
    }


    @Test
    public void testAlertThresholdExcessRemotePeer() throws Exception
    {
        when( localPeer.getContainerHostByName( HOST ) ).thenReturn( containerHost );
        Peer ownerPeer = mock( Peer.class );
        when( peerManager.getPeer( REMOTE_PEER_ID ) ).thenReturn( ownerPeer );
        when( ownerPeer.isLocal() ).thenReturn( false );

        monitor.alert( METRIC_JSON );


        verify( ownerPeer ).sendRequest( isA( ContainerHostMetric.class ), anyString(), anyInt(), anyMap() );
    }


    @Test( expected = MonitorException.class )
    public void testAlertThresholdExcessException2() throws Exception
    {

        when( localPeer.getContainerHostByName( HOST ) ).thenThrow( new HostNotFoundException( "" ) );

        monitor.alert( METRIC_JSON );
    }


    @Test
    public void testStartMonitoring() throws Exception
    {

        String longSubscriberId = StringUtils.repeat( "s", 101 );
        when( alertListener.getSubscriberId() ).thenReturn( longSubscriberId );
        when( containerHost.getPeer() ).thenReturn( localPeer );
        when( localPeer.getResourceHostByContainerId( HOST_ID ) ).thenReturn( resourceHost );
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );


        monitor.startMonitoring( SUBSCRIBER_ID, environment, monitoringSettings );

        verify( monitorDao ).addSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );
    }


    @Test
    public void testStartMonitoringContainer() throws Exception
    {

        String longSubscriberId = StringUtils.repeat( "s", 101 );
        when( alertListener.getSubscriberId() ).thenReturn( longSubscriberId );
        when( containerHost.getPeer() ).thenReturn( localPeer );
        when( localPeer.getResourceHostByContainerId( HOST_ID ) ).thenReturn( resourceHost );
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );


        monitor.startMonitoring( SUBSCRIBER_ID, containerHost, monitoringSettings );

        verify( monitorDao ).addSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );
    }


    @Test( expected = MonitorException.class )
    public void testStartMonitoringException() throws Exception
    {
        doThrow( new DaoException( "" ) ).when( monitorDao ).addSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );

        monitor.startMonitoring( SUBSCRIBER_ID, environment, monitoringSettings );
    }


    @Test( expected = MonitorException.class )
    public void testStartMonitoringContainerException() throws Exception
    {
        doThrow( new DaoException( "" ) ).when( monitorDao ).addSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );

        monitor.startMonitoring( SUBSCRIBER_ID, containerHost, monitoringSettings );
    }


    @Test
    public void testStopMonitoring() throws Exception
    {

        String longSubscriberId = StringUtils.repeat( "s", 101 );
        when( alertListener.getSubscriberId() ).thenReturn( longSubscriberId );

        monitor.stopMonitoring( SUBSCRIBER_ID, environment );

        verify( monitorDao ).removeSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );
    }


    @Test( expected = MonitorException.class )
    public void testStopMonitoringException() throws Exception
    {
        doThrow( new DaoException( "" ) ).when( monitorDao ).removeSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );

        monitor.stopMonitoring( SUBSCRIBER_ID, environment );
    }


    @Test
    public void testGetResourceHostMetrics() throws Exception
    {
        CommandResult commandResult = mock( CommandResult.class );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( commandResult.getStdOut() ).thenReturn( METRIC_JSON );

        Set<ResourceHostMetric> metrics = monitor.getResourceHostsMetrics();

        ResourceHostMetric metric = metrics.iterator().next();
        //        assertEquals( LOCAL_PEER_ID, metric.getPeerId() );
        assertEquals( HOST, metric.getHostName() );
        assertEquals( METRIC_VALUE, metric.getTotalRam() );
    }


    @Test
    public void testGetContainerHostMetrics() throws Exception
    {

        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( commandResult.getStdOut() ).thenReturn( METRIC_JSON );
        when( containerHost.getPeer() ).thenReturn( localPeer );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( resourceHost.getPeer() ).thenReturn( localPeer );
        when( localPeer.getResourceHostByContainerId( HOST_ID ) ).thenReturn( resourceHost );
        when( resourceHost.getContainerHostById( HOST_ID ) ).thenReturn( containerHost );


        Set<ContainerHostMetric> metrics = monitor.getContainerHostsMetrics( environment );

        ContainerHostMetric metric = metrics.iterator().next();
        assertEquals( ENVIRONMENT_ID, metric.getEnvironmentId() );
        assertEquals( HOST, metric.getHost() );
        assertEquals( METRIC_VALUE, metric.getTotalRam() );
    }


    @Test
    public void testGetContainerHostMetrics2() throws Exception
    {

        when( containerHost.getPeer() ).thenReturn( remotePeer );
        ContainerHostMetricResponse response = mock( ContainerHostMetricResponse.class );
        when( remotePeer
                .sendRequest( anyObject(), anyString(), anyInt(), eq( ContainerHostMetricResponse.class ), anyInt(),
                        anyMap() ) ).thenReturn( response );
        ContainerHostMetricImpl metric = JsonUtil.fromJson( METRIC_JSON, ContainerHostMetricImpl.class );
        when( response.getMetrics() ).thenReturn( Sets.newHashSet( metric ) );

        Set<ContainerHostMetric> metrics = monitor.getContainerHostsMetrics( environment );

        ContainerHostMetric metric2 = metrics.iterator().next();
        assertEquals( HOST, metric2.getHost() );
        assertEquals( METRIC_VALUE, metric2.getTotalRam() );


        PeerException exception = mock( PeerException.class );
        doThrow( exception ).when( remotePeer )
                            .sendRequest( anyObject(), anyString(), anyInt(), eq( ContainerHostMetricResponse.class ),
                                    anyInt(), anyMap() );


        monitor.getContainerHostsMetrics( environment );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test( expected = MonitorException.class )
    public void testGetContainerHostMetricsWithException() throws Exception
    {
        Exception exception = mock( RuntimeException.class );
        doThrow( exception ).when( containerHost ).getPeer();

        monitor.getContainerHostsMetrics( environment );
    }


    @Test
    public void testGetLocalContainerHostMetrics() throws Exception
    {
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( commandResult.getStdOut() ).thenReturn( METRIC_JSON );
        when( localPeer.getResourceHostByContainerId( HOST_ID ) ).thenReturn( resourceHost );
        when( resourceHost.getContainerHostById( HOST_ID ) ).thenReturn( containerHost );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );


        monitor.getLocalContainerHostsMetrics( ENVIRONMENT_ID );


        verify( containerHost ).getHostname();
    }


    @Test
    public void testtestGetLocalContainerHostMetricsTakingSet() throws Exception
    {
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( commandResult.getStdOut() ).thenReturn( METRIC_JSON );
        when( containerHost.isLocal() ).thenReturn( true );
        when( resourceHost.getContainerHostById( HOST_ID ) ).thenReturn( containerHost );
        when( localPeer.getResourceHostByContainerId( HOST_ID ) ).thenReturn( resourceHost );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );

        Set<ContainerHostMetric> metrics = monitor.getLocalContainerHostsMetrics( Sets.newHashSet( containerHost ) );

        verify( containerHost, atLeastOnce() ).getId();

        ContainerHostMetric metric = monitor.getLocalContainerHostMetric( containerHost );

        assertTrue( metrics.contains( metric ) );
    }


    @Test
    public void testGetLocalContainerHostMetricsWithException() throws Exception
    {
        HostNotFoundException exception = mock( HostNotFoundException.class );
        doThrow( exception ).when( localPeer ).getResourceHostByContainerId( any( String.class ) );


        monitor.getLocalContainerHostsMetrics( ENVIRONMENT_ID );


        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testGetLocalContainerHostMetricsWithException2() throws Exception
    {

        HostNotFoundException exception = mock( HostNotFoundException.class );
        doThrow( exception ).when( localPeer ).getResourceHostByContainerId( HOST_ID );


        monitor.getLocalContainerHostsMetrics( ENVIRONMENT_ID );


        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testGetContainerMetrics() throws Exception
    {
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( commandResult.getStdOut() ).thenReturn( METRIC_JSON );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );

        Set<ContainerHostMetricImpl> metrics = Sets.newHashSet();
        monitor.addLocalContainerHostMetric( ENVIRONMENT_ID, resourceHost, containerHost, metrics );

        ContainerHostMetric metric = metrics.iterator().next();
        assertEquals( ENVIRONMENT_ID, metric.getEnvironmentId() );
        assertEquals( HOST, metric.getHost() );
        assertEquals( METRIC_VALUE, metric.getTotalRam() );
    }


    @Test
    public void testGetContainerMetricsWithException() throws Exception
    {
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( false );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );


        monitor.addLocalContainerHostMetric( ENVIRONMENT_ID, resourceHost, containerHost, null );

        verify( commandResult ).getStdErr();
    }


    @Test
    public void testGetContainerMetricsWithException2() throws Exception
    {
        CommandException exception = mock( CommandException.class );
        doThrow( exception ).when( resourceHost ).execute( any( RequestBuilder.class ) );


        monitor.addLocalContainerHostMetric( ENVIRONMENT_ID, resourceHost, containerHost, null );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testGetResourceMetrics() throws Exception
    {
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( commandResult.getStdOut() ).thenReturn( METRIC_JSON );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        Set<ResourceHostMetric> metrics = Sets.newHashSet();

        monitor.addResourceHostMetric( resourceHost, metrics );


        ResourceHostMetric metric = metrics.iterator().next();
        //        assertEquals( LOCAL_PEER_ID, metric.getPeerId() );
        assertEquals( HOST, metric.getHostName() );
        assertEquals( METRIC_VALUE, metric.getTotalRam() );
    }


    @Test
    public void testGetResourceHostMetric() throws Exception
    {
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( commandResult.getStdOut() ).thenReturn( METRIC_JSON );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        monitor.getResourceHostMetric( resourceHost );

        ResourceHostMetric metric = monitor.getResourceHostMetric( resourceHost );

        //        assertEquals( LOCAL_PEER_ID, metric.getPeerId() );
        assertEquals( HOST, metric.getHostName() );
        assertEquals( METRIC_VALUE, metric.getTotalRam() );
    }


    @Test
    public void testGetResourceMetricsCommandFailed() throws Exception
    {
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( false );
        Set<ResourceHostMetric> metrics = Sets.newHashSet();
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );


        monitor.addResourceHostMetric( resourceHost, metrics );


        verify( resourceHost, times( 2 ) ).getHostname();
    }


    @Test
    public void testGetResourceMetricsCommandException() throws Exception
    {
        Set<ResourceHostMetric> metrics = Sets.newHashSet();
        CommandException exception = mock( CommandException.class );
        doThrow( exception ).when( resourceHost ).execute( any( RequestBuilder.class ) );

        monitor.addResourceHostMetric( resourceHost, metrics );


        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testActivateMonitoringAtRemoteContainers() throws Exception
    {
        monitor.activateMonitoringAtRemoteContainers( remotePeer, Sets.newHashSet( containerHost ), monitoringSettings,
                ENVIRONMENT_ID );

        verify( remotePeer ).sendRequest( isA( MonitoringActivationRequest.class ), anyString(), anyInt(), anyMap() );


        PeerException exception = mock( PeerException.class );
        doThrow( exception ).when( remotePeer )
                            .sendRequest( isA( MonitoringActivationRequest.class ), anyString(), anyInt(), anyMap() );

        monitor.activateMonitoringAtRemoteContainers( remotePeer, Sets.newHashSet( containerHost ), monitoringSettings,
                ENVIRONMENT_ID );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testActivateMonitoringAtLocalContainers() throws Exception
    {
        when( localPeer.getResourceHostByContainerId( HOST_ID ) ).thenReturn( resourceHost );
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );

        monitor.activateMonitoringAtLocalContainers( Sets.newHashSet( containerHost ), monitoringSettings );

        verify( resourceHost ).execute( any( RequestBuilder.class ) );


        when( commandResult.hasSucceeded() ).thenReturn( false );

        monitor.activateMonitoringAtLocalContainers( Sets.newHashSet( containerHost ), monitoringSettings );

        verify( commandResult ).getStdErr();


        HostNotFoundException exception = mock( HostNotFoundException.class );
        doThrow( exception ).when( localPeer ).getResourceHostByContainerId( HOST_ID );

        monitor.activateMonitoringAtLocalContainers( Sets.newHashSet( containerHost ), monitoringSettings );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test( expected = MonitorException.class )
    public void testActivateMonitoring() throws Exception
    {
        when( containerHost.getPeer() ).thenReturn( localPeer );
        when( localPeer.getResourceHostByContainerId( HOST_ID ) ).thenReturn( resourceHost );
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );

        monitor.activateMonitoring( Sets.newHashSet( containerHost ), monitoringSettings, ENVIRONMENT_ID );

        verify( resourceHost ).execute( any( RequestBuilder.class ) );


        when( containerHost.getPeer() ).thenReturn( remotePeer );

        monitor.activateMonitoring( Sets.newHashSet( containerHost ), monitoringSettings, ENVIRONMENT_ID );

        verify( remotePeer ).sendRequest( isA( MonitoringActivationRequest.class ), anyString(), anyInt(), anyMap() );


        monitor.activateMonitoring( containerHost, monitoringSettings );

        verify( remotePeer, times( 2 ) )
                .sendRequest( isA( MonitoringActivationRequest.class ), anyString(), anyInt(), anyMap() );

        monitor.activateMonitoring( containerHost, monitoringSettings );

        verify( remotePeer, times( 3 ) )
                .sendRequest( isA( MonitoringActivationRequest.class ), anyString(), anyInt(), anyMap() );


        Exception exception = mock( RuntimeException.class );
        doThrow( exception ).when( containerHost ).getPeer();

        monitor.activateMonitoring( Sets.newHashSet( containerHost ), monitoringSettings, ENVIRONMENT_ID );
    }


    @Test
    public void testHistoricalMetrics()
    {
        Monitor monitor1 = null;
        try
        {
            monitor1 = new MonitorImpl( peerManager, daoManager, environmentManager );
        }
        catch ( MonitorException e )
        {
            e.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder();
        CommandResult commandResult = mock( CommandResult.class );
        stringBuilder.append( "1425289620: 1.2902400000e+06" ).append( System.getProperty( "line.separator" ) )
                     .append( "1425289680: 1.2902400000e+06" ).append( System.getProperty( "line.separator" ) );
        try
        {
            when( commandResult.hasSucceeded() ).thenReturn( true );
            when( peerManager.getLocalPeer().getResourceHostByContainerId( any( String.class ) ) )
                    .thenReturn( resourceHost );
            when( commandResult.getStdOut() ).thenReturn( stringBuilder.toString() );
            when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        }
        catch ( CommandException | HostNotFoundException e )
        {
            e.printStackTrace();
        }
        assert monitor1 != null;
        List<HistoricalMetric> historicalMetric = monitor1.getHistoricalMetric( containerHost, MetricType.CPU );
        assertNotNull( historicalMetric );
        assertTrue( historicalMetric.size() == 2 );
    }


    @Test( expected = MonitorException.class )
    public void testGetProcessResourceUsage() throws Exception
    {
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true ).thenReturn( false );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( localPeer.getResourceHostByContainerName( containerHost.getHostname() ) ).thenReturn( resourceHost );

        monitor.getProcessResourceUsage( containerId, PID );

        verify( commandResult ).getStdOut();

        monitor.getProcessResourceUsage( containerId, PID );
    }


    @Test
    public void testGetOwnerResourceUsage() throws Exception
    {
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( commandResult.getStdOut() ).thenReturn( METRIC_JSON );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( localPeer.getContainerHostById( HOST_ID ) ).thenReturn( containerHost );
        when( containerHost.isLocal() ).thenReturn( true );
        when( localPeer.getResourceHostByContainerId( HOST_ID ) ).thenReturn( resourceHost );
        when( resourceHost.getContainerHostById( HOST_ID ) ).thenReturn( containerHost );

        OwnerResourceUsage ownerResourceUsage = monitor.getOwnerResourceUsage( OWNER_ID );

        assertEquals( METRIC_VALUE, ownerResourceUsage.getUsedCpu() );
    }
}
