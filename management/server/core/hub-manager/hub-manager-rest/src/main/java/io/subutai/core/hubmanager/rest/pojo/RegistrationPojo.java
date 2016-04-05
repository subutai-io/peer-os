package io.subutai.core.hubmanager.rest.pojo;


public class RegistrationPojo
{
    private boolean isRegisteredToHub;
    private String ownerId;
    private String ownerEmail;

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


    public String getOwnerEmail()
    {
        return ownerEmail;
    }


    public void setOwnerEmail( String ownerEmail )
    {
        this.ownerEmail = ownerEmail;
    }
}
