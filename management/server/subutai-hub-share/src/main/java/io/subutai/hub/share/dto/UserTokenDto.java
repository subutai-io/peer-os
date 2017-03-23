package io.subutai.hub.share.dto;


import java.util.Date;


public class UserTokenDto
{
    public enum State
    {
        UPDATE,
        DELETE
    }


    private Long userId;

    private String authId;

    private String token;

    private String tokenId;

    private Long lifetime;

    private Date validDate;

    private State state;


    public UserTokenDto()
    {
    }


    public Long getUserId()
    {
        return userId;
    }


    public void setUserId( final Long userId )
    {
        this.userId = userId;
    }


    public String getAuthId()
    {
        return authId;
    }


    public void setAuthId( final String authId )
    {
        this.authId = authId;
    }


    public String getToken()
    {
        return token;
    }


    public void setToken( final String token )
    {
        this.token = token;
    }


    public String getTokenId()
    {
        return tokenId;
    }


    public void setTokenId( final String tokenId )
    {
        this.tokenId = tokenId;
    }


    public State getState()
    {
        return state;
    }


    public void setState( final State state )
    {
        this.state = state;
    }


    public Long getLifetime()
    {
        return lifetime;
    }


    public void setLifetime( final Long lifetime )
    {
        this.lifetime = lifetime;
    }


    public Date getValidDate()
    {
        return validDate;
    }


    public void setValidDate( final Date validDate )
    {
        this.validDate = validDate;
    }
}
