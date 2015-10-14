package io.subutai.core.identity.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.core.identity.api.model.PermissionOperation;


/**
 *
 */
@Entity
@Table( name = "permission_operation" )
@Access( AccessType.FIELD )
public class PermissionOperationEntity  implements PermissionOperation
{
    @Id
    @GeneratedValue
    @Column( name = "id" )
    private Long id;

    @Column( name = "scope" )
    private short scope;

    @Column( name = "read" )
    private short read;

    @Column( name = "write" )
    private short write;

    @Column( name = "update" )
    private short update;

    @Column( name = "delete" )
    private short delete;


    public Long getId()
    {
        return id;
    }


    public void setId( final Long id )
    {
        this.id = id;
    }


    public short getScope()
    {
        return scope;
    }


    public void setScope( final short scope )
    {
        this.scope = scope;
    }


    public short getRead()
    {
        return read;
    }


    public void setRead( final short read )
    {
        this.read = read;
    }


    public short getWrite()
    {
        return write;
    }


    public void setWrite( final short write )
    {
        this.write = write;
    }


    public short getUpdate()
    {
        return update;
    }


    public void setUpdate( final short update )
    {
        this.update = update;
    }


    public short getDelete()
    {
        return delete;
    }


    public void setDelete( final short delete )
    {
        this.delete = delete;
    }
}
