package io.subutai.core.identity.impl.model;


import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;


/**
 *
 */
@Entity
@Table( name = "permission" )
@Access( AccessType.FIELD )
public class PermissionEntity implements Permission
{
    @Id
    @GeneratedValue
    @Column( name = "id" )
    private Long id;

    @Column( name = "object" )
    private int object;

    @Column( name = "scope" )
    private int scope;

    @Column( name = "operation" )
    private int operation;


    //*********************************************
    @ManyToMany(cascade={ CascadeType.ALL}, mappedBy="permissions")
    private Set<Role> roles;
    //*********************************************

    public Long getId()
    {
        return id;
    }


    public void setId( final Long id )
    {
        this.id = id;
    }


    public Set<Role> getRoles()
    {
        return roles;
    }


    public void setRoles( final Set<Role> roles )
    {
        this.roles = roles;
    }
}
