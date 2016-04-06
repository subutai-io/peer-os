package io.subutai.core.hubmanager.api.model;


public interface Config
{
    String getPeerId();

    void setPeerId( final String peerId );

    String getHubIp();

    void setHubIp( final String serverIp );

    String getOwnerId();

    void setOwnerId( final String ownerId );

    String getOwnerEmail();

    void setOwnerEmail( String ownerEmail );
}
