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
import javax.persistence.Table;

import io.subutai.common.security.objects.TokenType;
import io.subutai.common.security.token.TokenUtil;
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
    private int type;

    @Column( name = "hash_algorithm" )
    private String hashAlgorithm;

    @Column( name = "issuer" )
    private String issuer;

    @Column( name = "valid_date" )
    private Date validDate;

	/*@OneToOne (cascade = CascadeType.ALL, targetEntity = UserEntity.class)
    @JoinColumn (name = "user_id", nullable = true, insertable = true, updatable = true)
	private User user;*/


    @Column( name = "user_id" )
    private long userId;


    //***********************************
    @Override
    public String getHeader()
    {
        String str = "";

        str += "{\"typ\":\"JWT\",";
        str += "\"alg\":\"" + hashAlgorithm + "\"}";

        return str;
    }


    //***********************************
    @Override
    public String getClaims()
    {
        String str = "";

        str += "{\"iss\":\"" + issuer + "\",";
        str += "\"sub\":\"" + token + "\"}";

        return str;
    }


    //***********************************
    @Override
    public String getFullToken()
    {
        return TokenUtil.createToken( getHeader(), getClaims(), secret );
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


	/*@Override
	public User getUser ()
	{
		return user;
	}


	@Override
	public void setUser (final User user)
	{
		this.user = user;
	}*/


    @Override
    public int getType()
    {
        return type;
    }


    @Override
    public void setType( final int type )
    {
        this.type = type;
    }


    @Override
    public String getTypeName()
    {
        return TokenType.values()[type - 1].getName();
    }


    @Override
    public long getUserId()
    {
        return userId;
    }


    @Override
    public void setUserId( long userId )
    {
        this.userId = userId;
    }
}
