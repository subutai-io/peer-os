package io.subutai.core.identity.impl.dao;


import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.dao.UserDAO;


/**
 * Implementation of User Dao Manager
 */
public class UserDAOImpl implements UserDAO
{
    private DaoManager daoManager = null;


    /* *************************************************
     *
     */
    public UserDAOImpl( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }
}
