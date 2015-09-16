package io.subutai.core.environment.impl.entity;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.PeerConf;
import io.subutai.common.protocol.N2NConfig;


@Entity
@Table( name = "environment_peer" )
@Access( AccessType.FIELD )
public class PeerConfImpl implements PeerConf, Serializable
{
    @Id
    @Column( name = "id" )
    @GeneratedValue( strategy = GenerationType.AUTO )
    private Long id;

    @ManyToOne( targetEntity = EnvironmentImpl.class )
    @JoinColumn( name = "environment_id" )
    private Environment environment;


    @Embedded
    private N2NConfig n2NConfig;


    public Long getId()
    {
        return id;
    }


    public void setId( final Long id )
    {
        this.id = id;
    }


    public Environment getEnvironment()
    {
        return environment;
    }


    public void setEnvironment( final Environment environment )
    {
        this.environment = environment;
    }


    @Override
    public N2NConfig getN2NConfig()
    {
        return n2NConfig;
    }


    public void setN2NConfig( final N2NConfig n2NConfig )
    {
        this.n2NConfig = n2NConfig;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "PeerConfImpl{" );
        sb.append( "environment=" ).append( environment );
        sb.append( ", n2NConfig=" ).append( n2NConfig );
        sb.append( '}' );
        return sb.toString();
    }
}
