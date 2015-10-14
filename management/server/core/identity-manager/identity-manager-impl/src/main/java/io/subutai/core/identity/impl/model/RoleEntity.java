package io.subutai.core.identity.impl.model;


import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;


/**
 * Implementation of Role interface.
 */
@Entity
@Table( name = "role" )
@Access( AccessType.FIELD )
public class RoleEntity implements Role
{
    @Id
    @GeneratedValue
    @Column( name = "id" )
    private Long id;

    @Column( name = "name" )
    private String name;

    @Column( name = "type" )
    private Short type = 1;

    //*********************************************
    @ManyToMany
    @JoinTable( name = "AssignedUsers",
            joinColumns = { @JoinColumn( name = "role_id", referencedColumnName = "id" ) },
            inverseJoinColumns = { @JoinColumn( name = "user_id", referencedColumnName = "id" ) } )
    private Set<User> assignedUsers;
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
    public String getName()
    {
        return name;
    }


    @Override
    public void setName( final String name )
    {
        this.name = name;
    }


    @Override
    public Short getType()
    {
        return type;
    }


    @Override
    public void setType( final Short type )
    {
        this.type = type;
    }


    @Override
    public Set<User> getAssignedUsers()
    {
        return assignedUsers;
    }


    @Override
    public void setAssignedUsers( final Set<User> assignedUsers )
    {
        this.assignedUsers = assignedUsers;
    }
}
