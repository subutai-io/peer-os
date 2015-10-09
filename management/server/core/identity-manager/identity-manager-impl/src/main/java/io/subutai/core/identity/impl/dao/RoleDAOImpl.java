package io.subutai.core.identity.impl.dao;


import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.dao.RoleDAO;


/**
 *
 */
public class RoleDAOImpl implements RoleDAO
{
    private DaoManager daoManager= null;


    /* *************************************************
     *
     */
    public RoleDAOImpl( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }
}
