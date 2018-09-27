package io.subutai.core.bazaarmanager.rest.pojo;


public class RegistrationPojo
{
    private boolean isRegisteredToBazaar;
    private boolean isBazaarReachable;
    private String ownerId;
    private String currentUserEmail;
    private String peerName;


    public boolean isRegisteredToBazaar()
    {
        return isRegisteredToBazaar;
    }


    public void setRegisteredToBazaar( boolean isRegisteredToBazaar )
    {
        this.isRegisteredToBazaar = isRegisteredToBazaar;
    }


    public boolean isBazaarReachable()
    {
        return isBazaarReachable;
    }


    public void setBazaarReachable( final boolean bazaarReachable )
    {
        isBazaarReachable = bazaarReachable;
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
