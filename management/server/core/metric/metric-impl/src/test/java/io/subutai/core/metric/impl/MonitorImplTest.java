package io.subutai.core.metric.impl;


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

import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Environment;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.RemotePeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.metric.api.MonitoringSettings;
import io.subutai.core.peer.api.PeerManager;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
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
    private static final String HOST_ID = UUID.randomUUID().toString();
    private static final Long USER_ID = 123L;
    private static final int PID = 123;
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
    AlertEvent alert;
    @Mock
    Set<AlertListener> alertListeners;
    @Mock
    AlertListener alertListener;
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


        public void setMonitorDao( MonitorDao monitorDao )
        {
            this.monitorDao = monitorDao;
        }


        public void setAlertListeners( Set<AlertListener> alertHandlers )
        {
            this.alertListeners = alertHandlers;
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


        alert = mock( AlertEvent.class );
        when( environmentId.getId() ).thenReturn( ENVIRONMENT_ID );
        when( alertListener.getId() ).thenReturn( SUBSCRIBER_ID );
        monitor.setAlertListeners( Sets.newHashSet( alertListener ) );
        when( monitorDao.findHandlersByEnvironment( ENVIRONMENT_ID ) ).thenReturn( Sets.newHashSet( SUBSCRIBER_ID ) );
        when( environment.getId() ).thenReturn( ENVIRONMENT_ID );
        when( environment.getUserId() ).thenReturn( USER_ID );
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

        monitor.notifyAlertListeners();

        verify( notificationService ).execute( alertNotifierArgumentCaptor.capture() );
        assertEquals( alertListener, alertNotifierArgumentCaptor.getValue().listener );
        assertEquals( alert, alertNotifierArgumentCaptor.getValue().alert );
    }


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
}
