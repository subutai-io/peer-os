package io.subutai.core.identity.impl.entity;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.identity.api.PermissionGroup;
import io.subutai.core.identity.impl.entity.PermissionEntity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith( MockitoJUnitRunner.class )
public class PermissionEntityTest
{
    private PermissionEntity permissionEntity;

    @Mock
    Object object;

    @Before
    public void setUp() throws Exception
    {
        permissionEntity = new PermissionEntity(  );
        permissionEntity.setDescription( "description" );
        permissionEntity.setName( "name" );
        permissionEntity.setPermissionGroup( PermissionGroup.DEFAULT_PERMISSIONS );
        permissionEntity = new PermissionEntity( "name", PermissionGroup.DEFAULT_PERMISSIONS, "description" );
    }


    @Test
    public void testGetName() throws Exception
    {
        assertNotNull(permissionEntity.getName());
    }


    @Test
    public void testGetPermissionGroup() throws Exception
    {
        assertNotNull( permissionEntity.getPermissionGroup() );
    }


    @Test
    public void testGetDescription() throws Exception
    {
        assertNotNull( permissionEntity.getDescription() );
    }


    @Test
    public void testEquals() throws Exception
    {
        assertFalse( permissionEntity.equals( object ) );
        assertTrue( permissionEntity.equals( permissionEntity ) );
    }


    @Test
    public void testHashCode() throws Exception
    {
        permissionEntity.hashCode();
    }
}