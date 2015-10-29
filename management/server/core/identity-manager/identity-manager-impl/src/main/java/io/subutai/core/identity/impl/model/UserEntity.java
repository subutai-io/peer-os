package io.subutai.core.identity.impl.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.impl.model.RoleEntity;


/**
 * Implementation of User interface. Used for storing user information.
 */
@Entity
@Table( name = "userl" )
@Access( AccessType.FIELD )
public class UserEntity implements User
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id" )
    private long id;

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


    //*********************************************
    @ManyToMany (targetEntity=RoleEntity.class,fetch = FetchType.EAGER)
    @JoinTable( name = "user_roles",
            joinColumns = { @JoinColumn( name = "user_id", referencedColumnName = "id" ) },
            inverseJoinColumns = { @JoinColumn( name = "role_id", referencedColumnName = "id" ) })
    private List<Role> roles = new ArrayList<>();
    //*********************************************


    @Override
    public Long getId()
    {
        return id;
    }


    @Override
    public void setId( final Long id )
    {
        this.id = id;
    }


    @Override
    public String getUserName()
    {
        return userName;
    }


    @Override
    public void setUserName( final String userName )
    {
        this.userName = userName;
    }


    @Override
    public String getFullName()
    {
        return fullName;
    }


    @Override
    public void setFullName( final String fullName )
    {
        this.fullName = fullName;
    }


    @Override
    public String getPassword()
    {
        return password;
    }


    @Override
    public void setPassword( final String password )
    {
        this.password = password;
    }


    @Override
    public String getSalt()
    {
        return salt;
    }


    @Override
    public void setSalt( final String salt )
    {
        this.salt = salt;
    }


    @Override
    public String getEmail()
    {
        return email;
    }


    @Override
    public void setEmail( final String email )
    {
        this.email = email;
    }



    @Override
    public List<Role> getRoles()
    {
        return roles;
    }


    @Override
    public void setRoles( final List<Role> roles )
    {
        this.roles = roles;
    }
}
