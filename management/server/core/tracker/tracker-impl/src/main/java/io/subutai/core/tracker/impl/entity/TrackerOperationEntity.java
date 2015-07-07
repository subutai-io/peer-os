package io.subutai.core.tracker.impl.entity;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


@Entity
@Table( name = "tracker_operation" )
@Access( AccessType.FIELD )
@IdClass( TrackerOperationPK.class )
@NamedQueries( {
        @NamedQuery( name = "getTrackerOperation", query = "SELECT to FROM TrackerOperationEntity to WHERE to.source "
                + "= :source AND to.operationTrackId = :operationTrackId" )
} )
public class TrackerOperationEntity
{

    public static final String QUERY_GET_OPERATION = "getTrackerOperation";

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


    public TrackerOperationEntity() {}


    public String getInfo()
    {
        return info;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof TrackerOperationEntity ) )
        {
            return false;
        }

        final TrackerOperationEntity entity = ( TrackerOperationEntity ) o;

        return operationTrackId.equals( entity.operationTrackId ) && source.equals( entity.source );
    }


    @Override
    public int hashCode()
    {
        int result = source.hashCode();
        result = 31 * result + operationTrackId.hashCode();
        return result;
    }
}
