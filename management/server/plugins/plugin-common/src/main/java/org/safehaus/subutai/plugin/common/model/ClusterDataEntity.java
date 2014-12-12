package org.safehaus.subutai.plugin.common.model;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;


/**
 * Created by talas on 12/9/14.
 */
@Entity
@Access( AccessType.FIELD )
@Table( name = "cluster_data" )
@IdClass( ClusterDataEntityPK.class )
public class ClusterDataEntity implements Serializable
{
    @Id
    @Column( name = "source" )
    private String source;

    @Id
    @Column( name = "cluster_id" )
    private String id;

    @Lob
    @Column( name = "info" )
    private String info;


    public ClusterDataEntity( final String source, final String key, final String info )
    {
        this.source = source;
        this.id = key;
        this.info = info;
    }


    public String getInfo()
    {
        return info;
    }
}
