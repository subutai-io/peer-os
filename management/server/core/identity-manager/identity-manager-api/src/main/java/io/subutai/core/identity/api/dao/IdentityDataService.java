package io.subutai.core.identity.api.dao;


import java.util.List;

import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.PermissionObject;
import io.subutai.core.identity.api.model.PermissionOperation;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.User;


/**
 *
 */
public interface IdentityDataService
{
    /* ******User *************************************
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

    /* ******PermissionObjects***************************
     *
     */
    List<PermissionObject> getAllPermissionObjects();

    /* *************************************************
     *
     */
    void persistPermissionObject( PermissionObject item );

    /* *************************************************
     *
     */
    void removePermissionObject( long id );

    /* *************************************************
     *
     */
    void updatePermissionObject( PermissionObject item );

    /* ******PermissionOperations************************
     *
     */
    List<PermissionOperation> getAllPermissionOperations();

    /* *************************************************
     *
     */
    void persistPermissionOperation( PermissionOperation item );

    /* *************************************************
     *
     */
    void removePermissionOperation( long id );

    /* *************************************************
     *
     */
    void updatePermissionOperation( PermissionOperation item );

    /* ******Session************************
     *
     */
    List<Session> getAllSession();

    /* *************************************************
     *
     */
    void persistSession( Session item );

    /* *************************************************
     *
     */
    void removeSession( long id );

    /* *************************************************
     *
     */
    void updateSession( Session item );
}
