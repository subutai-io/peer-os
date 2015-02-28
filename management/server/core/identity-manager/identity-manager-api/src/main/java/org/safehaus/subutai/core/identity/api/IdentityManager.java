package org.safehaus.subutai.core.identity.api;


import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;


/**
 * IdentityManager implements RBAC system control
 */
public interface IdentityManager
{
    public static final String[] ROLES = { "admin", "manager", "viewer" };

    /**
     * Returns shiro SecurityManager
     *
     * @return {@link org.apache.shiro.mgt.SecurityManager}
     *
     * @see org.apache.shiro.mgt.SecurityManager
     */
    public SecurityManager getSecurityManager();

    /**
     * Return user entity
     *
     * @return user entity
     */
    public User getUser();

    //    public User getUser( String username );


    /**
     * Logs in user with token passed
     *
     * @param username - username
     * @param password - password
     *
     * @return - {@code Subject} represents state and security operations for a <em>single</em> application user.
     */
    public Serializable login( String username, String password );

    //    public Subject login( AuthenticationToken token );


    //    boolean isAuthenticated( Serializable sessionId );

    /**
     * Get {@code Subject} for target session
     *
     * @param sessionId - session id
     *
     * @return - {@code Subject} represents state and security operations for a <em>single</em> application user.
     */
    public Subject getSubject( Serializable sessionId );


    /**
     * Logout user by sessions id
     *
     * @param sessionId - logout user by session id
     */
    public void logout( Serializable sessionId );


    /**
     * Get all registered users
     *
     * @return - {@code List} of {@code User}
     */
    public List<User> getAllUsers();


    /**
     * Save new user in system
     *
     * @param username - username
     * @param fullname - surname/name
     * @param password - password
     * @param email - email
     *
     * @return - user registration operation result
     */
    public boolean addUser( String username, String fullname, String password, String email );


    /**
     * Returns user key by username
     *
     * @param username - username
     *
     * @return - user key
     */
    public String getUserKey( String username );


    /**
     * Creates sample user entity. This generates salt from username and hashed password
     *
     * @param username - username
     * @param fullName - fullName
     * @param password - password
     * @param email - email
     *
     * @return - {@code User}
     */
    public User createMockUser( String username, String fullName, String password, String email );


    /**
     * Update user parameters
     *
     * @param user - target user to update some parameters
     *
     * @return - update result
     */
    public boolean updateUser( User user );


    /**
     * Get user by id
     *
     * @param id - user id
     *
     * @return - {@code User}
     */
    public User getUser( Long id );


    /**
     * Remove user from system
     *
     * @param user - {@code User} entity
     *
     * @return - remove operation result
     */
    public boolean deleteUser( User user );


    //Permissions

    /**
     * Get all Permissions
     *
     * @return - {@code Collection} of {@code Permission}
     */
    public List<Permission> getAllPermissions();


    /**
     * Create sample {@code Permission} entity
     *
     * @param permissionName - permission name
     * @param permissionGroup - permission group
     * @param description - description
     *
     * @return - {@code Permission} entity
     */
    public Permission createMockPermission( String permissionName, PermissionGroup permissionGroup,
                                            String description );


    /**
     * Update existing permission
     *
     * @param permission - {@code Permission} entity to update
     *
     * @return - update operation result
     */
    public boolean updatePermission( Permission permission );


    /**
     * Returns {@code Permission} for {@link Permission#getName()} and {@link Permission#getPermissionGroup()}
     * parameters
     *
     * @param name - permission name
     * @param permissionGroup - permission group
     *
     * @return - {@code Permission} entity
     */
    public Permission getPermission( String name, PermissionGroup permissionGroup );


    /**
     * Erases {@code Permission} entity from database
     *
     * @param permission - target {@code Permission} to erase
     *
     * @return - result for delete operation
     */
    public boolean deletePermission( Permission permission );


    //Roles

    /**
     * Get list of {@code Role} existing in database
     *
     * @return - {@code Collection} of {@code Role} entities
     */
    public List<Role> getAllRoles();


    /**
     * Create sample {@code Role} for persisting purposes
     *
     * @param permissionName - permission name
     * @param permissionGroup - permission group {@link PermissionGroup}
     * @param description - description
     *
     * @return - {@code Role} entity
     */
    public Role createMockRole( String permissionName, PermissionGroup permissionGroup, String description );


    /**
     * Update existing role or save a new one
     *
     * @param role - {@code Role} entity
     *
     * @return - result for {@code Role} update operation
     */
    public boolean updateRole( Role role );


    /**
     * Return {@code Role} by name
     *
     * @param name - role name
     *
     * @return - {@code Role} entity
     */
    public Role getRole( String name );


    /**
     * Erase existing {@code Role} from database
     *
     * @param role - {@code Role}
     *
     * @return - result for erase operation
     */
    public boolean deleteRole( Role role );

    public boolean isAuthenticated();

    public Set<String> getRoles( Serializable shiroSessionId );
}

