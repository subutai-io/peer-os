package io.subutai.core.registration.impl;


import java.util.Collection;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.subutai.common.cache.ExpiringCache;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.common.settings.Common;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.registration.api.ResourceHostRegistrationStatus;
import io.subutai.core.registration.api.exception.HostRegistrationException;
import io.subutai.core.registration.api.service.RequestedHost;
import io.subutai.core.registration.impl.dao.RequestDataService;
import io.subutai.core.registration.impl.entity.RequestedHostImpl;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.security.api.crypto.KeyStoreManager;
import io.subutai.core.security.api.jetty.HttpContextManager;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class HostRegistrationManagerImplTest
{

    private Collection<RequestedHostImpl> requestedHosts = Lists.newArrayList();

    @Mock
    RequestDataService requestDataService;

    @Mock
    DaoManager daoManager;

    @Mock
    SecurityManager securityManager;
    @Mock
    ServiceLocator serviceLocator;
    @Mock
    LocalPeer localPeer;

    HostRegistrationManagerImpl registrationManager;
    @Mock
    ExpiringCache<String, Boolean> tokenCache;
    @Mock
    KeyManager keyManager;
    @Mock
    EncryptionTool encryptionTool;
    @Mock
    KeyStoreManager keyStoreManager;
    @Mock
    HttpContextManager httpContextManager;
    @Mock
    EnvironmentManager environmentManager;

    private static final String REQUEST_ID = UUID.randomUUID().toString();


    @Before
    public void setUp() throws Exception
    {
        registrationManager = spy( new HostRegistrationManagerImpl( securityManager, daoManager ) );
        registrationManager.requestDataService = requestDataService;
        registrationManager.serviceLocator = serviceLocator;
        registrationManager.tokenCache = tokenCache;

        RequestedHostImpl host1 = mock( RequestedHostImpl.class );
        RequestedHostImpl host2 = mock( RequestedHostImpl.class );


        requestedHosts.add( host1 );
        requestedHosts.add( host2 );
        doReturn( "host1" ).when( host1 ).getHostname();
        doReturn( ResourceHostRegistrationStatus.APPROVED ).when( host1 ).getStatus();
        doReturn( "host2" ).when( host2 ).getHostname();
        when( requestDataService.getAll() ).thenReturn( requestedHosts );
        when( requestDataService.find( REQUEST_ID ) ).thenReturn( host1 );
        when( host1.getId() ).thenReturn( "ID" );
        when( securityManager.getEncryptionTool() ).thenReturn( encryptionTool );
        when( securityManager.getKeyManager() ).thenReturn( keyManager );
        doReturn( localPeer ).when( serviceLocator ).getService( LocalPeer.class );
        doReturn( environmentManager ).when( serviceLocator ).getService( EnvironmentManager.class );
        doReturn( keyStoreManager ).when( securityManager ).getKeyStoreManager();
        doReturn( httpContextManager ).when( securityManager ).getHttpContextManager();
    }


    @Test
    public void testInit() throws Exception
    {
        registrationManager.init();

        assertNotNull( registrationManager.requestDataService );
    }


    @Test
    public void testGetRequestDataService() throws Exception
    {
        assertEquals( registrationManager.getRequestDataService(), requestDataService );
    }


    @Test
    public void testGetRequests() throws Exception
    {
        assertArrayEquals( registrationManager.getRequests().toArray(), requestedHosts.toArray() );
    }


    @Test
    public void testGetRequest() throws Exception
    {
        assertEquals( requestedHosts.iterator().next(), registrationManager.getRequest( REQUEST_ID ) );
    }


    @Test
    public void testQueueRequest() throws Exception
    {
        RequestedHost requestedHost = mock( RequestedHost.class );
        when( requestedHost.getId() ).thenReturn( REQUEST_ID );
        doReturn( TestHelper.PGP_PUBLIC_KEY ).when( requestedHost ).getPublicKey();
        doReturn( "hostname" ).when( requestedHost ).getHostname();

        registrationManager.queueRequest( requestedHost );

        verify( requestDataService ).update( isA( RequestedHostImpl.class ) );

        //-----

        doReturn( null ).when( requestDataService ).find( REQUEST_ID );

        registrationManager.queueRequest( requestedHost );

        verify( registrationManager ).checkManagement( isA( RequestedHost.class ) );
    }


    @Test( expected = HostRegistrationException.class )
    public void testRemoveRequest() throws Exception
    {
        registrationManager.removeRequest( REQUEST_ID );

        verify( localPeer ).removeResourceHost( anyString() );

        //-----

        doThrow( new HostNotFoundException( "" ) ).when( localPeer ).removeResourceHost( anyString() );

        registrationManager.removeRequest( REQUEST_ID );

        //-----

        doThrow( new RuntimeException() ).when( localPeer ).removeResourceHost( anyString() );

        registrationManager.removeRequest( REQUEST_ID );
    }


    @Test
    public void testGenerateContainerTTLToken() throws Exception
    {
        registrationManager.generateContainerToken( 123L );

        verify( tokenCache ).put( anyString(), anyBoolean(), eq( 123L ) );
    }


    @Test
    public void testVerifyToken() throws Exception
    {
        String containerToken = UUID.randomUUID().toString();
        doReturn( containerToken ).when( registrationManager ).generateContainerToken( anyLong() );
        doReturn( true ).when( tokenCache ).keyExists( containerToken );

        registrationManager.verifyTokenAndRegisterKey( containerToken, "id", TestHelper.PGP_PUBLIC_KEY );

        verify( keyManager )
                .savePublicKeyRing( "id", SecurityKeyType.CONTAINER_HOST_KEY.getId(), TestHelper.PGP_PUBLIC_KEY );

        //-----

        doThrow( new RuntimeException() ).when( keyManager )
                                         .savePublicKeyRing( "id", SecurityKeyType.CONTAINER_HOST_KEY.getId(),
                                                 TestHelper.PGP_PUBLIC_KEY );

        try
        {
            registrationManager.verifyTokenAndRegisterKey( containerToken, "id", TestHelper.PGP_PUBLIC_KEY );

            fail( "Expected HostRegistrationException" );
        }
        catch ( HostRegistrationException e )
        {
        }


        //-----

        doReturn( false ).when( tokenCache ).keyExists( containerToken );

        boolean result =
                registrationManager.verifyTokenAndRegisterKey( containerToken, "id", TestHelper.PGP_PUBLIC_KEY );

        assertFalse( result );
    }


    @Test
    public void testImportHostSslCert() throws Exception
    {
        registrationManager.importHostSslCert( "id", "cert" );

        verify( keyStoreManager ).importCertAsTrusted( Common.DEFAULT_PUBLIC_SECURE_PORT, "id", "cert" );
    }


    @Test
    public void testImportHostPublicKey() throws Exception
    {
        registrationManager.importHostPublicKey( "id", "key", false );

        verify( keyManager ).savePublicKeyRing( "id", SecurityKeyType.CONTAINER_HOST_KEY.getId(), "key" );
    }


    @Test
    public void testCheckManagement() throws Exception
    {
        RequestedHost requestedHost = mock( RequestedHost.class );
        doReturn( ResourceHostRegistrationStatus.REQUESTED ).when( requestedHost ).getStatus();
        doReturn( true ).when( registrationManager ).containsManagementContainer( anySet() );

        registrationManager.checkManagement( requestedHost );

        verify( registrationManager, never() ).approveRequest( anyString() );

        //-----

        doReturn( Sets.newHashSet() ).when( requestDataService ).getAll();

        registrationManager.checkManagement( requestedHost );

        verify( registrationManager ).approveRequest( anyString() );
    }
}