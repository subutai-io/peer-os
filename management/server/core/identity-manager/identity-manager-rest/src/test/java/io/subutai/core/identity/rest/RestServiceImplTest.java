package io.subutai.core.identity.rest;


import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.identity.api.CliCommand;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.Permission;
import io.subutai.core.identity.api.PermissionGroup;
import io.subutai.core.identity.api.PortalModuleScope;
import io.subutai.core.identity.api.RestEndpointScope;
import io.subutai.core.identity.api.Role;
import io.subutai.core.identity.api.User;
import io.subutai.core.identity.rest.RestServiceImpl;

import org.apache.shiro.subject.Subject;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class RestServiceImplTest
{
    private RestServiceImpl restService;
    private IdentityManager identityManager;


    @Before
    public void setUp() throws Exception
    {
        identityManager = new IdentityManager()
        {
            @Override
            public org.apache.shiro.mgt.SecurityManager getSecurityManager()
            {
                return null;
            }


            @Override
            public User getUser()
            {
                return null;
            }


            @Override
            public User getUser( final String username )
            {
                return null;
            }


            @Override
            public Serializable login( final String username, final String password )
            {
                return null;
            }


            @Override
            public Serializable loginWithToken( final String username )
            {
                return null;
            }


            @Override
            public short checkRestPermissions( final User user, final String restURL )
            {
                return 0;
            }


            @Override
            public Subject getSubject( final Serializable sessionId )
            {
                return null;
            }


            @Override
            public void touch( final Serializable sessionId )
            {

            }


            @Override
            public void logout( final Serializable sessionId )
            {

            }


            @Override
            public List<User> getAllUsers()
            {
                return null;
            }


            @Override
            public boolean addUser( final String username, final String fullname, final String password,
                                    final String email )
            {
                return false;
            }


            @Override
            public String getUserKey( final String username )
            {
                return "user";
            }


            @Override
            public User createMockUser( final String username, final String fullName, final String password,
                                        final String email )
            {
                return null;
            }


            @Override
            public boolean updateUser( final User user )
            {
                return false;
            }


            @Override
            public User getUser( final Long id )
            {
                return null;
            }


            @Override
            public boolean deleteUser( final User user )
            {
                return false;
            }


            @Override
            public List<CliCommand> getAllCliCommands()
            {
                return null;
            }


            @Override
            public CliCommand createCliCommand( final String scope, final String name )
            {
                return null;
            }


            @Override
            public boolean updateCliCommand( final CliCommand cliCommand )
            {
                return false;
            }


            @Override
            public Set<RestEndpointScope> getAllRestEndpoints()
            {
                return null;
            }


            @Override
            public Set<PortalModuleScope> getAllPortalModules()
            {
                return null;
            }


            @Override
            public boolean updateUserPortalModule( final String moduleKey, final String moduleName )
            {
                return false;
            }


            @Override
            public List<Permission> getAllPermissions()
            {
                return null;
            }


            @Override
            public Permission createPermission( final String permissionName, final PermissionGroup permissionGroup,
                                                final String description )
            {
                return null;
            }


            @Override
            public boolean updatePermission( final Permission permission )
            {
                return false;
            }


            @Override
            public Permission getPermission( final String name, final PermissionGroup permissionGroup )
            {
                return null;
            }


            @Override
            public boolean deletePermission( final Permission permission )
            {
                return false;
            }


            @Override
            public List<Role> getAllRoles()
            {
                return null;
            }


            @Override
            public Role createRole( final String roleName )
            {
                return null;
            }


            @Override
            public boolean updateRole( final Role role )
            {
                return false;
            }


            @Override
            public Role getRole( final String name )
            {
                return null;
            }


            @Override
            public void deleteRole( final Role role )
            {

            }


            @Override
            public boolean isAuthenticated()
            {
                return false;
            }


            @Override
            public Set<String> getRoles( final Serializable shiroSessionId )
            {
                return null;
            }
        };
        restService = new RestServiceImpl( identityManager );
    }


    @Test
    public void testGetKey() throws Exception
    {
        assertNotNull( restService.getKey( "userName" ) );
    }
}