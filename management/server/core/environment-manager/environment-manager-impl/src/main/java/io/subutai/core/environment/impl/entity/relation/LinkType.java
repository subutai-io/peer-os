package io.subutai.core.environment.impl.entity.relation;


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
 * Created by talas on 1/11/16.
 */
@Entity
@Table( name = "link_type" )
@Access( AccessType.FIELD )
public class LinkType implements Serializable
{
    // Try to use enums instead of classes
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id" )
    private long id;

    @Column( name = "name" )
    private String name;

    @Column( name = "inward" )
    private String inward;

    @Column( name = "outward" )
    private String outward;


    public LinkType()
    {
    }


    public long getId()
    {
        return id;
    }


    public void setId( final long id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public String getInward()
    {
        return inward;
    }


    public void setInward( final String inward )
    {
        this.inward = inward;
    }


    public String getOutward()
    {
        return outward;
    }


    public void setOutward( final String outward )
    {
        this.outward = outward;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof LinkType ) )
        {
            return false;
        }

        final LinkType linkType = ( LinkType ) o;

        return id == linkType.id;
    }


    @Override
    public int hashCode()
    {
        return ( int ) ( id ^ ( id >>> 32 ) );
    }
}
