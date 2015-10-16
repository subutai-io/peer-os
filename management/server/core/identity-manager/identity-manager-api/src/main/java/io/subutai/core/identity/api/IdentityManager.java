package io.subutai.core.identity.api;


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
     *
     */
    User createUser( String userName, String password, String fullName, String email );


    /* *************************************************
     *
     */
    Role createRole( String roleName, short roleType );


    /* *************************************************
     *
     */
    User login( String userName, String password);

}
