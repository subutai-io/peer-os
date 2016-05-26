package io.subutai.core.identity.rest.model;


/**
 *
 */
public class AuthID
{
    private String authID;
    private long date;


    public String getAuthID()
    {
        return authID;
    }


    public void setAuthID( final String authID )
    {
        this.authID = authID;
    }


    public long getDate()
    {
        return date;
    }


    public void setDate( final long date )
    {
        this.date = date;
    }
}
