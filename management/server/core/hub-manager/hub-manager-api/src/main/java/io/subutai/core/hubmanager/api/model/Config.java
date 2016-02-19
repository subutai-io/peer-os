package io.subutai.core.hubmanager.api.model;


/**
 * Created by ermek on 10/27/15.
 */
public interface Config
{
    public String getPeerId();

    public void setPeerId( final String peerId );

    public String getHubIp();

    public void setHubIp( final String serverIp );

    public String getSuperNodeIp();

    public void setSuperNodeIp( final String superNodeIp );
}
