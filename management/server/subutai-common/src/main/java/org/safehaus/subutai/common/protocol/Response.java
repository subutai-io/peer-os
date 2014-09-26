package org.safehaus.subutai.common.protocol;


import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.enums.ResponseType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class Response implements Serializable
{

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


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof Response ) )
        {
            return false;
        }

        final Response response = ( Response ) o;

        if ( changeType != null ? !changeType.equals( response.changeType ) : response.changeType != null )
        {
            return false;
        }
        if ( !Arrays.equals( confPoints, response.confPoints ) )
        {
            return false;
        }
        if ( configPoint != null ? !configPoint.equals( response.configPoint ) : response.configPoint != null )
        {
            return false;
        }
        if ( dateTime != null ? !dateTime.equals( response.dateTime ) : response.dateTime != null )
        {
            return false;
        }
        if ( environmentId != null ? !environmentId.equals( response.environmentId ) : response.environmentId != null )
        {
            return false;
        }
        if ( exitCode != null ? !exitCode.equals( response.exitCode ) : response.exitCode != null )
        {
            return false;
        }
        if ( hostname != null ? !hostname.equals( response.hostname ) : response.hostname != null )
        {
            return false;
        }
        if ( ips != null ? !ips.equals( response.ips ) : response.ips != null )
        {
            return false;
        }
        if ( isLxc != null ? !isLxc.equals( response.isLxc ) : response.isLxc != null )
        {
            return false;
        }
        if ( macAddress != null ? !macAddress.equals( response.macAddress ) : response.macAddress != null )
        {
            return false;
        }
        if ( parentHostName != null ? !parentHostName.equals( response.parentHostName ) :
             response.parentHostName != null )
        {
            return false;
        }
        if ( pid != null ? !pid.equals( response.pid ) : response.pid != null )
        {
            return false;
        }
        if ( requestSequenceNumber != null ? !requestSequenceNumber.equals( response.requestSequenceNumber ) :
             response.requestSequenceNumber != null )
        {
            return false;
        }
        if ( responseSequenceNumber != null ? !responseSequenceNumber.equals( response.responseSequenceNumber ) :
             response.responseSequenceNumber != null )
        {
            return false;
        }
        if ( source != null ? !source.equals( response.source ) : response.source != null )
        {
            return false;
        }
        if ( stdErr != null ? !stdErr.equals( response.stdErr ) : response.stdErr != null )
        {
            return false;
        }
        if ( stdOut != null ? !stdOut.equals( response.stdOut ) : response.stdOut != null )
        {
            return false;
        }
        if ( taskUuid != null ? !taskUuid.equals( response.taskUuid ) : response.taskUuid != null )
        {
            return false;
        }
        if ( transportId != null ? !transportId.equals( response.transportId ) : response.transportId != null )
        {
            return false;
        }
        if ( type != response.type )
        {
            return false;
        }
        if ( uuid != null ? !uuid.equals( response.uuid ) : response.uuid != null )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + ( type != null ? type.hashCode() : 0 );
        result = 31 * result + ( exitCode != null ? exitCode.hashCode() : 0 );
        result = 31 * result + ( uuid != null ? uuid.hashCode() : 0 );
        result = 31 * result + ( taskUuid != null ? taskUuid.hashCode() : 0 );
        result = 31 * result + ( requestSequenceNumber != null ? requestSequenceNumber.hashCode() : 0 );
        result = 31 * result + ( responseSequenceNumber != null ? responseSequenceNumber.hashCode() : 0 );
        result = 31 * result + ( stdOut != null ? stdOut.hashCode() : 0 );
        result = 31 * result + ( stdErr != null ? stdErr.hashCode() : 0 );
        result = 31 * result + ( pid != null ? pid.hashCode() : 0 );
        result = 31 * result + ( macAddress != null ? macAddress.hashCode() : 0 );
        result = 31 * result + ( hostname != null ? hostname.hashCode() : 0 );
        result = 31 * result + ( parentHostName != null ? parentHostName.hashCode() : 0 );
        result = 31 * result + ( ips != null ? ips.hashCode() : 0 );
        result = 31 * result + ( isLxc != null ? isLxc.hashCode() : 0 );
        result = 31 * result + ( transportId != null ? transportId.hashCode() : 0 );
        result = 31 * result + ( environmentId != null ? environmentId.hashCode() : 0 );
        result = 31 * result + ( confPoints != null ? Arrays.hashCode( confPoints ) : 0 );
        result = 31 * result + ( configPoint != null ? configPoint.hashCode() : 0 );
        result = 31 * result + ( changeType != null ? changeType.hashCode() : 0 );
        result = 31 * result + ( dateTime != null ? dateTime.hashCode() : 0 );
        return result;
    }
}
