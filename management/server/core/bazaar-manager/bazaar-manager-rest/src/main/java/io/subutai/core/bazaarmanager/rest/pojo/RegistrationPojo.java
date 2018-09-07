package io.subutai.core.bazaarmanager.rest.pojo;


public class RegistrationPojo
{
    private boolean isRegisteredToHub;
    private boolean isHubReachable;
    private String ownerId;
    private String currentUserEmail;
    private String peerName;


    public boolean isRegisteredToHub()
    {
        return isRegisteredToHub;
    }


    public void setRegisteredToHub( boolean isRegisteredToHub )
    {
        this.isRegisteredToHub = isRegisteredToHub;
    }


    public boolean isHubReachable()
    {
        return isHubReachable;
    }


    public void setHubReachable( final boolean hubReachable )
    {
        isHubReachable = hubReachable;
    }


    public String getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( final String ownerId )
    {
        this.ownerId = ownerId;
    }


    public String getCurrentUserEmail()
    {
        return currentUserEmail;
    }


    public void setCurrentUserEmail( String currentUserEmail )
    {
        this.currentUserEmail = currentUserEmail;
    }


    public void setPeerName( final String peerName )
    {
        this.peerName = peerName;
    }
}
