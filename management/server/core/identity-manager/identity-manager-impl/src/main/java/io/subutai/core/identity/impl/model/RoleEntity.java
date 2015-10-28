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

import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.impl.model.PermissionEntity;


/**
 * Implementation of Role interface.
 */
@Entity
@Table( name = "role" )
@Access( AccessType.FIELD )
public class RoleEntity implements Role
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id" )
    private long id;

    @Column( name = "name" )
    private String name;

    @Column( name = "type" )
    private Short type = 1;


    //*********************************************
    @ManyToMany(targetEntity=PermissionEntity.class,fetch = FetchType.EAGER)
    @JoinTable( name = "role_permissions",
            joinColumns = { @JoinColumn( name = "role_id", referencedColumnName = "id" ) },
            inverseJoinColumns = { @JoinColumn( name = "permission_id", referencedColumnName = "id" ) })
    private List<Permission> permissions = new ArrayList<>();
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
    public List<Permission> getPermissions()
    {
        return permissions;
    }


    @Override
    public void setPermissions( List<Permission> permissions )
    {
        this.permissions = permissions;
    }


}
