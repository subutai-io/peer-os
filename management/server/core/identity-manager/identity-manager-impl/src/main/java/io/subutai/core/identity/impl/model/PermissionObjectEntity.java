package io.subutai.core.identity.impl.model;


import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.subutai.core.identity.api.model.Permission;
import io.subutai.core.identity.api.model.PermissionObject;


/**
 *
 */
@Entity
@Table( name = "permission_object" )
@Access( AccessType.FIELD )
public class PermissionObjectEntity  implements PermissionObject
{
    @Id
    @GeneratedValue
    @Column( name = "id" )
    private Long id;

    @Column( name = "name" )
    private String name;

    @Column( name = "type" )
    private Short type = 1;

    @OneToMany(mappedBy="operation")
    private List<Permission> permissions;


    public Long getId()
    {
        return id;
    }


    public void setId( final Long id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public Short getType()
    {
        return type;
    }


    public void setType( final Short type )
    {
        this.type = type;
    }


    public List<Permission> getPermissions()
    {
        return permissions;
    }


    public void setPermissions( final List<Permission> permissions )
    {
        this.permissions = permissions;
    }

}
