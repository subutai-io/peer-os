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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.security.objects.UserType;
import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.Role;


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

    @Column( name = "name", unique = true, nullable = false )
    private String name;

    @Column( name = "type", nullable = false )
    private int type = UserType.SYSTEM.getId();


    //*********************************************
    @ManyToMany( targetEntity = PermissionEntity.class, fetch = FetchType.EAGER )
    @JoinTable( name = "role_permissions", joinColumns = {
            @JoinColumn( name = "role_id", referencedColumnName = "id" )
    }, inverseJoinColumns = { @JoinColumn( name = "permission_id", referencedColumnName = "id" ) } )
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
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ) );

        this.name = name.toLowerCase();
    }


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
        return UserType.values()[type - 1].getName();
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
