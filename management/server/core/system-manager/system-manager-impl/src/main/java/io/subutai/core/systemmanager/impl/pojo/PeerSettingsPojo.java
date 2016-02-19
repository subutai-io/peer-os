package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.PeerSettings;


/**
 * Created by ermek on 2/11/16.
 */
public class PeerSettingsPojo implements PeerSettings
{
    private String peerOwnerId;
    private String userPeerOwnerName;
    private boolean isRegisteredToHub;


    public String getPeerOwnerId()
    {
        return peerOwnerId;
    }


    public void setPeerOwnerId( final String peerOwnerId )
    {
        this.peerOwnerId = peerOwnerId;
    }


    public String getUserPeerOwnerName()
    {
        return userPeerOwnerName;
    }


    public void setUserPeerOwnerName( final String userPeerOwnerName )
    {
        this.userPeerOwnerName = userPeerOwnerName;
    }


    public boolean isRegisteredToHub()
    {
        return isRegisteredToHub;
    }


    public void setRegisteredToHub( final boolean isRegisteredToHub )
    {
        this.isRegisteredToHub = isRegisteredToHub;
    }
}
