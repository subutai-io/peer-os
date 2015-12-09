package io.subutai.core.security.impl.model;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;


/**
 * Created by talas on 12/8/15.
 */
@Entity
@Table( name = "trust_relation" )
@Access( AccessType.FIELD )
public class TrustRelation implements Serializable
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "relation_id" )
    private long id;

    @Column( name = "source_item" )
    @OneToOne( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    private TrustItem source;

    @Column( name = "target_item" )
    @OneToOne( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    private TrustItem target;

    @Column( name = "trust_object" )
    @OneToOne( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    private TrustItem trustedObject;

    @Column( name = "relationship" )
    @OneToOne( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
    private TrustRelationship relationship;


    public TrustRelation()
    {
    }


    public TrustRelation( final TrustItem source, final TrustItem target, final TrustItem trustedObject,
                          final TrustRelationship relationship )
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
}
