package io.subutai.hub.share.dto;


import java.util.Date;


public class UserTokenDto
{
    public enum State
    {
        EXPIRED, READY, DELETE, EMPTY
    }


    public enum Type
    {
        USER, ENV_USER, GITHUB, OTHER
    }


    private Long userId;

    private String authId;

    private String token;

    private String tokenId;

    private Date validDate;

    private State state;

    private Type type;


    public UserTokenDto()
    {
    }


    public UserTokenDto( final Long userId, final String authId, final String token, final String tokenId,
                         final Date validDate )
    {
        this.userId = userId;
        this.authId = authId;
        this.token = token;
        this.tokenId = tokenId;
        this.validDate = validDate;
        this.state = State.READY;
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


    public Date getValidDate()
    {
        return validDate;
    }


    public void setValidDate( final Date validDate )
    {
        this.validDate = validDate;
    }


    public Type getType()
    {
        return type;
    }


    public void setType( final Type type )
    {
        this.type = type;
    }
}
