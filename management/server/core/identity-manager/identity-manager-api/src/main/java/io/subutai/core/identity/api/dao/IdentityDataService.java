package io.subutai.core.identity.api.dao;


import java.util.List;

import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.identity.api.model.UserToken;


/**
 *
 */
public interface IdentityDataService
{

    /* ******User *************************************
     *
     */
    User getUserByUsername( String userName );


    /* *************************************************
     *
     */
    User getUserByKeyId( String keyId );


    /* *************************************************
     *
     */
    User getUser( long userId );

    /* *************************************************
     *
     */
    void assignUserRole( long userId, Role role );


    /* *************************************************
     *
     */
    void assignUserRole( User user, Role role );

    /* *************************************************
         *
         */
    void removeUserRole( long userId, Role role );


    /* *************************************************
     *
     */
    void removeUserRole( User user, Role role );

    /* *************************************************
         *
         */
    List<User> getAllUsers();


    /* *************************************************
    *
    */
    void persistUser( User item );


    /* *************************************************
     *
     */
    void removeUser( long id );


    /* *************************************************
     *
     */
    void updateUser( User item );


    /* ***********Roles ********************************
     */
    Role getRole( long roleId );

    /* *************************************************
     *
     */
    List<Role> getAllRoles();


    /* *************************************************
     *
     */
    void persistRole( Role item );


    /* *************************************************
     *
     */
    void removeRole( long id );


    /* *************************************************
     *
     */
    void updateRole( Role item );


    /* *************************************************
     *
     */
    void assignRolePermission( long roleId, Permission permission );


    /* *************************************************
     *
     */
    void assignRolePermission( Role role, Permission permission );


    /* *************************************************
     *
     */
    void removeAllRolePermissions( long roleId );


    /* *************************************************
     *
     */
    void removeAllRolePermissions( Role role );


    /*
     * ******Permission*********************************
     */
    Permission getPermission( long permissionId );


    /* *************************************************
     *
     */
    List<Permission> getAllPermissions();


    /* *************************************************
     *
     */
    void persistPermission( Permission item );


    /* *************************************************
     *
     */
    void removePermission( long id );


    /* *************************************************
     *
     */
    void updatePermission( Permission item );


    /* *************************************************
     */
    void removeRolePermission( long roleId, Permission permission );


    /* *************************************************
     */
    void removeRolePermission( Role role, Permission permission );


    /* ******UserToken *********************************
    *
    */
    List<UserToken> getAllUserTokens();


    /* *************************************************
     *
     */
    UserToken getUserToken( String token );


    /* *************************************************
     *
     */
    UserToken getValidUserToken( String token );


    /* *************************************************
     *
     */
    UserToken getUserToken( long userId );


    /* *************************************************
     *
     */
    UserToken getUserTokenByDetails( long userId, int tokenType );

    /* *************************************************
         *
         */
    UserToken getValidUserToken( long userId );


    /* *************************************************
     *
     */
    void persistUserToken( UserToken item );


    /* *************************************************
     *
     */
    void updateUserToken( UserToken item );


    /* *************************************************
     *
     */
    void removeUserToken( String token );


    /* *************************************************
     *
     */
    void removeInvalidTokens();


    /* ******UserDelegate *********************************
     *
     */
    List<UserDelegate> getAllUserDelegates();


    /* *************************************************
     *
     */
    UserDelegate getUserDelegate( String id );


    /* *************************************************
     *
     */
    UserDelegate getUserDelegateByUserId( long userId );


    /* *************************************************
     *
     */
    void persistUserDelegate( UserDelegate item );


    /* *************************************************
     *
     */
    void updateUserDelegate( UserDelegate item );


    /* *************************************************
     *
     */
    void removeUserDelegate( String id );
}
