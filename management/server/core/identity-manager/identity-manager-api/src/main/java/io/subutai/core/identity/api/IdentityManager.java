package io.subutai.core.identity.api;


import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.security.auth.Subject;

import io.subutai.core.identity.api.dao.IdentityDataService;
import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;


/**
 *
 */
public interface IdentityManager
{
    /* *************************************************
     *
     */
    public IdentityDataService getIdentityDataService();


    /* *************************************************
     */
    void logout();

    /* *************************************************
     */
    User authenticateUser( String userName, String password );

    /* *************************************************
         */
    List<User> getAllUsers();

    /* *************************************************
     */
    void assignUserRole( long userId, Role role );

    /* *************************************************
                 *
                 */
    User createUser( String userName, String password, String fullName, String email );


    /* *************************************************
     */
    void removeUser( long userId );

    /* *************************************************
         *
         */
    Role createRole( String roleName, short roleType );


    /* *************************************************
     *
     */
    Subject login( String userName, String password);

    /* *************************************************
         */
    void removeRole( long roleId );

    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Delete" )
    Permission createPermission( int objectId, int scope, boolean read, boolean write, boolean update, boolean delete );

    /* *************************************************
     */
    @RolesAllowed( "Identity-Management|A|Write" )
    void assignRolePermission( long roleId, Permission permission );

    /* *************************************************
                 */
    void removePermission( long permissionId );
}
