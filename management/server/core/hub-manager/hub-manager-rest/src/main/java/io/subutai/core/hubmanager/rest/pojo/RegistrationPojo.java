package io.subutai.core.hubmanager.rest.pojo;


public class RegistrationPojo
{
    private boolean isRegisteredToHub;
    private String ownerId;
    private String currentUserEmail;

    public boolean isRegisteredToHub()
    {
        return isRegisteredToHub;
    }


    public void setRegisteredToHub( boolean isRegisteredToHub )
    {
        this.isRegisteredToHub = isRegisteredToHub;
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
}
