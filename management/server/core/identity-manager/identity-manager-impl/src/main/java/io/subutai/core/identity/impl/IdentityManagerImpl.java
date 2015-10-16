package io.subutai.core.identity.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.dao.IdentityDataService;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.impl.dao.IdentityDataServiceImpl;
import io.subutai.core.identity.impl.model.RoleEntity;
import io.subutai.core.identity.impl.model.UserEntity;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;


/**
 * Implementation of Identity Manager
 */
public class IdentityManagerImpl implements IdentityManager
{
    private static final Logger LOG = LoggerFactory.getLogger( IdentityManagerImpl.class );

    private final DaoManager daoManager;
    private IdentityDataService identityDataService;


    /* *************************************************
     *
     */
    public IdentityManagerImpl( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     *
     */
    public void init()
    {
        LOG.info( "Initializing identity manager..." );

        identityDataService = new IdentityDataServiceImpl( daoManager );
    }


    /* *************************************************
     *
     */
    @Override
    public IdentityDataService getIdentityDataService()
    {
        return identityDataService;
    }


    /* *************************************************
     *
     */
    @Override
    @RolesAllowed("Administrator")
    public User login(String userName, String password)
    {
        User user = new UserEntity();
        user.setUserName( userName );

        return user;

    }


    /* *************************************************
     *
     */
    @Override
    public User createUser( String userName, String password, String fullName, String email )
    {
        User user = new UserEntity();
        user.setUserName( userName );
        user.setFullName( fullName );
        user.setEmail( email );

        String salt = "SALT";
        user.setPassword( password );
        user.setSalt( salt );

        identityDataService.persistUser( user );

        return user;
    }


    /* *************************************************
     *
     */
    @Override
    public Role createRole( String roleName, short roleType)
    {
        Role role = new RoleEntity();
        role.setName( roleName );
        role.setType( roleType );

        return role;
    }
}
