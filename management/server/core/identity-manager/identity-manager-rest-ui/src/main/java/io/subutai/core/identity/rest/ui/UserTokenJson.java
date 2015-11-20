package io.subutai.core.identity.rest.ui;


import java.util.Date;


public class UserTokenJson
{
    private long userId;

    private String userName;

    private String token;

    private String secret;

    private int type;

    private String hashAlgorithm;

    private String issuer;

    private Date validDate;


    public UserTokenJson( final long userId, final String userName, final String token, final String secret, final int type,
                          final String hashAlgorithm, final String issuer, final Date validDate )
    {
        this.userId = userId;
        this.userName = userName;
        this.token = token;
        this.secret = secret;
        this.type = type;
        this.hashAlgorithm = hashAlgorithm;
        this.issuer = issuer;
        this.validDate = validDate;
    }


    public long getUserId()
    {
        return userId;
    }


    public void setUserId( final long userId )
    {
        this.userId = userId;
    }


    public String getUserName()
    {
        return userName;
    }


    public void setUserName( final String userName )
    {
        this.userName = userName;
    }


    public String getToken()
    {
        return token;
    }


    public void setToken( final String token )
    {
        this.token = token;
    }


    public String getSecret()
    {
        return secret;
    }


    public void setSecret( final String secret )
    {
        this.secret = secret;
    }


    public int getType()
    {
        return type;
    }


    public void setType( final int type )
    {
        this.type = type;
    }


    public String getHashAlgorithm()
    {
        return hashAlgorithm;
    }


    public void setHashAlgorithm( final String hashAlgorithm )
    {
        this.hashAlgorithm = hashAlgorithm;
    }


    public String getIssuer()
    {
        return issuer;
    }


    public void setIssuer( final String issuer )
    {
        this.issuer = issuer;
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
