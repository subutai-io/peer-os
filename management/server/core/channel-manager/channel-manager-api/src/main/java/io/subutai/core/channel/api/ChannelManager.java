package io.subutai.core.channel.api;


import io.subutai.common.dao.DaoManager;

import io.subutai.core.channel.api.token.ChannelTokenManager;


/**
 * Manages  overall Subutai channel (tunnel).
 */
public interface ChannelManager
{
    public DaoManager getDaoManager();

    public void setDaoManager( DaoManager daoManager );

    public ChannelTokenManager getChannelTokenManager();
    public void setChannelTokenManager( final ChannelTokenManager channelTokenManager );


}
