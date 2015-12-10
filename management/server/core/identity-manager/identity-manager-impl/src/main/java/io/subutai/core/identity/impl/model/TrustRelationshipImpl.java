package io.subutai.core.identity.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.core.identity.api.model.TrustRelationship;


/**
 * Created by talas on 12/8/15.
 */
@Entity
@Table( name = "trust_relationship" )
@Access( AccessType.FIELD )
public class TrustRelationshipImpl implements TrustRelationship
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "relationship_id" )
    private long id;

    @Column( name = "trust_level" )
    private String trustLevel = "";

    // condition
    @Column( name = "context" )
    private String context = "";

    //read, write, delete, update
    @Column( name = "operation" )
    private String operation = "";

    //Permission, role
    @Column( name = "type" )
    private String type = "";


    public TrustRelationshipImpl()
    {
    }


    public TrustRelationshipImpl( final String trustLevel, final String context, final String operation,
                                  final String ttl, final String type )
    {
        this.trustLevel = trustLevel;
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
    public String getTrustLevel()
    {
        return trustLevel;
    }


    @Override
    public String getContext()
    {
        return context;
    }


    @Override
    public String getOperation()
    {
        return operation;
    }


    @Override
    public String getType()
    {
        return type;
    }


    public void setTrustLevel( final String trustLevel )
    {
        this.trustLevel = trustLevel;
    }


    public void setContext( final String context )
    {
        this.context = context;
    }


    public void setOperation( final String operation )
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
        if ( !( o instanceof TrustRelationshipImpl ) )
        {
            return false;
        }

        final TrustRelationshipImpl that = ( TrustRelationshipImpl ) o;

        if ( trustLevel != null ? !trustLevel.equals( that.trustLevel ) : that.trustLevel != null )
        {
            return false;
        }
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
        int result = trustLevel != null ? trustLevel.hashCode() : 0;
        result = 31 * result + ( context != null ? context.hashCode() : 0 );
        result = 31 * result + ( operation != null ? operation.hashCode() : 0 );
        result = 31 * result + ( type != null ? type.hashCode() : 0 );
        return result;
    }
}
