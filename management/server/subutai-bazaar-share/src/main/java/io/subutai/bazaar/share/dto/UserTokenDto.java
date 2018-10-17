package io.subutai.bazaar.share.dto;


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


    private Long ownerId;

    private Long ssUserId;

    private Long envId;

    private String authId;

    private String token;

    private String tokenId;

    private Date validDate;

    private State state;

    private Type type;


    public UserTokenDto()
    {
    }


    public UserTokenDto( final Long ownerId, final Long ssUserId, final Long envId, final String authId, final String token,
                         final String tokenId, final Date validDate )
    {
        this.ownerId = ownerId;
        this.ssUserId = ssUserId;
        this.envId = envId;
        this.authId = authId;
        this.token = token;
        this.tokenId = tokenId;
        this.validDate = validDate;
        this.state = State.READY;
    }


    public Long getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( final Long ownerId )
    {
        this.ownerId = ownerId;
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

    public Long getEnvId() {
        return envId;
    }

    public void setEnvId(Long envId) {
        this.envId = envId;
    }

    public void setTokenId(final String tokenId )
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


    public Long getSsUserId()
    {
        return ssUserId;
    }


    public void setSsUserId( final Long ssUserId )
    {
        this.ssUserId = ssUserId;
    }
}
