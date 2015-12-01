package io.subutai.core.registration.impl;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.registration.api.service.RequestedHost;
import io.subutai.core.registration.impl.dao.ContainerTokenDataService;
import io.subutai.core.registration.impl.dao.RequestDataService;
import io.subutai.core.registration.impl.entity.RequestedHostImpl;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by talas on 8/26/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class RegistrationManagerImplTest
{

    Collection<RequestedHostImpl> requestedHosts = new ArrayList<>();

    @Mock
    RequestDataService requestDataService;

    @Mock
    ContainerTokenDataService containerTokenDataService;

    @Mock
    DaoManager daoManager;

    @Mock
    SecurityManager securityManager;

    RegistrationManagerImpl registrationManager;

    UUID uuid = UUID.randomUUID();


    @Before
    public void setUp() throws Exception
    {
        registrationManager = new RegistrationManagerImpl( securityManager, daoManager, "" );
        registrationManager.setRequestDataService( requestDataService );

        RequestedHostImpl host1 = mock( RequestedHostImpl.class );
        RequestedHostImpl host2 = mock( RequestedHostImpl.class );

        EncryptionTool encryptionTool = mock( EncryptionTool.class );
        KeyManager keyManager = mock( KeyManager.class );
        InputStream secretKey = mock( InputStream.class );

        requestedHosts.add( host1 );
        requestedHosts.add( host2 );
        when( requestDataService.getAll() ).thenReturn( requestedHosts );
        when( requestDataService.find( uuid.toString() ) ).thenReturn( host1 );
        when( host1.getRestHook() ).thenReturn( "this is url" );
        when( host1.getId() ).thenReturn( "This is id" );
        when( securityManager.getEncryptionTool() ).thenReturn( encryptionTool );
        when( securityManager.getKeyManager() ).thenReturn( keyManager );
    }


    @Test
    public void testInit() throws Exception
    {
        //        registrationManager.init();
    }


    @Test
    public void testGetRequestDataService() throws Exception
    {
        assertEquals( registrationManager.getRequestDataService(), requestDataService );
    }


    @Test
    public void testSetRequestDataService() throws Exception
    {
        RequestDataService requestDataService1 = mock( RequestDataService.class );
        registrationManager.setRequestDataService( requestDataService1 );
        assertNotEquals( requestDataService, registrationManager.getRequestDataService() );
    }


    @Test
    public void testGetRequests() throws Exception
    {
        assertArrayEquals( registrationManager.getRequests().toArray(), requestedHosts.toArray() );
    }


    @Test
    public void testGetRequest() throws Exception
    {
        assertEquals( requestedHosts.iterator().next(), registrationManager.getRequest( uuid.toString() ) );
    }


    @Test
    public void testQueueRequest() throws Exception
    {
        RequestedHost temp = mock( RequestedHost.class );
        when( temp.getId() ).thenReturn( uuid.toString() );
        registrationManager.queueRequest( temp );
        assertEquals( requestedHosts.size(), registrationManager.getRequests().size() );
    }


    @Test
    public void testRejectRequest() throws Exception
    {
        //        registrationManager.rejectRequest( uuid );
    }


    @Test
    public void testApproveRequest() throws Exception
    {

    }


    @Test
    public void testRemoveRequest() throws Exception
    {
        registrationManager.removeRequest( uuid.toString() );
    }


}