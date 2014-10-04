package org.safehaus.subutai.core.environment.api;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.ContainerState;
import org.safehaus.subutai.core.environment.api.helper.Environment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;


/**
 * Created by bahadyr on 10/2/14.
 */
public class EnvironmentContainerTest
{

    private static final String HOSTNAME = "hostname";
    private static final String NAME = "name";
    private static final UUID AGENT_UUID = UUID.randomUUID();
    private static final String DESCRIPTION = "description";
    private static final UUID PEER_ID = UUID.randomUUID();
    EnvironmentContainer container;


    @Before
    public void setUp() throws Exception
    {
        this.container = new EnvironmentContainer();
    }


    @Test
    public void testHostnameSetAndGet() throws Exception
    {
        container.setHostname( HOSTNAME );
        assertEquals( HOSTNAME, container.getHostname() );
    }


    @Test
    public void testEnvironmentSetGet() throws Exception
    {
        Environment environment = mock( Environment.class );
        container.setEnvironment( environment );
        assertEquals( environment, container.getEnvironment() );
    }


    @Test
    public void testNameGetSet() throws Exception
    {
        container.setName( NAME );
        assertEquals( NAME, container.getName() );
    }


    @Test
    public void testAgentUUIDGetSet() throws Exception
    {
        container.setAgentId( AGENT_UUID );
        assertEquals( AGENT_UUID, container.getAgentId() );
    }


    @Test
    public void testDescriptionSetGet() throws Exception
    {
        container.setDescription( DESCRIPTION );
        assertEquals( DESCRIPTION, container.getDescription() );
    }


    @Test
    public void testPeerIDSetGet() throws Exception
    {
        container.setPeerId( PEER_ID );
        assertEquals( PEER_ID, container.getPeerId() );
    }


    @Test
    public void testStateSetGet() throws Exception
    {
        container.setState( ContainerState.STARTED );
        assertEquals( ContainerState.STARTED, container.getState() );
    }


    @Test
    public void testIsContainerConnected() throws Exception
    {
        Environment environment = mock( Environment.class );
        container.setEnvironment( environment );
        assertFalse( container.isConnected() );
    }


    @Test
    public void testContainerStart() throws Exception
    {
        Environment environment = mock( Environment.class );
        container.setEnvironment( environment );
        assertFalse( container.start() );
    }


    @Test
    public void testContainerStop() throws Exception
    {
        Environment environment = mock( Environment.class );
        container.setEnvironment( environment );
        assertFalse( container.stop() );
    }
}
