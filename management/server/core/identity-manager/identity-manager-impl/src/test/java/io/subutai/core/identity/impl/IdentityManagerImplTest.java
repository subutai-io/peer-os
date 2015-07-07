package io.subutai.core.identity.impl;


import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.CliCommand;
import io.subutai.core.identity.api.Permission;
import io.subutai.core.identity.api.PermissionGroup;
import io.subutai.core.identity.api.Role;
import io.subutai.core.identity.api.User;

import org.apache.felix.service.command.CommandSession;


@RunWith( MockitoJUnitRunner.class )
public class IdentityManagerImplTest
{
    private IdentityManagerImpl identityManager;

    @Mock
    DaoManager daoManager;
    @Mock
    DataSource dataSource;
    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    CommandSession commandSession;
    @Mock
    CharSequence charSequence;
    @Mock
    Role role;
    @Mock
    User user;
    @Mock
    Permission permission;
    @Mock
    CliCommand cliCommand;

    @Before
    public void setUp() throws Exception
    {
        identityManager = new IdentityManagerImpl( daoManager, dataSource );
    }

    @Test
    public void testCreateMockUser() throws Exception
    {
        identityManager.createMockUser( "userName", "fullName", "password", "email" );
    }


    @Test
    public void testUpdateUser() throws Exception
    {
        identityManager.updateUser( user );
    }


    @Test
    public void testDeleteUser() throws Exception
    {
        identityManager.deleteUser( user );
    }


    @Test
    public void testCreateCliCommand() throws Exception
    {
        identityManager.createCliCommand( "scope","name" );
    }


    @Test
    public void testUpdateCliCommand() throws Exception
    {
        identityManager.updateCliCommand( cliCommand );
    }


    @Test
    public void testCreatePermission() throws Exception
    {
        identityManager.createPermission( "permission", PermissionGroup.PEER_PERMISSIONS,"description" );
    }


    @Test
    public void testUpdatePermission() throws Exception
    {
        identityManager.updatePermission( permission );
    }


    @Test
    public void testDeletePermission() throws Exception
    {
        identityManager.deletePermission( permission );
    }


    @Test
    public void testCreateRole() throws Exception
    {
        identityManager.createRole( "role" );
    }


    @Test
    public void testUpdateRole() throws Exception
    {
        identityManager.updateRole( role );
    }


    @Test
    public void testBeforeExecute() throws Exception
    {
        identityManager.beforeExecute( commandSession, charSequence );
    }


    @Test
    public void testAfterExecute() throws Exception
    {
        identityManager.afterExecute( commandSession, charSequence, new Exception(  ) );
    }


    @Test
    public void testAfterExecute1() throws Exception
    {
        identityManager.afterExecute( commandSession, charSequence, new Object() );
    }
}