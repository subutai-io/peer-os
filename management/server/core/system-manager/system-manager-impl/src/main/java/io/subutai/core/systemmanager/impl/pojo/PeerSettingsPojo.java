package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.PeerSettings;


public class PeerSettingsPojo implements PeerSettings
{
    private String peerOwnerId;
    private String userPeerOwnerName;


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
}
