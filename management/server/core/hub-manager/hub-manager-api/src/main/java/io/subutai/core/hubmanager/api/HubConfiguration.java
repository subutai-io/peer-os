package io.subutai.core.hubmanager.api;


import io.subutai.core.hubmanager.api.model.Config;


public class HubConfiguration implements Config
{
    public static final String PRODUCT_NAME = "Hub integration";
    public static final String PRODUCT_KEY = "Hub integration";

    public static String serverName;
    public static String baseUrl;
    public static String superNodeIp;

    private String hubIp;
    private String nodeIp;
    private String peerId;


    public String getSuperNodeIp()
    {
        return superNodeIp;
    }


    public void setSuperNodeIp( final String superNodeIp )
    {
        this.superNodeIp = superNodeIp;
    }


    public void setServerName( final String serverName )
    {
        baseUrl = String.format( "https://%s", serverName );
        this.serverName = serverName;
    }


    public String getServerName()
    {
        return serverName;
    }


    public String getHubIp()
    {
        return hubIp;
    }


    public void setHubIp( final String hubIp )
    {
        this.hubIp = hubIp;
    }


    @Override
    public String getOwnerId()
    {
        return null;
    }


    @Override
    public void setOwnerId( final String ownerId )
    {

    }


    public String getNodeIp()
    {
        return nodeIp;
    }


    public void setNodeIp( final String nodeIp )
    {
        this.nodeIp = nodeIp;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public String getPeerId()
    {
        return peerId;
    }


    @Override
    public String getOwnerEmail()
    {
        return null;
    }


    @Override
    public void setOwnerEmail( final String ownerEmail )
    {

    }
}
