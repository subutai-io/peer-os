package io.subutai.core.identity.impl.model;


import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;


/**
 * .
 */
@Entity
@Table( name = "user_token" )
@Access( AccessType.FIELD )
public class UserTokenEntity implements UserToken
{
    @Id
    @Column( name = "token", unique = true )
    private String token;

    @Column( name = "secret" )
    private String secret;

    @Column( name = "type" )
    private String type;

    @Column( name = "hash_algorithm" )
    private String hashAlgorithm;

    @Column( name = "issuer")
    private String issuer;

    @Column( name = "valid_date")
    private Date validDate;

    @OneToOne(cascade= CascadeType.ALL, targetEntity = UserEntity.class)
    @JoinColumn(name="user_id", nullable=true, insertable=true, updatable=true )
    private User user;


    //***********************************
    @Override
    public String getHeader()
    {
        String str = "";

        str += "{\"typ\":\""+type+"\",";
        str +=  "\"alg\":\""+hashAlgorithm+"\"}";

        return str;
    }


    //***********************************
    @Override
    public String getClaims()
    {
        String str = "";

        str += "{\"iss\":\""+issuer+"\",";
        str +=  "\"sub\":\""+token+"\"}";

        return str;
    }


    //***********************************


    @Override
    public String getToken()
    {
        return token;
    }


    @Override
    public void setToken( final String token )
    {
        this.token = token;
    }


    @Override
    public String getSecret()
    {
        return secret;
    }


    @Override
    public void setSecret( final String secret )
    {
        this.secret = secret;
    }


    @Override
    public String getType()
    {
        return type;
    }


    @Override
    public void setType( final String type )
    {
        this.type = type;
    }


    @Override
    public String getHashAlgorithm()
    {
        return hashAlgorithm;
    }


    @Override
    public void setHashAlgorithm( final String hashAlgorithm )
    {
        this.hashAlgorithm = hashAlgorithm;
    }


    @Override
    public String getIssuer()
    {
        return issuer;
    }


    @Override
    public void setIssuer( final String issuer )
    {
        this.issuer = issuer;
    }


    @Override
    public Date getValidDate()
    {
        return validDate;
    }


    @Override
    public void setValidDate( final Date validDate )
    {
        this.validDate = validDate;
    }


    @Override
    public User getUser()
    {
        return user;
    }


    @Override
    public void setUser( final User user )
    {
        this.user = user;
    }
}
