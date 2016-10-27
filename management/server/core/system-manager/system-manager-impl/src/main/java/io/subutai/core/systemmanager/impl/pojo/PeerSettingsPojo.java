package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.PeerSettings;


public class PeerSettingsPojo implements PeerSettings
{
    private String peerOwnerId;
    private String userPeerOwnerName;


    @Override
    public String getPeerOwnerId()
    {
        return peerOwnerId;
    }


    @Override
    public void setPeerOwnerId( final String peerOwnerId )
    {
        this.peerOwnerId = peerOwnerId;
    }


    @Override
    public String getUserPeerOwnerName()
    {
        return userPeerOwnerName;
    }


    @Override
    public void setUserPeerOwnerName( final String userPeerOwnerName )
    {
        this.userPeerOwnerName = userPeerOwnerName;
    }
}
