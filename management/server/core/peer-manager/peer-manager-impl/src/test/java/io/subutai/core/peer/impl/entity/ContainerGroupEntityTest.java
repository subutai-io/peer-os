package io.subutai.core.peer.impl.entity;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.core.peer.impl.entity.ContainerGroupEntity;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;


@RunWith( MockitoJUnitRunner.class )
public class ContainerGroupEntityTest
{
    private static final String ENVIRONMENT_ID = UUID.randomUUID().toString();
    private static final String OWNER_ID = UUID.randomUUID().toString();
    private static final String INITIATOR_PEER_ID = UUID.randomUUID().toString();
    private static final String CONTAINER_ID = UUID.randomUUID().toString();

    ContainerGroupEntity containerGroupEntity;


    @Before
    public void setUp() throws Exception
    {
        containerGroupEntity = new ContainerGroupEntity( ENVIRONMENT_ID, INITIATOR_PEER_ID, OWNER_ID );
    }


    @Test
    public void testGetEnvironmentId() throws Exception
    {
        assertEquals( ENVIRONMENT_ID, containerGroupEntity.getEnvironmentId() );
    }


    @Test
    public void testSetEnvironmentId() throws Exception
    {
        String ENV_ID = UUID.randomUUID().toString();
        containerGroupEntity.setEnvironmentId( ENV_ID.toString() );

        assertEquals( ENV_ID, containerGroupEntity.getEnvironmentId() );
    }


    @Test
    public void testGetInitiatorPeerId() throws Exception
    {
        assertEquals( INITIATOR_PEER_ID, containerGroupEntity.getInitiatorPeerId() );
    }


    @Test
    public void testSetInitiatorPeerId() throws Exception
    {
        String initiatorPeerId = UUID.randomUUID().toString();

        containerGroupEntity.setInitiatorPeerId( initiatorPeerId.toString() );

        assertEquals( initiatorPeerId, containerGroupEntity.getInitiatorPeerId() );
    }


    @Test
    public void testGetOwnerId() throws Exception
    {
        assertEquals( OWNER_ID, containerGroupEntity.getOwnerId() );
    }


    @Test
    public void testSetOwnerId() throws Exception
    {
        String ownerId = UUID.randomUUID().toString();

        containerGroupEntity.setOwnerId( ownerId.toString() );

        assertEquals( ownerId, containerGroupEntity.getOwnerId() );
    }


    @Test
    public void testGetNSetContainerIds() throws Exception
    {
        containerGroupEntity.setContainerIds( Sets.newHashSet( CONTAINER_ID ) );

        assertTrue( containerGroupEntity.getContainerIds().contains( CONTAINER_ID ) );
    }
}
