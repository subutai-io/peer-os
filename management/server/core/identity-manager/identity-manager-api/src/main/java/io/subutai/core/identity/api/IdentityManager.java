package io.subutai.core.identity.api;


import java.util.Date;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.objects.PermissionOperation;
import io.subutai.common.security.objects.PermissionScope;
import io.subutai.core.identity.api.dao.IdentityDataService;
import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;


/**
 *
 */
public interface IdentityManager
{
    /* *************************************************
     */
    List<Permission> getAllPermissions();

    /* *************************************************
     *
     */
    public IdentityDataService getIdentityDataService();


    /* *************************************************
     */
    Session startSession( User user );


    /* *************************************************
     */
    void logout();


    /* *************************************************
     */
    String getUserToken( String userName, String password );

    /* *************************************************
     */
    User authenticateByToken( String token );


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
     */
    User getUser( long userId );

    /* *************************************************
     */
    @PermitAll
    User getActiveUser();

    /* *************************************************
         */
    User getLoggedUser();


    /* *************************************************
     */
    User createTempUser( String userName, String password, String fullName, String email, int type );


    /* *************************************************
     *
     */
    User createUser( String userName, String password, String fullName, String email, int type );


    /* *************************************************
     */
    void removeUserRole( long userId, Role role );


    /* *************************************************
     */
    void removeUserAllRoles( long userId );



    /* *************************************************
     */
    boolean changeUserPassword( long userId, String oldPassword, String newPassword );


    /* *************************************************
     */
    void updateUser( User user );


    /* *************************************************
     */
    void removeUser( long userId );


    /* *************************************************
     */
    boolean isUserPermitted( User user, PermissionObject permObj, PermissionScope permScope,
                             PermissionOperation permOp );


    /* *************************************************
     *
     */
    Role createRole( String roleName, int roleType );


    /* *************************************************
     *
     */
    User login( String userName, String password);


    /* *************************************************
     */
    List<Role> getAllRoles();


    /* *************************************************
     */
    Role getRole( long roleId );

    /* *************************************************
     */
    void updateRole( Role role );


    /* *************************************************
     */
    void removeRole( long roleId );


    /* *************************************************
     */
    Permission createPermission( int objectId, int scope, boolean read, boolean write, boolean update, boolean delete );


    /* *************************************************
     */
    void assignRolePermission( long roleId, Permission permission );


    /* *************************************************
     */
    void removeAllRolePermissions( long roleId );


    /* *************************************************
     */
    void removePermission( long permissionId );


    /* *************************************************
     */
    UserToken createUserToken( User user, String token, String secret, String issuer,int tokenType,Date validDate);

}
