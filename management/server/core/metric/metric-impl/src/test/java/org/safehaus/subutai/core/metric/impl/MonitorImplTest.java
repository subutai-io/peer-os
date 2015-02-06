package org.safehaus.subutai.core.metric.impl;


import java.io.PrintStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.metric.api.AlertListener;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.metric.api.MonitoringSettings;
import org.safehaus.subutai.core.metric.api.ResourceHostMetric;
import org.safehaus.subutai.core.peer.api.ContainerGroup;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.RemotePeer;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
    private static final UUID HOST_ID = UUID.randomUUID();
    private static final String HOST = "test";
    private static final double METRIC_VALUE = 123;
    private static final String METRIC_JSON = " {\"host\":\"test\", \"totalRam\":\"123\"," +
            "\"availableRam\":\"123\", \"usedRam\":\"123\", \"usedCpu\":\"123\","
            + "  \"availableDisk\" : \"123\", \"usedDisk\" : \"123\", \"totalDisk\" : \"123\"}";
    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    EntityManager entityManager;
    @Mock
    PeerManager peerManager;
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
    ResourceHost resourceHost;

    @Mock
    MonitoringSettings monitoringSettings;

    @Mock
    EnvironmentManager environmentManager;


    static class MonitorImplExt extends MonitorImpl
    {
        public MonitorImplExt( final PeerManager peerManager, DaoManager daoManager,
                               EnvironmentManager environmentManager ) throws MonitorException
        {
            super( peerManager, daoManager, environmentManager );
        }


        public void setMonitorDao( MonitorDao monitorDao ) {this.monitorDao = monitorDao;}


        public void setNotificationExecutor( ExecutorService executor ) {this.notificationExecutor = executor;}


        public void setMetricListeners( Set<AlertListener> alertListeners ) {this.alertListeners = alertListeners;}
    }


    @Before
    public void setUp() throws Exception
    {
        //Connection connection = mock( Connection.class );
        //PreparedStatement preparedStatement = mock( PreparedStatement.class );
        //when( connection.prepareStatement( anyString() ) ).thenReturn( preparedStatement );

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
        when( localPeer.getId() ).thenReturn( LOCAL_PEER_ID );
        when( localPeer.isLocal() ).thenReturn( true );
        when( remotePeer.isLocal() ).thenReturn( false );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( environment.getContainerHosts() ).thenReturn( Sets.newHashSet( containerHost ) );
        when( environment.getContainerHostById( HOST_ID ) ).thenReturn( containerHost );
        when( containerHost.getEnvironmentId() ).thenReturn( ENVIRONMENT_ID.toString() );
        when( containerHost.getId() ).thenReturn( HOST_ID );
        when( localPeer.getResourceHosts() ).thenReturn( Sets.newHashSet( resourceHost ) );
        when( environmentManager.getEnvironments() ).thenReturn( Sets.newHashSet( environment ) );
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

        verify( containerHostMetric ).getHostId();
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
        //set owner id as local peer
        ContainerGroup containerGroup = mock( ContainerGroup.class );
        when( containerGroup.getInitiatorPeerId() ).thenReturn( LOCAL_PEER_ID );
        when( localPeer.findContainerGroupByContainerId( HOST_ID ) ).thenReturn( containerGroup );
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
        //set owner id as local peer
        ContainerGroup containerGroup = mock( ContainerGroup.class );
        when( containerGroup.getInitiatorPeerId() ).thenReturn( REMOTE_PEER_ID );
        when( localPeer.findContainerGroupByContainerId( HOST_ID ) ).thenReturn( containerGroup );
        when( localPeer.getContainerHostByName( HOST ) ).thenReturn( containerHost );
        Peer ownerPeer = mock( Peer.class );
        when( peerManager.getPeer( REMOTE_PEER_ID ) ).thenReturn( ownerPeer );
        when( ownerPeer.isLocal() ).thenReturn( false );

        monitor.alert( METRIC_JSON );


        verify( ownerPeer ).sendRequest( isA( ContainerHostMetric.class ), anyString(), anyInt() );
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
        String subscriberId = StringUtils.repeat( "s", 100 );
        when( alertListener.getSubscriberId() ).thenReturn( longSubscriberId );
        when( containerHost.getPeer() ).thenReturn( localPeer );
        when( localPeer.getResourceHostByContainerId( HOST_ID.toString() ) ).thenReturn( resourceHost );
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );


        monitor.startMonitoring( alertListener, environment, monitoringSettings );

        verify( monitorDao ).addSubscription( ENVIRONMENT_ID, subscriberId );
    }


    @Test( expected = MonitorException.class )
    public void testStartMonitoringException() throws Exception
    {
        doThrow( new DaoException( "" ) ).when( monitorDao ).addSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );

        monitor.startMonitoring( alertListener, environment, monitoringSettings );
    }


    @Test
    public void testStopMonitoring() throws Exception
    {

        String longSubscriberId = StringUtils.repeat( "s", 101 );
        String subscriberId = StringUtils.repeat( "s", 100 );
        when( alertListener.getSubscriberId() ).thenReturn( longSubscriberId );

        monitor.stopMonitoring( alertListener, environment );

        verify( monitorDao ).removeSubscription( ENVIRONMENT_ID, subscriberId );
    }


    @Test( expected = MonitorException.class )
    public void testStopMonitoringException() throws Exception
    {
        doThrow( new DaoException( "" ) ).when( monitorDao ).removeSubscription( ENVIRONMENT_ID, SUBSCRIBER_ID );

        monitor.stopMonitoring( alertListener, environment );
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
        assertEquals( LOCAL_PEER_ID, metric.getPeerId() );
        assertEquals( HOST, metric.getHost() );
        assertEquals( METRIC_VALUE, metric.getTotalRam() );
    }


    @Test
    public void testGetContainerHostMetrics() throws Exception
    {

        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( commandResult.getStdOut() ).thenReturn( METRIC_JSON );
        when( containerHost.getPeer() ).thenReturn( localPeer );
        ContainerGroup containerGroup = mock( ContainerGroup.class );
        when( localPeer.findContainerGroupByEnvironmentId( ENVIRONMENT_ID ) ).thenReturn( containerGroup );
        when( containerGroup.getContainerIds() ).thenReturn( Sets.newHashSet( HOST_ID ) );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
        when( localPeer.getResourceHostByContainerId( HOST_ID.toString() ) ).thenReturn( resourceHost );
        when( resourceHost.getContainerHostById( HOST_ID.toString() ) ).thenReturn( containerHost );


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
                .sendRequest( anyObject(), anyString(), anyInt(), eq( ContainerHostMetricResponse.class ), anyInt() ) )
                .thenReturn( response );
        ContainerHostMetricImpl metric = JsonUtil.fromJson( METRIC_JSON, ContainerHostMetricImpl.class );
        when( response.getMetrics() ).thenReturn( Sets.newHashSet( metric ) );

        Set<ContainerHostMetric> metrics = monitor.getContainerHostsMetrics( environment );

        ContainerHostMetric metric2 = metrics.iterator().next();
        assertEquals( HOST, metric2.getHost() );
        assertEquals( METRIC_VALUE, metric2.getTotalRam() );


        PeerException exception = mock( PeerException.class );
        doThrow( exception ).when( remotePeer )
                            .sendRequest( anyObject(), anyString(), anyInt(), eq( ContainerHostMetricResponse.class ),
                                    anyInt() );


        monitor.getContainerHostsMetrics( environment );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    //    @Ignore
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
        ContainerGroup containerGroup = mock( ContainerGroup.class );
        when( localPeer.findContainerGroupByEnvironmentId( ENVIRONMENT_ID ) ).thenReturn( containerGroup );
        when( containerGroup.getContainerIds() ).thenReturn( Sets.newHashSet( HOST_ID ) );
        when( localPeer.getResourceHostByContainerId( HOST_ID.toString() ) ).thenReturn( resourceHost );
        when( resourceHost.getContainerHostById( HOST_ID.toString() ) ).thenReturn( containerHost );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );


        monitor.getLocalContainerHostsMetrics( ENVIRONMENT_ID );


        verify( containerHost ).getHostname();
    }


    @Test
    public void testGetLocalContainerHostMetricsWithException() throws Exception
    {
        ContainerGroup containerGroup = mock( ContainerGroup.class );
        when( localPeer.findContainerGroupByEnvironmentId( ENVIRONMENT_ID ) ).thenReturn( containerGroup );
        when( containerGroup.getContainerIds() ).thenReturn( Sets.newHashSet( HOST_ID ) );
        HostNotFoundException exception = mock( HostNotFoundException.class );
        doThrow( exception ).when( localPeer ).getResourceHostByContainerId( anyString() );


        monitor.getLocalContainerHostsMetrics( ENVIRONMENT_ID );


        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    //    @Ignore
    @Test
    public void testGetLocalContainerHostMetricsWithException2() throws Exception
    {
        ContainerGroup containerGroup = mock( ContainerGroup.class );
        when( localPeer.findContainerGroupByEnvironmentId( ENVIRONMENT_ID ) ).thenReturn( containerGroup );
        when( containerGroup.getContainerIds() ).thenReturn( Sets.newHashSet( HOST_ID ) );

        HostNotFoundException exception = mock( HostNotFoundException.class );
        doThrow( exception ).when( localPeer ).getResourceHostByContainerId( HOST_ID.toString() );


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
        assertEquals( LOCAL_PEER_ID, metric.getPeerId() );
        assertEquals( HOST, metric.getHost() );
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
        monitor.activateMonitoringAtRemoteContainers( remotePeer, Sets.newHashSet( containerHost ),
                monitoringSettings );

        verify( remotePeer ).sendRequest( isA( MonitoringActivationRequest.class ), anyString(), anyInt() );


        PeerException exception = mock( PeerException.class );
        doThrow( exception ).when( remotePeer )
                            .sendRequest( isA( MonitoringActivationRequest.class ), anyString(), anyInt() );

        monitor.activateMonitoringAtRemoteContainers( remotePeer, Sets.newHashSet( containerHost ),
                monitoringSettings );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testActivateMonitoringAtLocalContainers() throws Exception
    {
        when( localPeer.getResourceHostByContainerId( HOST_ID.toString() ) ).thenReturn( resourceHost );
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );

        monitor.activateMonitoringAtLocalContainers( Sets.newHashSet( containerHost ), monitoringSettings );

        verify( resourceHost ).execute( any( RequestBuilder.class ) );


        when( commandResult.hasSucceeded() ).thenReturn( false );

        monitor.activateMonitoringAtLocalContainers( Sets.newHashSet( containerHost ), monitoringSettings );

        verify( commandResult ).getStdErr();


        HostNotFoundException exception = mock( HostNotFoundException.class );
        doThrow( exception ).when( localPeer ).getResourceHostByContainerId( HOST_ID.toString() );

        monitor.activateMonitoringAtLocalContainers( Sets.newHashSet( containerHost ), monitoringSettings );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    //    @Ignore
    @Test( expected = MonitorException.class )
    public void testActivateMonitoring() throws Exception
    {
        when( containerHost.getPeer() ).thenReturn( localPeer );
        when( localPeer.getResourceHostByContainerId( HOST_ID.toString() ) ).thenReturn( resourceHost );
        CommandResult commandResult = mock( CommandResult.class );
        when( commandResult.hasSucceeded() ).thenReturn( true );
        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );

        monitor.activateMonitoring( Sets.newHashSet( containerHost ), monitoringSettings );

        verify( resourceHost ).execute( any( RequestBuilder.class ) );


        when( containerHost.getPeer() ).thenReturn( remotePeer );

        monitor.activateMonitoring( Sets.newHashSet( containerHost ), monitoringSettings );

        verify( remotePeer ).sendRequest( isA( MonitoringActivationRequest.class ), anyString(), anyInt() );


        monitor.activateMonitoring( containerHost, monitoringSettings );

        verify( remotePeer, times( 2 ) ).sendRequest( isA( MonitoringActivationRequest.class ), anyString(), anyInt() );

        monitor.activateMonitoring( containerHost, monitoringSettings );

        verify( remotePeer, times( 3 ) ).sendRequest( isA( MonitoringActivationRequest.class ), anyString(), anyInt() );


        Exception exception = mock( RuntimeException.class );
        doThrow( exception ).when( containerHost ).getPeer();

        monitor.activateMonitoring( Sets.newHashSet( containerHost ), monitoringSettings );
    }
}
