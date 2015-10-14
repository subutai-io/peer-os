package io.subutai.core.identity.api;


import io.subutai.common.security.annotations.AccessControl;
import io.subutai.common.security.objects.PermissionObjectType;
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
    @AccessControl(objects= PermissionObjectType.,operations="")
    public IdentityDataService getIdentityDataService();

    /* *************************************************
     *
     */
    @AccessControl(objects="",operations="")
    User createUser( String userName, String password, String fullName, String email );

    /* *************************************************
     *
     */
    @AccessControl(objects="",operations="")
    Role createRole( String roleName, short roleType );
}
