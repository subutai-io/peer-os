package io.subutai.core.channel.impl;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.common.dao.DaoManager;
import io.subutai.core.channel.api.ChannelManager;
import io.subutai.core.channel.api.token.ChannelTokenManager;
import io.subutai.core.channel.impl.token.ChannelTokenController;
import io.subutai.core.channel.impl.token.ChannelTokenManagerImpl;
import io.subutai.core.identity.api.IdentityManager;


/**
 * Manages all CXF channels (tunnel)
 */
public class ChannelManagerImpl implements ChannelManager
{
    private DaoManager daoManager = null;
    private ChannelTokenManager channelTokenManager = null;
    private IdentityManager identityManager = null;
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();


    public void init()
    {
        channelTokenManager = new ChannelTokenManagerImpl();
        channelTokenManager.setEntityManagerFactory( daoManager.getEntityManagerFactory() );

        executorService
                .scheduleWithFixedDelay( new ChannelTokenController( channelTokenManager ), 1, 1, TimeUnit.HOURS );
        //-------------------------------------------------------------
        //        new Thread( new ChannelTokenController( channelTokenManager ) ).start();
        //-------------------------------------------------------------
    }


    public void destroy()
    {
        executorService.shutdown();
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

