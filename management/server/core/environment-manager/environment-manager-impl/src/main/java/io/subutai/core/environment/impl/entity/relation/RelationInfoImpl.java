package io.subutai.core.environment.impl.entity.relation;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.common.security.objects.Ownership;
import io.subutai.common.security.relation.model.RelationInfo;
import io.subutai.common.security.relation.model.RelationInfoMeta;


/**
 * Created by talas on 12/8/15.
 */


/**
 * Relation info is simple string presentation of propertyKey=propertyValue where each pair will describe relation with
 * other object. When verifying transitive relation, relation validity is checked upon key=pair existence.
 */
@Entity
@Table( name = "relation_info" )
@Access( AccessType.FIELD )
public class RelationInfoImpl implements RelationInfo
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "relation_info_id" )
    private long id;

    @Column( name = "read_p" )
    private boolean readPermission;

    @Column( name = "write_p" )
    private boolean writePermission;

    @Column( name = "update_p" )
    private boolean updatePermission;

    @Column( name = "delete_p" )
    private boolean deletePermission;

    //Permission, role
    @Column( name = "ownership_level" )
    private int ownershipLevel = Ownership.ALL.getLevel();


    public RelationInfoImpl()
    {
    }


    public RelationInfoImpl( final RelationInfoMeta relationInfoMeta )
    {
        this.readPermission = relationInfoMeta.isReadPermission();
        this.writePermission = relationInfoMeta.isWritePermission();
        this.updatePermission = relationInfoMeta.isUpdatePermission();
        this.deletePermission = relationInfoMeta.isDeletePermission();
        this.ownershipLevel = relationInfoMeta.getOwnershipLevel();
    }


    @Override
    public long getId()
    {
        return id;
    }


    @Override
    public int getOwnershipLevel()
    {
        return ownershipLevel;
    }


    public void setOwnershipLevel( final int ownershipLevel )
    {
        this.ownershipLevel = ownershipLevel;
    }


    public boolean isReadPermission()
    {
        return readPermission;
    }


    public boolean isWritePermission()
    {
        return writePermission;
    }


    public boolean isUpdatePermission()
    {
        return updatePermission;
    }


    public boolean isDeletePermission()
    {
        return deletePermission;
    }


    public void setReadPermission( final boolean readPermission )
    {
        this.readPermission = readPermission;
    }


    public void setWritePermission( final boolean writePermission )
    {
        this.writePermission = writePermission;
    }


    public void setUpdatePermission( final boolean updatePermission )
    {
        this.updatePermission = updatePermission;
    }


    public void setDeletePermission( final boolean deletePermission )
    {
        this.deletePermission = deletePermission;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof RelationInfoImpl ) )
        {
            return false;
        }

        final RelationInfoImpl that = ( RelationInfoImpl ) o;

        return id == that.id;
    }


    @Override
    public int hashCode()
    {
        return ( int ) ( id ^ ( id >>> 32 ) );
    }
}
