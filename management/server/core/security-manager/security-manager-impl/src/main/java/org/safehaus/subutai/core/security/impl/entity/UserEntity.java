package org.safehaus.subutai.core.security.impl.entity;


import java.util.Arrays;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.safehaus.subutai.core.security.api.User;


/**
 * Implementation of User interface. Used for storing user information.
 */
@Entity
@Table( name = "subutai_user" )
@Access( AccessType.FIELD )
public class UserEntity implements User
{
    @Id
    @Column( name = "user_id" )
    private Long id;
    //
    //    @OneToMany( mappedBy = "user", fetch = FetchType.EAGER,
    //            targetEntity = RoleEntity.class )
    //    List<Role> roles = new ArrayList();

    @Column( name = "username" )
    public String username;

    @Column( name = "password" )
    public String password;

    @Column( name = "permissions" )
    public String permissions;

    //
    //    @Override
    //    public List<Role> getRoles()
    //    {
    //        return roles;
    //    }


    @Override
    public Long getId()
    {
        return id;
    }


    @Override
    public String getPassword()
    {
        return password;
    }


    @Override
    public List<String> getPermissions()
    {
        return Arrays.asList( permissions.split( ";" ) );
    }


    public void setId( final Long id )
    {
        this.id = id;
    }


    //    public void setRoles( final List<Role> roles )
    //    {
    //        this.roles = roles;
    //    }


    public void setPassword( final String password )
    {
        this.password = password;
    }


    public void setPermissions( final String permissions )
    {
        this.permissions = permissions;
    }
}
