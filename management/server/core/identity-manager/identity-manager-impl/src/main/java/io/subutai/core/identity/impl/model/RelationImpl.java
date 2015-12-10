package io.subutai.core.identity.impl.model;


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

import io.subutai.core.identity.api.model.Relation;


/**
 * Created by talas on 12/8/15.
 */
@Entity
@Table( name = "relation" )
@Access( AccessType.FIELD )
public class RelationImpl implements Relation
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "relation_id" )
    private long id;

    @Column( name = "source_link" )
    @ManyToOne( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    private RelationLinkImpl source;

    @Column( name = "target_link" )
    @ManyToOne( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    private RelationLinkImpl target;

    @Column( name = "trusted_object_link" )
    @ManyToOne( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    private RelationLinkImpl trustedObject;

    @Column( name = "relation_info" )
    @OneToOne( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    private RelationInfoImpl relationship;


    public RelationImpl()
    {
    }


    public RelationImpl( final RelationLinkImpl source, final RelationLinkImpl target,
                         final RelationLinkImpl trustedObject, final RelationInfoImpl relationship )
    {
        this.source = source;
        this.target = target;
        this.trustedObject = trustedObject;
        this.relationship = relationship;
    }


    @Override
    public long getId()
    {
        return id;
    }


    @Override
    public RelationLinkImpl getSource()
    {
        return source;
    }


    @Override
    public RelationLinkImpl getTarget()
    {
        return target;
    }


    @Override
    public RelationLinkImpl getTrustedObject()
    {
        return trustedObject;
    }


    @Override
    public RelationInfoImpl getRelationInfo()
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
        if ( !( o instanceof RelationImpl ) )
        {
            return false;
        }

        final RelationImpl that = ( RelationImpl ) o;

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
