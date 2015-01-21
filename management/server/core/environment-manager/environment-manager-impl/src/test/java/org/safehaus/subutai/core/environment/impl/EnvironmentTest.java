package org.safehaus.subutai.core.environment.impl;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.impl.entity.EnvironmentImpl;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.peer.api.LocalPeer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@Ignore
public class EnvironmentTest
{

    private static final String NAME = "name";
    Environment environment;

    @Mock
    LocalPeer localPeer;


    @Before
    public void setUp() throws Exception
    {

        this.environment = new EnvironmentImpl( NAME );
    }


    @Test
    public void testSetGetContainers() throws Exception
    {

        Set<ContainerHost> set = new HashSet<>();
        //        environment.setContainers( set );
        assertEquals( set, environment.getContainerHosts() );
    }


    @Test
    public void testGetName() throws Exception
    {
        assertEquals( NAME, environment.getName() );
    }


    @Test
    public void testNodesNotNull() throws Exception
    {
        Set<ContainerHost> environmentContainers = environment.getContainerHosts();
        assertNotNull( environmentContainers );
    }


    @Test
    public void testUUIDNotNull() throws Exception
    {
        UUID uuid = environment.getId();
        assertNotNull( uuid );
    }


    //    @Test
    //    public void testInvoke() throws Exception
    //    {
    //        PeerCommandMessage peerCommandMessage = mock( PeerCommandMessage.class );
    //        environment.invoke( peerCommandMessage );
    //    }
}
