package io.subutai.core.identity.impl.model;


import io.subutai.common.security.objects.PermissionObject;
import io.subutai.core.identity.api.model.RolePermission;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table( name = "role_permission" )
@Access( AccessType.FIELD )
public class RolePermissionEntity implements RolePermission
{


    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;


    @Column( name = "permission_id" )
    private Long permissionId;

    @Column( name = "role_id" )
    private Long roleId;

    @Column( name = "scope" )
    private int scope;

    @Column( name = "read" )
    private boolean read = false;

    @Column( name = "write" )
    private boolean write = false;

    @Column( name = "update" )
    private boolean update = false;

    @Column( name = "delete" )
    private boolean delete = false;

    @Column( name = "role_object" )
    private String roleObject;


    @Column( name = "perm_object" )
    private int permObject;


    public Long getPermissionId()
    {
        return permissionId;
    }


    public void setPermissionId( Long permissionId )
    {
        this.permissionId = permissionId;
    }


    public Long getRoleId()
    {
        return roleId;
    }


    public void setRoleId( Long roleId )
    {
        this.roleId = roleId;
    }


    public int getScope()
    {
        return scope;
    }


    public void setScope( int scope )
    {
        this.scope = scope;
    }


    public boolean isRead()
    {
        return read;
    }


    public void setRead( boolean read )
    {
        this.read = read;
    }


    public boolean isWrite()
    {
        return write;
    }


    public void setWrite( boolean write )
    {
        this.write = write;
    }


    public boolean isUpdate()
    {
        return update;
    }


    public void setUpdate( boolean update )
    {
        this.update = update;
    }


    public boolean isDelete()
    {
        return delete;
    }


    public void setDelete( boolean delete )
    {
        this.delete = delete;
    }


    @Override
    public String getObjectName()
    {
        return roleObject;
    }


    @Override
    public void setObjectName( String obj )
    {
        this.roleObject = obj;
    }


    public Long getId()
    {
        return id;
    }


    public void setId( Long id )
    {
        this.id = id;
    }


    public int getPermObject()
    {
        return permObject;
    }


    public void setPermObject( int permObject )
    {
        this.permObject = permObject;
    }


    @Override
    public List<String> asString()
    {
        List<String> perms = new ArrayList<>();

        if ( PermissionObject.values()[permObject - 1] == PermissionObject.KarafServerAdministration )
        {
            perms.add( "admin" );
        }
        else if ( PermissionObject.values()[permObject - 1] == PermissionObject.KarafServerManagement )
        {
            perms.add( "manager" );
        }
        else
        {
            String permString = "";

            permString += ( PermissionObject.values() )[permObject - 1].getName() + "|A|";
            //permString +="|"+(PermissionScope.values())[scope-1].getName()+"|";

            if ( read )
            {
                perms.add( permString + "Read" );
            }
            if ( write )
            {
                perms.add( permString + "Write" );
            }
            if ( update )
            {
                perms.add( permString + "Update" );
            }
            if ( delete )
            {
                perms.add( permString + "Delete" );
            }
        }


        return perms;
    }
}
