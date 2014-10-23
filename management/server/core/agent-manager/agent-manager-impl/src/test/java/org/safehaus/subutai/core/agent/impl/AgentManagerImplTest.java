/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.agent.impl;


import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.agent.api.AgentListener;
import org.safehaus.subutai.core.communication.api.CommunicationManager;

import com.jayway.awaitility.Awaitility;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for Agent Manager class
 */
public class AgentManagerImplTest
{

    private final String LXC_HOSTNAME = "parent-lxc-hostname";
    private final String PARENT_HOSTNAME = "parent-lxc-hostname";
    private final String TRANSPORT_ID = "transport-id";
    private AgentManagerImpl agentManager;
    private CommunicationManager communicationManager;


    @Before
    public void setUp()
    {
        communicationManager = mock( CommunicationManager.class );
        agentManager = new AgentManagerImpl( communicationManager );
        agentManager.init();
    }


    @After
    public void tearDown()
    {
        agentManager.destroy();
    }


    @Test
    public void shouldRegisterWithCommManager()
    {

        verify( communicationManager ).addListener( agentManager );
    }


    @Test
    public void shouldUnregisterFromCommManager()
    {

        agentManager.destroy();

        verify( communicationManager ).removeListener( agentManager );
    }


    @Test
    public void shouldReturnRegisteredLxcAgent()
    {
        agentManager.onResponse( MockUtils.getRegistrationRequestFromLxcAgent() );

        assertFalse( agentManager.getLxcAgents().isEmpty() );
        assertTrue( agentManager.getPhysicalAgents().isEmpty() );
    }


    @Test
    public void shouldReturnRegisteredPhysicalAgent()
    {

        agentManager.onResponse( MockUtils.getRegistrationRequestFromPhysicalAgent() );

        assertFalse( agentManager.getPhysicalAgents().isEmpty() );
        assertTrue( agentManager.getLxcAgents().isEmpty() );
    }

    @Test
    public void shouldSetNotifyListenersFlag()
    {

        agentManager.setNotifyAgentListeners( true );

        assertTrue( agentManager.isNotifyAgentListeners() );
    }


    @Test
    public void shouldWaitForAgentRegistration()
    {

        final Response registrationRequest = MockUtils.getRegistrationRequestFromPhysicalAgent();

        Thread t = new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep( 500 );
                    agentManager.onResponse( registrationRequest );
                }
                catch ( InterruptedException e )
                {
                }
            }
        } );
        t.start();


        Awaitility.await().atMost( 2, TimeUnit.SECONDS ).with().pollInterval( 50, TimeUnit.MILLISECONDS ).and()
                  .pollDelay( 100, TimeUnit.MILLISECONDS ).until( new Callable<Boolean>()
        {

            public Boolean call() throws Exception
            {
                return agentManager.waitForRegistration( registrationRequest.getHostname(), 2000 ) != null;
            }
        } );
    }


    @Test
    public void shouldReturnAgentWithMissingHostnameByUUID()
    {

        UUID agentUUID = UUIDUtil.generateTimeBasedUUID();;
        Response response = MockUtils.getRegistrationRequestFromLxcAgent();
        when( response.getUuid() ).thenReturn( agentUUID );
        when( response.getHostname() ).thenReturn( null );

        agentManager.onResponse( response );

        assertNotNull( agentManager.getAgentByHostname( agentUUID.toString() ) );
        assertNotNull( agentManager.getAgentByUUID( agentUUID ) );
    }


    @Test
    public void shouldReturnAgentByHostname()
    {
        Response response = MockUtils.getRegistrationRequestFromPhysicalAgent();
        when( response.getHostname() ).thenReturn( LXC_HOSTNAME );

        agentManager.onResponse( response );

        assertNotNull( agentManager.getAgentByHostname( LXC_HOSTNAME ) );
    }


    @Test
    public void shouldReturnAgentByEnvId()
    {
        UUID id = UUIDUtil.generateTimeBasedUUID();;
        Response response = MockUtils.getRegistrationRequestFromLxcAgentWithEnvironmentId( id );

        agentManager.onResponse( response );

        assertTrue( !agentManager.getAgentsByEnvironmentId( id ).isEmpty() );
        assertTrue( id.equals( agentManager.getAgentsByEnvironmentId( id ).iterator().next().getEnvironmentId() ) );
    }


    @Test
    public void shouldNotReturnAgentsWithMissingUUID()
    {
        Response response = MockUtils.getRegistrationRequestFromPhysicalAgent();
        when( response.getUuid() ).thenReturn( null );

        agentManager.onResponse( response );

        assertTrue( agentManager.getAgents().isEmpty() );
    }


    @Test
    public void shouldReturnAgentByUUID()
    {
        UUID agentUUID = UUIDUtil.generateTimeBasedUUID();
        Response response = MockUtils.getRegistrationRequestFromPhysicalAgent();
        when( response.getUuid() ).thenReturn( agentUUID );

        agentManager.onResponse( response );

        assertNotNull( agentManager.getAgentByUUID( agentUUID ) );
    }


    @Test
    public void shouldReturnAgentsByParentHostname()
    {

        Response response = MockUtils.getRegistrationRequestFromPhysicalAgent();
        when( response.getHostname() ).thenReturn( LXC_HOSTNAME );

        agentManager.onResponse( response );

        assertNotNull( agentManager.getLxcAgentsByParentHostname( PARENT_HOSTNAME ) );
    }


    @Test
    public void shouldSendRegistrationAckToAgent()
    {

        Response response = MockUtils.getRegistrationRequestFromLxcAgent();

        agentManager.onResponse( response );

        verify( communicationManager ).sendRequest( Matchers.any( Request.class ) );
    }


    @Test
    public void shouldDeleteAgentOnDisconnect()
    {

        Response response = MockUtils.getRegistrationRequestFromLxcAgent();
        when( response.getTransportId() ).thenReturn( TRANSPORT_ID );
        //registering agent
        agentManager.onResponse( response );

        assertFalse( agentManager.getAgents().isEmpty() );

        when( response.getType() ).thenReturn( ResponseType.AGENT_DISCONNECT );
        //disconnecting agent
        agentManager.onResponse( response );

        assertTrue( agentManager.getAgents().isEmpty() );
    }


    @Test
    public void shouldAddAgentListener()
    {
        agentManager.addListener( mock( AgentListener.class ) );

        assertFalse( agentManager.getListeners().isEmpty() );
        assertFalse( agentManager.getListenersQueue().isEmpty() );
    }


    @Test
    public void shouldRemoveAgentListener()
    {

        AgentListener listener = mock( AgentListener.class );

        agentManager.addListener( listener );
        agentManager.removeListener( listener );

        assertTrue( agentManager.getListeners().isEmpty() );
        assertTrue( agentManager.getListenersQueue().isEmpty() );
    }
}
