package io.subutai.core.systemmanager.api.pojo;


/**
 * Created by ermek on 2/11/16.
 */
public interface PeerSettings
{
    public String getPeerOwnerId();


    public void setPeerOwnerId( final String peerOwnerId );


    public String getUserPeerOwnerName();


    public void setUserPeerOwnerName( final String userPeerOwnerName );
}
