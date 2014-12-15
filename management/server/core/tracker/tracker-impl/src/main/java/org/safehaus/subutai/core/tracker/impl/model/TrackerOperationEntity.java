package org.safehaus.subutai.core.tracker.impl.model;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;


/**
 * Created by talas on 12/7/14.
 */
@Entity
@Table( name = "tracker_operation" )
@Access( AccessType.FIELD )
@IdClass( TrackerOperationPK.class )
public class TrackerOperationEntity
{
    //source varchar(100), " +
    //    "id uuid, ts timestamp, "
    //            + "info clob, PRIMARY KEY (source, id))

    @Id
    @Column( name = "source_id" )
    private String source;
    @Id
    @Column( name = "operation_track_id" )
    private String operationTrackId;

    @Column( name = "ts" )
    private Long ts;

    @Lob
    @Column( name = "info" )
    private String info;


    public TrackerOperationEntity( final String source, final String id, final Long ts, final String info )
    {
        this.source = source;
        this.operationTrackId = id;
        this.ts = ts;
        this.info = info;
    }


    public String getInfo()
    {
        return info;
    }
}
