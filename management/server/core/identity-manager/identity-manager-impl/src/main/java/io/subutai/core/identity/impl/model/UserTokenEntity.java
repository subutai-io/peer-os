package io.subutai.core.identity.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id" )
    private long id;

    @Column( name = "user_id" )
    private long userId;

    @Column( name = "token", unique = true )
    private String token;


    public long getId()
    {
        return id;
    }


    public void setId( final long id )
    {
        this.id = id;
    }


    public long getUserId()
    {
        return userId;
    }


    public void setUserId( final long userId )
    {
        this.userId = userId;
    }


    public String getToken()
    {
        return token;
    }


    public void setToken( final String token )
    {
        this.token = token;
    }
}
