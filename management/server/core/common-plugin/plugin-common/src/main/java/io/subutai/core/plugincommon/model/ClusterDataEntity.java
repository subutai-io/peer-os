package io.subutai.core.plugincommon.model;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;


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

    @Column( name = "user_id" )
    private Long userId;


    public ClusterDataEntity( final String source, final String key, final String info, final Long userId )
    {
        this.source = source;
        this.id = key;
        this.info = info;
        this.userId = userId;
    }


    public String getInfo()
    {
        return info;
    }


    public Long getUserId() {
        return userId;
    }
}
