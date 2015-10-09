package io.subutai.core.identity.impl.dao;


import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.dao.PermissionDAO;


/**
 *
 */
public class PermissionDAOImpl implements PermissionDAO
{
    private DaoManager daoManager= null;


    /* *************************************************
     *
     */
    public PermissionDAOImpl( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }
}
