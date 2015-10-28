package io.subutai.core.identity.api;


import java.util.List;

import javax.security.auth.Subject;

import io.subutai.core.identity.api.dao.IdentityDataService;
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
    List<User> getAllUsers();

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
    void removePermission( long permissionId );
}
