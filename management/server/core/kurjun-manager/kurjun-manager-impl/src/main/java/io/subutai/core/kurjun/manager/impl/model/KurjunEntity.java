package io.subutai.core.kurjun.manager.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.core.kurjun.manager.api.model.Kurjun;


/**
 * Implementation of Kurjun interface. Used for storing kurjun data.
 */
@Entity
@Table( name = "kurjun_data" )
@Access( AccessType.FIELD )
public class KurjunEntity implements Kurjun
{

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id" )
    private long id;

    @Column( name = "url" )
    private String url;

    @Column( name = "state" )
    private boolean state;

    @Column( name = "type" )
    private int type = KurjunType.Local.getId();


    @Override
    public long getId()
    {
        return id;
    }


    @Override
    public void setId( final long id )
    {
        this.id = id;
    }


    @Override
    public int getType()
    {
        return type;
    }


    @Override
    public void setType( final int type )
    {
        this.type = type;
    }


    public String getUrl()
    {
        return url;
    }


    public void setUrl( final String url )
    {
        this.url = url;
    }


    public boolean getState()
    {
        return state;
    }


    public void setState( final boolean state )
    {
        this.state = state;
    }
}
