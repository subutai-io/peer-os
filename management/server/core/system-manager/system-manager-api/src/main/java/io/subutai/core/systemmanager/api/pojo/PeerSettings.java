package io.subutai.core.systemmanager.api.pojo;


//todo remove mutators from interface
public interface PeerSettings
{
    String getPeerOwnerId();


    void setPeerOwnerId( final String peerOwnerId );


    String getUserPeerOwnerName();


    void setUserPeerOwnerName( final String userPeerOwnerName );
}
