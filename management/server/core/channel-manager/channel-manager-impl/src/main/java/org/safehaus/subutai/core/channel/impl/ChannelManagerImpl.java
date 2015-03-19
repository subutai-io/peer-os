package org.safehaus.subutai.core.channel.impl;


import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.core.channel.api.ChannelManager;
import org.safehaus.subutai.core.channel.api.token.ChannelTokenManager;
import org.safehaus.subutai.core.channel.impl.token.ChannelTokenController;
import org.safehaus.subutai.core.channel.impl.token.ChannelTokenManagerImpl;
import org.safehaus.subutai.core.identity.api.IdentityManager;



/**
 * Created by nisakov on 2/25/15.
 * Manages all CXF channels (tunnel)
 */
public class ChannelManagerImpl implements ChannelManager
{
    private DaoManager daoManager = null;
    private ChannelTokenManager channelTokenManager = null;
    private IdentityManager identityManager = null;

    public void init()
    {
        channelTokenManager = new ChannelTokenManagerImpl();
        channelTokenManager.setEntityManagerFactory( daoManager.getEntityManagerFactory() );

        //-------------------------------------------------------------
        new Thread( new ChannelTokenController(channelTokenManager) ).start();
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


    public ChannelTokenManager getChannelTokenManager()
    {
        return channelTokenManager;
    }


    public void setChannelTokenManager( final ChannelTokenManager channelTokenManager )
    {
        this.channelTokenManager = channelTokenManager;
    }


    public IdentityManager getIdentityManager()
    {
        return identityManager;
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }
}

