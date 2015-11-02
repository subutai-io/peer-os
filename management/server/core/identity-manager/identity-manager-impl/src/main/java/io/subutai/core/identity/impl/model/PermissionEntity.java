package io.subutai.core.identity.impl.model;


import java.util.ArrayList;
import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.common.security.objects.PermissionObject;
import io.subutai.core.identity.api.model.Permission;


/**
 *
 */
@Entity
@Table( name = "permission" )
@Access( AccessType.FIELD )
public class PermissionEntity implements Permission
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id" )
    private long id;

    @Column( name = "object" )
    private int object;

    @Column( name = "scope" )
    private int scope  = 1;

    @Column( name = "read" )
    private boolean read = false;

    @Column( name = "write" )
    private boolean write = false;

    @Column( name = "update" )
    private boolean update = false;

    @Column( name = "delete" )
    private boolean delete = false;


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
    public int getObject()
    {
        return object;
    }


    @Override
    public void setObject( int object )
    {
        this.object = object;
    }


    @Override
    public int getScope()
    {
        return scope;
    }


    @Override
    public void setScope( int scope )
    {
        this.scope = scope;
    }


    @Override
    public boolean isRead()
    {
        return read;
    }


    @Override
    public void setRead( boolean read )
    {
        this.read = read;
    }


    @Override
    public boolean isWrite()
    {
        return write;
    }


    @Override
    public void setWrite( boolean write )
    {
        this.write = write;
    }


    @Override
    public boolean isUpdate()
    {
        return update;
    }


    @Override
    public void setUpdate( boolean update )
    {
        this.update = update;
    }


    @Override
    public boolean isDelete()
    {
        return delete;
    }


    @Override
    public void setDelete( boolean delete )
    {
        this.delete = delete;
    }

    @Override
    public String getObjectName()
    {
        return PermissionObject.values()[object-1].getName();
    }

    @Override
    public List<String> asString()
    {
        List<String> perms = new ArrayList<>();

        if(PermissionObject.values()[object-1] == PermissionObject.KarafServerAdministration)
        {
            perms.add( "admin" );
        }
        else if(PermissionObject.values()[object-1] == PermissionObject.KarafServerManagement)
        {
            perms.add( "manager" );
        }
        else
        {
            String permString = "";

            permString +=( PermissionObject.values())[object-1].getName()+"|A|";
            //permString +="|"+(PermissionScope.values())[scope-1].getName()+"|";

            if(read)
                perms.add( permString+"Read" );
            if(write)
                perms.add( permString+"Write" );
            if(update)
                perms.add( permString+"Update" );
            if(delete)
                perms.add( permString+"Delete" );
        }


        return perms;
    }

}