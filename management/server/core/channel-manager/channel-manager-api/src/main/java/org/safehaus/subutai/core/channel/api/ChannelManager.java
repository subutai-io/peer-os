package org.safehaus.subutai.core.channel.api;


import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.core.channel.api.entity.IUserChannelToken;
import org.safehaus.subutai.core.channel.api.token.ChannelTokenManager;


/**
 * Created by nisakov on 3/3/15.
 */
public interface ChannelManager
{
    public DaoManager getDaoManager();

    public void setDaoManager( DaoManager daoManager );

    public ChannelTokenManager getChannelTokenManager();
    public void setChannelTokenManager( final ChannelTokenManager channelTokenManager );


}
