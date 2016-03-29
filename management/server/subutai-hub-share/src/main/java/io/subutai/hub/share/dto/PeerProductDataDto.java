package io.subutai.hub.share.dto;


import java.util.Date;


public class PeerProductDataDto
{
    public enum State
    {
        INSTALL, INSTALLED, REMOVE
    }

    private State state;

    private String productId;

    private Date installDate;


    public PeerProductDataDto()
    {
    }


    public State getState()
    {
        return state;
    }


    public void setState( final State state )
    {
        this.state = state;
    }


    public String getProductId()
    {
        return productId;
    }


    public void setProductId( final String productId )
    {
        this.productId = productId;
    }


    public Date getInstallDate()
    {
        return installDate;
    }


    public void setInstallDate( final Date installDate )
    {
        this.installDate = installDate;
    }
}
