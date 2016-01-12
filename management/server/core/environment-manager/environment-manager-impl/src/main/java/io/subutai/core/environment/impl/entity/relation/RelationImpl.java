package io.subutai.core.environment.impl.entity.relation;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import io.subutai.common.security.relation.model.Relation;
import io.subutai.common.security.relation.model.RelationStatus;


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
    private RelationInfoImpl relationInfo;

    @Enumerated( EnumType.STRING )
    @Column( name = "status", nullable = false )
    private RelationStatus relationStatus;

    /**
     * Public key id to verify signed message
     */
    @Column( name = "signature_key_id" )
    private String keyId;

    @Column( name = "link_type" )
    @ManyToOne( fetch = FetchType.EAGER, cascade = CascadeType.ALL )
    private LinkType linkType;


    public RelationImpl()
    {
    }


    public RelationImpl( final RelationLinkImpl source, final RelationLinkImpl target,
                         final RelationLinkImpl trustedObject, final RelationInfoImpl relationInfo, final String keyId )
    {
        this.source = source;
        this.target = target;
        this.trustedObject = trustedObject;
        this.relationInfo = relationInfo;
        this.relationStatus = RelationStatus.REQUESTED;
        this.keyId = keyId;
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
        return relationInfo;
    }


    @Override
    public RelationStatus getRelationStatus()
    {
        return relationStatus;
    }


    @Override
    public String getKeyId()
    {
        return keyId;
    }


    public LinkType getLinkType()
    {
        return linkType;
    }


    public void setRelationStatus( final RelationStatus relationStatus )
    {
        this.relationStatus = relationStatus;
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

        final RelationImpl relation = ( RelationImpl ) o;

        if ( source != null ? !source.equals( relation.source ) : relation.source != null )
        {
            return false;
        }
        if ( target != null ? !target.equals( relation.target ) : relation.target != null )
        {
            return false;
        }
        if ( trustedObject != null ? !trustedObject.equals( relation.trustedObject ) : relation.trustedObject != null )
        {
            return false;
        }
        return !( keyId != null ? !keyId.equals( relation.keyId ) : relation.keyId != null );
    }


    @Override
    public int hashCode()
    {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + ( target != null ? target.hashCode() : 0 );
        result = 31 * result + ( trustedObject != null ? trustedObject.hashCode() : 0 );
        result = 31 * result + ( keyId != null ? keyId.hashCode() : 0 );
        return result;
    }
}
