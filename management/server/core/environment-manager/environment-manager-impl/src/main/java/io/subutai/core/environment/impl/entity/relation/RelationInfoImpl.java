package io.subutai.core.environment.impl.entity.relation;


import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.collect.Sets;

import io.subutai.common.security.objects.Ownership;
import io.subutai.core.identity.api.model.RelationInfo;
import io.subutai.core.identity.api.model.RelationInfoMeta;


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

    // condition
    @Column( name = "context" )
    private String context = "";

    //read, write, delete, update
    @Column( name = "operation" )
    @ElementCollection( targetClass = String.class, fetch = FetchType.EAGER )
    private Set<String> operation = Sets.newHashSet();

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


    public RelationInfoImpl( final RelationInfo relationInfo )
    {
        this.context = relationInfo.getContext();
        this.operation = relationInfo.getOperation();
        this.ownershipLevel = relationInfo.getOwnershipLevel();
    }


    @Deprecated
    public RelationInfoImpl( final String context, final Set<String> operation, final int ownershipLevel )
    {
        this.context = context;
        this.operation = operation;
        this.ownershipLevel = ownershipLevel;
    }


    public RelationInfoImpl( final RelationInfoMeta relationInfoMeta )
    {
        this.context = relationInfoMeta.getContext();
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
    public String getContext()
    {
        return context;
    }


    @Override
    public Set<String> getOperation()
    {
        return operation;
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


    public void setOperation( final Set<String> operation )
    {
        this.operation = operation;
    }


    public void setContext( final String context )
    {
        this.context = context;
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
