package org.safehaus.subutai.core.identity.impl.entity;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.identity.api.PermissionGroup;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith( MockitoJUnitRunner.class )
public class PermissionPKTest
{
    private PermissionPK permissionPK;

    @Mock
    Object object;

    @Before
    public void setUp() throws Exception
    {
        permissionPK = new PermissionPK();
        permissionPK = new PermissionPK( "permissionKey", PermissionGroup.DEFAULT_PERMISSIONS );
    }


    @Test
    public void testGetPermissionKey() throws Exception
    {
        assertNotNull(permissionPK.getPermissionKey());
    }


    @Test
    public void testGetPermissionGroup() throws Exception
    {
        assertNotNull( permissionPK.getPermissionGroup() );
    }


    @Test
    public void testEquals() throws Exception
    {
        assertFalse( permissionPK.equals( object ) );
        assertTrue( permissionPK.equals( permissionPK ) );
    }


    @Test
    public void testHashCode() throws Exception
    {
        permissionPK.hashCode();
    }
}