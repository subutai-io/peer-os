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
    @Column( name = "scope" )
    private String scope = "";

    //read, write, delete, update
    @Column( name = "action" )
    private String action = "";

    @Column( name = "ttl" )
    private String ttl = "";

    //Permission, role
    @Column( name = "type" )
    private String type = "";


    public TrustRelationshipImpl()
    {
    }


    public TrustRelationshipImpl( final String trustLevel, final String scope, final String action, final String ttl,
                                  final String type )
    {
        this.trustLevel = trustLevel;
        this.scope = scope;
        this.action = action;
        this.ttl = ttl;
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
    public String getScope()
    {
        return scope;
    }


    @Override
    public String getAction()
    {
        return action;
    }


    @Override
    public String getTtl()
    {
        return ttl;
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


    public void setScope( final String scope )
    {
        this.scope = scope;
    }


    public void setAction( final String action )
    {
        this.action = action;
    }


    public void setTtl( final String ttl )
    {
        this.ttl = ttl;
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
        if ( scope != null ? !scope.equals( that.scope ) : that.scope != null )
        {
            return false;
        }
        if ( action != null ? !action.equals( that.action ) : that.action != null )
        {
            return false;
        }
        if ( ttl != null ? !ttl.equals( that.ttl ) : that.ttl != null )
        {
            return false;
        }
        return !( type != null ? !type.equals( that.type ) : that.type != null );
    }


    @Override
    public int hashCode()
    {
        int result = trustLevel != null ? trustLevel.hashCode() : 0;
        result = 31 * result + ( scope != null ? scope.hashCode() : 0 );
        result = 31 * result + ( action != null ? action.hashCode() : 0 );
        result = 31 * result + ( ttl != null ? ttl.hashCode() : 0 );
        result = 31 * result + ( type != null ? type.hashCode() : 0 );
        return result;
    }
}
