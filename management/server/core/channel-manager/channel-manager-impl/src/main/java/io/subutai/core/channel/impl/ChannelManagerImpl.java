package io.subutai.core.channel.impl;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.channel.api.ChannelManager;
import io.subutai.core.channel.api.token.ChannelTokenManager;
import io.subutai.core.channel.impl.token.ChannelTokenController;
import io.subutai.core.channel.impl.token.ChannelTokenManagerImpl;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;


/**
 * Manages all CXF channels (tunnel)
 */
public class ChannelManagerImpl implements ChannelManager
{
    private DaoManager daoManager = null;
    private ChannelTokenManager channelTokenManager = null;
    private IdentityManager identityManager = null;
    private SecurityManager securityManager = null;
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private boolean encryptionEnabled;

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


    public void setEncryptionEnabled( final boolean encryptionEnabled )
    {
        this.encryptionEnabled = encryptionEnabled;
    }


    public ChannelTokenManager getChannelTokenManager()
    {
        return channelTokenManager;
    }


    public void setChannelTokenManager( final ChannelTokenManager channelTokenManager )
    {
        this.channelTokenManager = channelTokenManager;
    }


    @Override
    public boolean isEncryptionEnabled()
    {
        return encryptionEnabled;
    }


    public IdentityManager getIdentityManager()
    {
        return identityManager;
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    public SecurityManager getSecurityManager()
    {
        return securityManager;
    }


    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    public ScheduledExecutorService getExecutorService()
    {
        return executorService;
    }


    public void setExecutorService( final ScheduledExecutorService executorService )
    {
        this.executorService = executorService;
    }
}

