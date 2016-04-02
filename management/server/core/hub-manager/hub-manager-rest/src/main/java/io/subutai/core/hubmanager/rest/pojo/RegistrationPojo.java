package io.subutai.core.hubmanager.rest.pojo;


public class RegistrationPojo
{
    private boolean isRegisteredToHub;
    private String ownerId;

    public boolean isRegisteredToHub()
    {
        return isRegisteredToHub;
    }


    public void setRegisteredToHub( final boolean isRegisteredToHub )
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
}
