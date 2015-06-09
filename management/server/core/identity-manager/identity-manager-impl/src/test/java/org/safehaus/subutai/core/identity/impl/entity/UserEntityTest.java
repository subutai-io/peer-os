package org.safehaus.subutai.core.identity.impl.entity;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.identity.api.Role;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class UserEntityTest
{
    private UserEntity userEntity;

    @Mock
    Role role;


    @Before
    public void setUp() throws Exception
    {
        userEntity = new UserEntity();
        userEntity.setEmail( "email" );
        userEntity.setFullname( "fullName" );
        userEntity.setId( ( long ) 5 );
        userEntity.setKey( "key" );
        userEntity.setPassword( "password" );
        userEntity.setPermissions( "permissions" );
        userEntity.setSalt( "salt" );
        userEntity.addRole( role );
        userEntity.setUsername( "userName" );
    }


    @Test
    public void testGetId() throws Exception
    {
        assertNotNull( userEntity.getId() );
    }


    @Test
    public void testGetPassword() throws Exception
    {
        assertNotNull( userEntity.getPassword() );
    }


    @Test
    public void testGetPermissions() throws Exception
    {
        assertNotNull( userEntity.getPermissions() );
    }


    @Test
    public void testGetUsername() throws Exception
    {
        assertNotNull( userEntity.getUsername() );
    }


    @Test
    public void testGetSalt() throws Exception
    {
        assertNotNull( userEntity.getSalt() );
    }


    @Test
    public void testRemoveRole() throws Exception
    {
        userEntity.removeRole( role );
    }


    @Test
    public void testRemoveAllRoles() throws Exception
    {
        userEntity.removeAllRoles();
    }


    @Test
    public void testGetFullname() throws Exception
    {
        assertNotNull( userEntity.getFullname() );
    }


    @Test
    public void testGetEmail() throws Exception
    {
        assertNotNull( userEntity.getEmail() );
    }


    @Test
    public void testGetKey() throws Exception
    {
        assertNotNull( userEntity.getKey() );
    }


    @Test
    public void testGetRoles() throws Exception
    {
        assertNotNull( userEntity.getRoles() );
    }


    @Test
    public void testIsAdmin() throws Exception
    {
        when( role.getName() ).thenReturn( "admin" );
        assertTrue( userEntity.isAdmin() );
    }


    @Test
    public void testIsAdmin2() throws Exception
    {
        when( role.getName() ).thenReturn( "user" );
        assertFalse( userEntity.isAdmin() );
    }


    @Test(expected = IllegalArgumentException.class)
    public void testAddRoleException()
    {
        userEntity.addRole( null );
    }
}