package org.safehaus.subutai.core.channel.api.entity;


import java.sql.Timestamp;


public interface IUserChannelToken
{
    public Long getUserId();

    public void setUserId( final Long userId );

    public short getStatus();

    public void setStatus( short status );

    public String getToken();

    public void setToken( String token );

    public Timestamp getDate();

    public void setDate( Timestamp date );

    public short getValidPeriod();

    public void setValidPeriod( short validPeriod );

    public String getIpRangeStart();

    public void setIpRangeStart( final String ipRangeStart );

    public String getIpRangeEnd();

    public void setIpRangeEnd( final String ipRangeEnd );

    public String getTokenName();

    public void setTokenName( final String tokenName );
}
