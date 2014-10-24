package org.safehaus.subutai.core.db.ext.api.entity;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table( name = "user" )

public class User implements Serializable
{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id" )
    private long id;

    @Column( name = "type" )
    private short type;


    public User()
    {

    }


    public long getId()
    {
        return id;
    }


    public void setId( long id )
    {
        this.id = id;
    }


    public short getType()
    {
        return type;
    }


    public void setType( short type )
    {
        this.type = type;
    }
}