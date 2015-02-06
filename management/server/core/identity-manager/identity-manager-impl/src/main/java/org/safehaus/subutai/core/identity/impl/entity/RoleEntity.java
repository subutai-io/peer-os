package org.safehaus.subutai.core.identity.impl.entity;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.safehaus.subutai.core.identity.api.Role;
import org.safehaus.subutai.core.identity.api.User;


/**
 * Implementation of Role interface.
 */
@Entity
@Table( name = "subutai_role" )
@Access( AccessType.FIELD )
public class RoleEntity implements Role
{
    @Id
    private String name;
    @Column
    private String permissions;


    @ManyToMany( targetEntity = UserEntity.class )
    @JoinTable( name = "subutai_user_role", joinColumns = @JoinColumn( name = "role_name", referencedColumnName =
            "name" ), inverseJoinColumns = @JoinColumn( name = "user_id", referencedColumnName = "user_id" ) )
    Set<User> users = new HashSet();


    @Override
    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    @Override
    public List<String> getPermissions()
    {
        return Arrays.asList( permissions.split( ";" ) );
    }
}
