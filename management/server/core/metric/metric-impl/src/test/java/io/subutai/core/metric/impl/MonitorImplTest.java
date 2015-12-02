package io.subutai.core.metric.impl;


import java.util.Date;
import java.util.Map;
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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Environment;
import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.AlertPack;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.RemotePeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.resource.HistoricalMetrics;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.metric.api.MonitoringSettings;
import io.subutai.core.peer.api.PeerManager;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for MonitorImpl
 */
@Ignore
@RunWith( MockitoJUnitRunner.class )
public class MonitorImplTest
{
    private static final String SUBSCRIBER_ID = "master";
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
    AlertPack alert;
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
    @Mock
    private EnvironmentId environmentId;

    @Mock
    private HostRegistry hostRegistry;


    static class MonitorImplExt extends MonitorImpl
    {
        public MonitorImplExt( PeerManager peerManager, DaoManager daoManager, EnvironmentManager environmentManager,
                               HostRegistry hostRegistry ) throws MonitorException
        {
            super( peerManager, daoManager, environmentManager, hostRegistry );
        }


        public void setMonitorDao( MonitorDao monitorDao ) {this.monitorDao = monitorDao;}


        //        public void setNotificationExecutor( ExecutorService executor ) {this.notificationExecutor =
        // executor;}


        public void setAlertListeners( Set<AlertListener> alertListeners )
        {
            this.alertListeners = alertListeners;
        }
    }


    @Before
    public void setUp() throws Exception
    {

        when( containerId.getId() ).thenReturn( CONTAINER_ID );
        when( entityManagerFactory.createEntityManager() ).thenReturn( entityManager );
        when( daoManager.getEntityManagerFactory() ).thenReturn( entityManagerFactory );


        monitor = new MonitorImplExt( peerManager, daoManager, environmentManager, hostRegistry );
        monitor.setMonitorDao( monitorDao );


        alert = mock( AlertPack.class );
        when( environmentId.getId() ).thenReturn( ENVIRONMENT_ID );
        //        when( alert.getEnvironmentId() ).thenReturn( environmentId );
        //        when( alert.getContainerId() ).thenReturn( containerId );
        when( alertListener.getSubscriberId() ).thenReturn( SUBSCRIBER_ID );
        alertListeners = Sets.newHashSet();
        alertListeners.add( alertListener );
        monitor.setAlertListeners( alertListeners );
        //        monitor.setNotificationExecutor( notificationService );
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
        when( environmentContainerHost.getEnvironmentId() ).thenReturn( environmentId );
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
    public void testAddAlertListener() throws Exception
    {
        Set<AlertListener> alertListeners = Sets.newHashSet();
        monitor.setAlertListeners( alertListeners );

        monitor.addAlertListener( alertListener );

        assertTrue( alertListeners.contains( alertListener ) );
    }


    @Test
    public void testRemoveAlertListener() throws Exception
    {
        monitor.removeAlertListener( alertListener );

        assertFalse( alertListeners.contains( alertListener ) );
    }


    @Test
    public void testNotify() throws Exception
    {

        ArgumentCaptor<AlertNotifier> alertNotifierArgumentCaptor = ArgumentCaptor.forClass( AlertNotifier.class );

        monitor.notifyOnAlert( alert );

        verify( notificationService ).execute( alertNotifierArgumentCaptor.capture() );
        assertEquals( alertListener, alertNotifierArgumentCaptor.getValue().listener );
        assertEquals( alert, alertNotifierArgumentCaptor.getValue().alert );
    }


    //    @Test
    //    public void testAlertThresholdExcess() throws Exception
    //    {
    //
    //        monitor.notifyOnAlert( alert );
    //
    //        //verify( identityManager ).loginWithToken( anyString() );
    //    }
    //
    //
    //    @Test( expected = MonitorException.class )
    //    public void testAlertThresholdExcessException() throws Exception
    //    {
    //        doThrow( new DaoException( "" ) ).when( monitorDao ).getEnvironmentSubscribersIds( ENVIRONMENT_ID );
    //
    //        monitor.notifyOnAlert( alert );
    //    }


    @Test
    public void testAlertThresholdExcessLocalPeer() throws Exception
    {
        when( localPeer.getContainerHostByName( HOST ) ).thenReturn( containerHost );
        Peer ownerPeer = mock( Peer.class );
        when( peerManager.getPeer( LOCAL_PEER_ID ) ).thenReturn( ownerPeer );
        when( ownerPeer.isLocal() ).thenReturn( true );


        //        monitor.alert( METRIC_JSON );

        //        verify( monitorDao ).getEnvironmentSubscribersIds( ENVIRONMENT_ID );
    }


    @Test
    public void testAlertThresholdExcessRemotePeer() throws Exception
    {
        when( localPeer.getContainerHostByName( HOST ) ).thenReturn( containerHost );
        Peer ownerPeer = mock( Peer.class );
        when( peerManager.getPeer( REMOTE_PEER_ID ) ).thenReturn( ownerPeer );
        when( ownerPeer.isLocal() ).thenReturn( false );

        //        monitor.alert( METRIC_JSON );


        //        verify( ownerPeer ).sendRequest( isA( ContainerHostMetric.class ), anyString(), anyInt(), anyMap() );
    }


    @Test( expected = MonitorException.class )
    public void testAlertThresholdExcessException2() throws Exception
    {

        //        when( localPeer.getContainerHostByName( HOST ) ).thenThrow( new HostNotFoundException( "" ) );

        //        monitor.alert( METRIC_JSON );
    }


    //    @Test
    //    public void testGetResourceHostMetrics() throws Exception
    //    {
    //        CommandResult commandResult = mock( CommandResult.class );
    //        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
    //        when( commandResult.hasSucceeded() ).thenReturn( true );
    //        when( commandResult.getStdOut() ).thenReturn( METRIC_JSON );
    //
    //        Set<ResourceHostMetric> metrics = monitor.getResourceHostsMetrics();
    //
    //        ResourceHostMetric metric = metrics.iterator().next();
    //        //        assertEquals( LOCAL_PEER_ID, metric.getPeerId() );
    //        assertEquals( HOST, metric.getHostName() );
    //        assertEquals( METRIC_VALUE, metric.getTotalRam() );
    //    }

    //
    //    @Test
    //    public void testGetContainerHostMetrics() throws Exception
    //    {
    //
    //        CommandResult commandResult = mock( CommandResult.class );
    //        when( commandResult.hasSucceeded() ).thenReturn( true );
    //        when( commandResult.getStdOut() ).thenReturn( METRIC_JSON );
    //        when( containerHost.getPeer() ).thenReturn( localPeer );
    //        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
    //        when( resourceHost.getPeer() ).thenReturn( localPeer );
    //        when( localPeer.getResourceHostByContainerId( HOST_ID ) ).thenReturn( resourceHost );
    //        when( resourceHost.getContainerHostById( HOST_ID ) ).thenReturn( containerHost );
    //
    //
    //        Set<ContainerHostMetric> metrics = monitor.getContainerHostsMetrics( environment );
    //
    //        ContainerHostMetric metric = metrics.iterator().next();
    //        assertEquals( ENVIRONMENT_ID, metric.getEnvironmentId() );
    //        assertEquals( HOST, metric.getHost() );
    //        assertEquals( METRIC_VALUE, metric.getTotalRam() );
    //    }


    //    @Test
    //    public void testGetContainerHostMetrics2() throws Exception
    //    {
    //
    //        when( containerHost.getPeer() ).thenReturn( remotePeer );
    //        ContainerHostMetricResponse response = mock( ContainerHostMetricResponse.class );
    //        when( remotePeer
    //                .sendRequest( anyObject(), anyString(), anyInt(), eq( ContainerHostMetricResponse.class ),
    // anyInt(),
    //                        anyMap() ) ).thenReturn( response );
    //        ContainerHostMetricImpl metric = JsonUtil.fromJson( METRIC_JSON, ContainerHostMetricImpl.class );
    //        when( response.getMetrics() ).thenReturn( Sets.newHashSet( metric ) );
    //
    //        Set<ContainerHostMetric> metrics = monitor.getContainerHostsMetrics( environment );
    //
    //        ContainerHostMetric metric2 = metrics.iterator().next();
    //        assertEquals( HOST, metric2.getHost() );
    //        assertEquals( METRIC_VALUE, metric2.getTotalRam() );
    //
    //
    //        PeerException exception = mock( PeerException.class );
    //        doThrow( exception ).when( remotePeer )
    //                            .sendRequest( anyObject(), anyString(), anyInt(), eq( ContainerHostMetricResponse
    // .class ),
    //                                    anyInt(), anyMap() );
    //
    //
    //        monitor.getContainerHostsMetrics( environment );
    //
    //        verify( exception ).printStackTrace( any( PrintStream.class ) );
    //    }


    //    @Test( expected = MonitorException.class )
    //    public void testGetContainerHostMetricsWithException() throws Exception
    //    {
    //        Exception exception = mock( RuntimeException.class );
    //        doThrow( exception ).when( containerHost ).getPeer();
    //
    //        monitor.getContainerHostsMetrics( environment );
    //    }

    //    @Test
    //    public void testGetResourceMetrics() throws Exception
    //    {
    //        CommandResult commandResult = mock( CommandResult.class );
    //        when( commandResult.hasSucceeded() ).thenReturn( true );
    //        when( commandResult.getStdOut() ).thenReturn( METRIC_JSON );
    //        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
    //        Set<ResourceHostMetric> metrics = Sets.newHashSet();
    //
    //        monitor.addResourceHostMetric( resourceHost, metrics );
    //
    //
    //        ResourceHostMetric metric = metrics.iterator().next();
    //        //        assertEquals( LOCAL_PEER_ID, metric.getPeerId() );
    //        assertEquals( HOST, metric.getHostName() );
    //        assertEquals( METRIC_VALUE, metric.getTotalRam() );
    //    }


    //    @Test
    //    public void testGetResourceHostMetric() throws Exception
    //    {
    //        CommandResult commandResult = mock( CommandResult.class );
    //        when( commandResult.hasSucceeded() ).thenReturn( true );
    //        when( commandResult.getStdOut() ).thenReturn( METRIC_JSON );
    //        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
    //        monitor.getResourceHostMetric( resourceHost );
    //
    //        ResourceHostMetric metric = monitor.getResourceHostMetric( resourceHost );
    //
    //        //        assertEquals( LOCAL_PEER_ID, metric.getPeerId() );
    //        assertEquals( HOST, metric.getHostName() );
    //        assertEquals( METRIC_VALUE, metric.getTotalRam() );
    //    }
    //
    //
    //    @Test
    //    public void testGetResourceMetricsCommandFailed() throws Exception
    //    {
    //        CommandResult commandResult = mock( CommandResult.class );
    //        when( commandResult.hasSucceeded() ).thenReturn( false );
    //        Set<ResourceHostMetric> metrics = Sets.newHashSet();
    //        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
    //
    //
    //        monitor.addResourceHostMetric( resourceHost, metrics );
    //
    //
    //        verify( resourceHost, times( 2 ) ).getHostname();
    //    }
    //
    //
    //    @Test
    //    public void testGetResourceMetricsCommandException() throws Exception
    //    {
    //        Set<ResourceHostMetric> metrics = Sets.newHashSet();
    //        CommandException exception = mock( CommandException.class );
    //        doThrow( exception ).when( resourceHost ).execute( any( RequestBuilder.class ) );
    //
    //        monitor.addResourceHostMetric( resourceHost, metrics );
    //
    //
    //        verify( exception ).printStackTrace( any( PrintStream.class ) );
    //    }


    @Test
    public void testHistoricalMetrics()
    {
        Monitor monitor1 = null;
        try
        {
            monitor1 = new MonitorImpl( peerManager, daoManager, environmentManager, hostRegistry );
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
        HistoricalMetrics historicalMetric = monitor1.getHistoricalMetrics( containerHost, new Date(), new Date() );
        assertNotNull( historicalMetric );
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


    //    @Test
    //    public void testGetOwnerResourceUsage() throws Exception
    //    {
    //        CommandResult commandResult = mock( CommandResult.class );
    //        when( commandResult.hasSucceeded() ).thenReturn( true );
    //        when( commandResult.getStdOut() ).thenReturn( METRIC_JSON );
    //        when( resourceHost.execute( any( RequestBuilder.class ) ) ).thenReturn( commandResult );
    //        when( localPeer.getContainerHostById( HOST_ID ) ).thenReturn( containerHost );
    //        when( containerHost.isLocal() ).thenReturn( true );
    //        when( localPeer.getResourceHostByContainerId( HOST_ID ) ).thenReturn( resourceHost );
    //        when( resourceHost.getContainerHostById( HOST_ID ) ).thenReturn( containerHost );
    //
    //        OwnerResourceUsage ownerResourceUsage = monitor.getOwnerResourceUsage( OWNER_ID );
    //
    //        assertEquals( METRIC_VALUE, ownerResourceUsage.getUsedCpu() );
    //    }
}
