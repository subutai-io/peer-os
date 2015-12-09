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
@Table( name = "trust_item" )
@Access( AccessType.FIELD )
public class TrustItem implements Serializable
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "item_id" )
    private long id;

    @Column( name = "unique_identifier" )
    private String uniqueIdentifier;

    @Column( name = "class_path" )
    private String classPath;


    public TrustItem()
    {
    }


    public TrustItem( final String uniqueIdentifier, final String classPath )
    {
        this.uniqueIdentifier = uniqueIdentifier;
        this.classPath = classPath;
    }


    public long getId()
    {
        return id;
    }


    public String getUniqueIdentifier()
    {
        return uniqueIdentifier;
    }


    public String getClassPath()
    {
        return classPath;
    }
}
