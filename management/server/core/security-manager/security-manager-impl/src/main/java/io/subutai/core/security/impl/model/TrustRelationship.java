package io.subutai.core.security.impl.model;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Created by talas on 12/8/15.
 */
@Entity
@Table( name = "trust_relationship" )
@Access( AccessType.FIELD )
public class TrustRelationship implements Serializable
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "relationship_id" )
    private long id;

    @Column( name = "trust_level" )
    private String trustLevel;

    @Column( name = "scope" )
    private String scope;

    @Column( name = "action" )
    private String action;

    @Column( name = "ttl" )
    private String ttl;

    @Column( name = "type" )
    private String type;


    public TrustRelationship( final String trustLevel, final String scope, final String action, final String ttl,
                              final String type )
    {
        this.trustLevel = trustLevel;
        this.scope = scope;
        this.action = action;
        this.ttl = ttl;
        this.type = type;
    }


    public long getId()
    {
        return id;
    }


    public String getTrustLevel()
    {
        return trustLevel;
    }


    public String getScope()
    {
        return scope;
    }


    public String getAction()
    {
        return action;
    }


    public String getTtl()
    {
        return ttl;
    }


    public String getType()
    {
        return type;
    }
}
