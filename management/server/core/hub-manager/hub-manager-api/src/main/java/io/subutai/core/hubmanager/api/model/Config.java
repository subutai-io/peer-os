package io.subutai.core.hubmanager.api.model;


public interface Config
{
    public String getPeerId();

    public void setPeerId( final String peerId );

    public String getHubIp();

    public void setHubIp( final String serverIp );

    public String getOwnerId();

    public void setOwnerId( final String ownerId );
}
