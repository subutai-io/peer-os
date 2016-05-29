package io.subutai.core.identity.rest.model;


/**
 *
 */
public class AuthMessage
{
    private String token  = "";
    private String authId = "";

    private int status = 0;


    public String getToken()
    {
        return token;
    }


    public void setToken( final String token )
    {
        this.token = token;
    }


    public String getAuthId()
    {
        return authId;
    }


    public void setAuthId( final String authId )
    {
        this.authId = authId;
    }


    public int getStatus()
    {
        return status;
    }


    public void setStatus( final int status )
    {
        this.status = status;
    }
}
