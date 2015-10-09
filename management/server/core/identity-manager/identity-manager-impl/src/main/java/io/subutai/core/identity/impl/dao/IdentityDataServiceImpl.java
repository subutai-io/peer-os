package io.subutai.core.identity.impl.dao;


import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.dao.IdentityDataService;
import io.subutai.core.identity.api.dao.PermissionDAO;
import io.subutai.core.identity.api.dao.RoleDAO;
import io.subutai.core.identity.api.dao.UserDAO;


/**
 *
 */
public class IdentityDataServiceImpl implements IdentityDataService
{
    private final DaoManager daoManager;
    private UserDAO userDAOService;
    private RoleDAO roleDAOService;
    private PermissionDAO permissionDAOService;

    private IdentityDataService identityDataService;


    /* *************************************************
     *
     */
    public IdentityDataServiceImpl( final DaoManager daoManager)
    {
        this.daoManager = daoManager;
        userDAOService  = new UserDAOImpl( daoManager );
        roleDAOService  = new RoleDAOImpl( daoManager );
        permissionDAOService = new PermissionDAOImpl( daoManager );
    }


}
