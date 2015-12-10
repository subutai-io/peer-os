package io.subutai.core.security.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import io.subutai.core.security.api.model.TrustItem;
import io.subutai.core.security.api.model.TrustRelation;
import io.subutai.core.security.api.model.TrustRelationship;


/**
 * Created by talas on 12/8/15.
 */
@Entity
@Table( name = "trust_relation" )
@Access( AccessType.FIELD )
public class TrustRelationImpl implements TrustRelation
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "relation_id" )
    private long id;

    @Column( name = "source_item" )
    @ManyToOne( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    private TrustItemImpl source;

    @Column( name = "target_item" )
    @ManyToOne( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    private TrustItemImpl target;

    @Column( name = "trust_object" )
    @ManyToOne( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    private TrustItemImpl trustedObject;

    @Column( name = "relationship" )
    @OneToOne( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    private TrustRelationshipImpl relationship;


    public TrustRelationImpl()
    {
    }


    public TrustRelationImpl( final TrustItemImpl source, final TrustItemImpl target, final TrustItemImpl trustedObject,
                              final TrustRelationshipImpl relationship )
    {
        this.source = source;
        this.target = target;
        this.trustedObject = trustedObject;
        this.relationship = relationship;
    }


    public long getId()
    {
        return id;
    }


    public TrustItem getSource()
    {
        return source;
    }


    public TrustItem getTarget()
    {
        return target;
    }


    public TrustItem getTrustedObject()
    {
        return trustedObject;
    }


    public TrustRelationship getRelationship()
    {
        return relationship;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof TrustRelationImpl ) )
        {
            return false;
        }

        final TrustRelationImpl that = ( TrustRelationImpl ) o;

        if ( source != null ? !source.equals( that.source ) : that.source != null )
        {
            return false;
        }
        if ( target != null ? !target.equals( that.target ) : that.target != null )
        {
            return false;
        }
        return !( trustedObject != null ? !trustedObject.equals( that.trustedObject ) : that.trustedObject != null );
    }


    @Override
    public int hashCode()
    {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + ( target != null ? target.hashCode() : 0 );
        result = 31 * result + ( trustedObject != null ? trustedObject.hashCode() : 0 );
        return result;
    }
}
