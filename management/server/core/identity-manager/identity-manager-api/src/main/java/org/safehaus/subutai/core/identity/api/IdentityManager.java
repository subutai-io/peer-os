package org.safehaus.subutai.core.identity.api;


import java.io.Serializable;
import java.util.List;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;


public interface IdentityManager
{
    public SecurityManager getSecurityManager();

    public User getUser( String username );

    public Subject login( AuthenticationToken token );

    public Subject getSubject( Serializable sessionId );

    public void logout( Serializable sessionId );

    public List<User> getAllUsers();

    public boolean addUser( String username, String fullname, String password, String email );

    public String getUserKey( String username );

    public User createMockUser( String username, String fullName, String password, String email );

    public boolean updateUser( User user );

    public User getUser( Long id );

    public boolean deleteUser( User user );

    //Permissions
    public List<Permission> getAllPermissions();

    public Permission createMockPermission( String permissionName, PermissionGroup permissionGroup,
                                            String description );

    public boolean updatePermission( Permission permission );

    public Permission getPermission( String name, PermissionGroup permissionGroup );

    public boolean deletePermission( Permission permission );

    //Roles
    public List<Role> getAllRoles();

    public Role createMockRole( String permissionName, PermissionGroup permissionGroup, String description );

    public boolean updateRole( Role role );

    public Permission getRole( String name, PermissionGroup permissionGroup );

    public boolean deleteRole( Role role );
}

