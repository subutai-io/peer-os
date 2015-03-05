package org.safehaus.subutai.core.channel.impl.entity;


import java.sql.Timestamp;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.safehaus.subutai.core.channel.api.entity.IUserChannelToken;

/**
 * Created by nisakov on 3/3/15.
 */


@Entity
@Table( name = "user_channel_token" )
@Access( AccessType.FIELD )

public class UserChannelToken implements IUserChannelToken
{
    @Id
    @Column( name = "user_id" )
    private Long userId;

    @Column(name = "valid_period")
    private short validPeriod = 0;

    @Column(name = "status")
    private short status;

    @Column(name = "ip_range_start")
    private String ipRangeStart;

    @Column(name = "ip_range_end")
    private String ipRangeEnd;

    @Column( name = "token")
    private String token;

    @Column (name = "date")
    private Timestamp date;


    /************************************************************************
     *
     */
    public Long getUserId()
    {
        return userId;
    }


    public void setUserId( final Long userId )
    {
        this.userId = userId;
    }

    public short getStatus()
    {
        return status;
    }

    public void setStatus( short status )
    {
        this.status = status;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken( String token )
    {
        this.token = token;
    }

    public Timestamp getDate()
    {
        return date;
    }

    public void setDate( Timestamp date )
    {
        this.date = date;
    }

    public short getValidPeriod()
    {
        return validPeriod;
    }

    public void setValidPeriod( short validPeriod )
    {
        this.validPeriod = validPeriod;
    }


    public String getIpRangeStart()
    {
        return ipRangeStart;
    }


    public void setIpRangeStart( final String ipRangeStart )
    {
        this.ipRangeStart = ipRangeStart;
    }


    public String getIpRangeEnd()
    {
        return ipRangeEnd;
    }


    public void setIpRangeEnd( final String ipRangeEnd )
    {
        this.ipRangeEnd = ipRangeEnd;
    }
}
