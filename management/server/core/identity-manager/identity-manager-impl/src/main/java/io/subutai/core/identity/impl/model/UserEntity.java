package io.subutai.core.identity.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.core.identity.api.model.User;


/**
 * Implementation of User interface. Used for storing user information.
 */
@Entity
@Table( name = "userl" )
@Access( AccessType.FIELD )
public class UserEntity implements User
{
    @Id
    @GeneratedValue
    @Column( name = "id" )
    private Long id;

    @Column( name = "user_name", unique = true )
    private String userName;

    @Column( name = "full_name" )
    private String fullName;

    @Column( name = "password" )
    private String password;

    @Column( name = "salt" )
    private String salt;

    @Column( name = "email" )
    private String email;

    @Column( name = "key" )
    private String key = "Empty key: not implemented yet.";


    public Long getId()
    {
        return id;
    }


    public void setId( final Long id )
    {
        this.id = id;
    }


    public String getUserName()
    {
        return userName;
    }


    public void setUserName( final String userName )
    {
        this.userName = userName;
    }


    public String getFullName()
    {
        return fullName;
    }


    public void setFullName( final String fullName )
    {
        this.fullName = fullName;
    }


    public String getPassword()
    {
        return password;
    }


    public void setPassword( final String password )
    {
        this.password = password;
    }


    public String getSalt()
    {
        return salt;
    }


    public void setSalt( final String salt )
    {
        this.salt = salt;
    }


    public String getEmail()
    {
        return email;
    }


    public void setEmail( final String email )
    {
        this.email = email;
    }


    public String getKey()
    {
        return key;
    }


    public void setKey( final String key )
    {
        this.key = key;
    }
}
