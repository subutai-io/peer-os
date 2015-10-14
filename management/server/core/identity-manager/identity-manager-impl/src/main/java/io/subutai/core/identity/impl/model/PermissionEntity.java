package io.subutai.core.identity.impl.model;


import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.PermissionObject;
import io.subutai.core.identity.api.model.PermissionOperation;
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

    //*********************************************
    @ManyToOne
    @JoinColumn(name="operation_id", referencedColumnName="id")
    private PermissionOperation operation;
    //*********************************************


    //*********************************************
    @ManyToOne
    @JoinColumn(name="object_id", referencedColumnName="id")
    private PermissionObject object;
    //*********************************************


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


    public PermissionOperation getOperation()
    {
        return operation;
    }


    public void setOperation( final PermissionOperation operation )
    {
        this.operation = operation;
    }


    public PermissionObject getObject()
    {
        return object;
    }


    public void setObject( final PermissionObject object )
    {
        this.object = object;
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
