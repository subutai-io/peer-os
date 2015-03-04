package org.safehaus.subutai.core.channel.impl;


import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.core.channel.api.ChannelManager;
import org.safehaus.subutai.core.channel.impl.token.ChannelTokenController;
import org.safehaus.subutai.core.channel.impl.token.ChannelTokenManagerImpl;



/**
 * Created by nisakov on 2/25/15.
 */
public class ChannelManagerImpl implements ChannelManager
{
    private DaoManager daoManager = null;

    public void init()
    {
        ChannelTokenManagerImpl.setEntityManagerFactory( daoManager.getEntityManagerFactory() );

        //-------------------------------------------------------------
        new Thread( new ChannelTokenController() ).start();
        //-------------------------------------------------------------
    }



    public DaoManager getDaoManager()
    {

        return daoManager;
    }

    public void setDaoManager( DaoManager daoManager )
    {


        this.daoManager = daoManager;
    }
}

