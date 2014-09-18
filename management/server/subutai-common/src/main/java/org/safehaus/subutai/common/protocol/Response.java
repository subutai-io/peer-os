package org.safehaus.subutai.common.protocol;


import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.enums.ResponseType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class Response implements Serializable {

    private String source;
    private ResponseType type;
    private Integer exitCode; //might be null if not final response chunk
    private UUID uuid;
    private UUID taskUuid;
    private Integer requestSequenceNumber;
    private Integer responseSequenceNumber;
    private String stdOut;
    private String stdErr;
    private Integer pid;
    private String macAddress;
    private String hostname;
    private String parentHostName;
    private List<String> ips;
    private Boolean isLxc;
    private String transportId;
    @SkipNull
    private UUID environmentId;


    // inotify fields
    private String confPoints[];
    private String configPoint;
    private String changeType;
    private String dateTime;


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public String getParentHostName()
    {
        return parentHostName;
    }


    public String getTransportId()
    {
        return transportId;
    }


    public void setTransportId( String transportId )
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( transportId ), "Transport id is null or empty" );

        this.transportId = transportId;
    }


    public Boolean isLxc()
    {
        return isLxc;
    }


    public List<String> getIps()
    {
        return ips;
    }


    public String getMacAddress()
    {
        return macAddress;
    }


    public String getHostname()
    {
        return hostname;
    }


    public String getSource()
    {
        return source;
    }


    public ResponseType getType()
    {
        return type;
    }


    public void setType( ResponseType type )
    {

        Preconditions.checkNotNull( type, "Response type is null" );

        this.type = type;
    }


    public Integer getExitCode()
    {
        return exitCode;
    }


    public UUID getUuid()
    {
        return uuid;
    }


    public Integer getRequestSequenceNumber()
    {
        return requestSequenceNumber;
    }


    public Integer getResponseSequenceNumber()
    {
        return responseSequenceNumber;
    }


    public String getStdOut()
    {
        return stdOut;
    }


    public String getStdErr()
    {
        return stdErr;
    }


    public Integer getPid()
    {
        return pid;
    }


    public UUID getTaskUuid()
    {
        return taskUuid;
    }


    public String[] getConfPoints()
    {
        return confPoints;
    }


    public String getChangeType()
    {
        return changeType;
    }


    public String getConfigPoint()
    {
        return configPoint;
    }


    public String getDateTime()
    {
        return dateTime;
    }


    public boolean isFinal()
    {
        return ResponseType.EXECUTE_RESPONSE_DONE.equals( type ) || ResponseType.EXECUTE_TIMEOUT.equals( type )
                || ResponseType.TERMINATE_RESPONSE_DONE.equals( type ) || ResponseType.TERMINATE_RESPONSE_FAILED
                .equals( type );
    }


    public boolean hasSucceeded()
    {
        return ( ResponseType.EXECUTE_RESPONSE_DONE.equals( type ) || ResponseType.TERMINATE_RESPONSE_DONE
                .equals( type ) ) && exitCode != null && exitCode == 0;
    }


    @Override
    public String toString()
    {
        return new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE ).append( "source", source )
                                                                            .append( "type", type )
                                                                            .append( "exitCode", exitCode )
                                                                            .append( "uuid", uuid )
                                                                            .append( "taskUuid", taskUuid )
                                                                            .append( "requestSequenceNumber",
                                                                                    requestSequenceNumber )
                                                                            .append( "stdOut", stdOut )
                                                                            .append( "stdErr", stdErr )
                                                                            .append( "pid", pid )
                                                                            .append( "macAddress", macAddress )
                                                                            .append( "hostname", hostname )
                                                                            .append( "ips", ips )
                                                                            .append( "isLxc", isLxc )
                                                                            .append( "transportId", transportId )
                                                                            .append( "environmentId", environmentId )
                                                                            .append( "confPoints", confPoints )
                                                                            .append( "changeType", changeType )
                                                                            .append( "configPoint", configPoint )
                                                                            .append( "dateTime", dateTime ).toString();
    }
}
