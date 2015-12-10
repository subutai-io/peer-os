package io.subutai.core.identity.impl.model;


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

import io.subutai.core.identity.api.model.RelationInfo;


/**
 * Created by talas on 12/8/15.
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

    //Permission, role
    @Column( name = "type" )
    private String type = "";


    public RelationInfoImpl()
    {
    }


    public RelationInfoImpl( final RelationInfo relationInfo )
    {
        this.context = relationInfo.getContext();
        this.operation = relationInfo.getOperation();
        this.type = relationInfo.getType();
    }


    public RelationInfoImpl( final String context, final Set<String> operation, final String type )
    {
        this.context = context;
        this.operation = operation;
        this.type = type;
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
    public String getType()
    {
        return type;
    }


    public void setContext( final String context )
    {
        this.context = context;
    }


    public void setOperation( final Set<String> operation )
    {
        this.operation = operation;
    }


    public void setType( final String type )
    {
        this.type = type;
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

        if ( context != null ? !context.equals( that.context ) : that.context != null )
        {
            return false;
        }
        if ( operation != null ? !operation.equals( that.operation ) : that.operation != null )
        {
            return false;
        }
        return !( type != null ? !type.equals( that.type ) : that.type != null );
    }


    @Override
    public int hashCode()
    {
        int result = context != null ? context.hashCode() : 0;
        result = 31 * result + ( operation != null ? operation.hashCode() : 0 );
        result = 31 * result + ( type != null ? type.hashCode() : 0 );
        return result;
    }
}
