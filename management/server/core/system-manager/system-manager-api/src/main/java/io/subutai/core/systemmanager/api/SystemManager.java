package io.subutai.core.systemmanager.api;


import io.subutai.core.systemmanager.api.pojo.ChannelSettings;
import io.subutai.core.systemmanager.api.pojo.KurjunSettings;
import io.subutai.core.systemmanager.api.pojo.PeerOwner;
import io.subutai.core.systemmanager.api.pojo.PeerSettings;
import io.subutai.core.systemmanager.api.pojo.SystemInfo;


/**
 * Created by ermek on 2/6/16.
 */
public interface SystemManager
{
    public PeerSettings getPeerSettings();

    public void setPeerSettings( PeerSettings settings );

    public KurjunSettings getKurjunSettings();

    public void setKurjunSettings( KurjunSettings settings );

    public ChannelSettings getChannelSettings();

    public void setChannelSettings( ChannelSettings settings );

    public SystemInfo getSystemInfo();

    public void setPeerOwner();

    public PeerOwner getPeerOwnerInfo();
}
