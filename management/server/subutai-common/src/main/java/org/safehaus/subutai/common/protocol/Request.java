package org.safehaus.subutai.common.protocol;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.enums.RequestType;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class Request implements Serializable
{

    private String source = null;
    private RequestType type = null;
    private UUID uuid = null;
    private UUID taskUuid = null;
    private Integer requestSequenceNumber = null;
    private String workingDirectory = null;
    private String program = null;
    private OutputRedirection stdOut = null;
    private OutputRedirection stdErr = null;
    private String stdOutPath = null;
    private String stdErrPath = null;
    private String runAs = null;
    private List<String> args = null;
    private Map<String, String> environment = null;
    private Integer pid = null;
    private Integer timeout = 30;
    private Set<String> confPoints;


    public Request( String source, RequestType type, UUID uuid, UUID taskUuid, Integer requestSequenceNumber,
                    String workingDirectory, String program, OutputRedirection stdOut, OutputRedirection stdErr,
                    String stdOutPath, String stdErrPath, String runAs, List<String> args,
                    Map<String, String> environment, Integer pid, Integer timeout )
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( source ), "Source is null or empty" );

        Preconditions.checkNotNull( type, "Request Type is null" );

        Preconditions.checkNotNull( uuid, "UUID is null" );

        Preconditions.checkNotNull( taskUuid, "TaskUuid is null" );

        this.source = source;
        this.type = type;
        this.uuid = uuid;
        this.taskUuid = taskUuid;
        this.requestSequenceNumber = requestSequenceNumber;
        this.workingDirectory = workingDirectory;
        this.program = program;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.stdOutPath = stdOutPath;
        this.stdErrPath = stdErrPath;
        this.runAs = runAs;
        this.args = args;
        this.environment = environment;
        this.pid = pid;
        this.timeout = timeout;
    }


    public UUID getTaskUuid()
    {
        return taskUuid;
    }


    public String getSource()
    {
        return source;
    }


    public RequestType getType()
    {
        return type;
    }


    public UUID getUuid()
    {
        return uuid;
    }


    public Integer getRequestSequenceNumber()
    {
        return requestSequenceNumber;
    }


    public String getWorkingDirectory()
    {
        return workingDirectory;
    }


    public String getProgram()
    {
        return program;
    }


    public OutputRedirection getStdOut()
    {
        return stdOut;
    }


    public OutputRedirection getStdErr()
    {
        return stdErr;
    }


    public String getStdOutPath()
    {
        return stdOutPath;
    }


    public String getStdErrPath()
    {
        return stdErrPath;
    }


    public String getRunAs()
    {
        return runAs;
    }


    public List<String> getArgs()
    {
        return args;
    }


    public Map<String, String> getEnvironment()
    {
        return environment;
    }


    public Integer getTimeout()
    {
        return timeout;
    }


    public Integer getPid()
    {
        return pid;
    }


    public Set<String> getConfPoints()
    {
        return confPoints;
    }


    public Request setConfPoints( Set<String> confPoints )
    {
        this.confPoints = confPoints;
        return this;
    }


    @Override
    public String toString()
    {
        return "Request{" + "source=" + source + ", type=" + type + ", uuid=" + uuid + ", taskUuid=" + taskUuid
                + ", requestSequenceNumber=" + requestSequenceNumber + ", workingDirectory=" + workingDirectory
                + ", program=" + program + ", stdOut=" + stdOut + ", stdErr=" + stdErr + ", stdOutPath=" + stdOutPath
                + ", stdErrPath=" + stdErrPath + ", runAs=" + runAs + ", args=" + args + ", environment=" + environment
                + ", pid=" + pid + ", timeout=" + timeout + '}';
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof Request ) )
        {
            return false;
        }

        final Request request = ( Request ) o;

        if ( args != null ? !args.equals( request.args ) : request.args != null )
        {
            return false;
        }
        if ( confPoints != null ? !confPoints.equals( request.confPoints ) : request.confPoints != null )
        {
            return false;
        }
        if ( environment != null ? !environment.equals( request.environment ) : request.environment != null )
        {
            return false;
        }
        if ( pid != null ? !pid.equals( request.pid ) : request.pid != null )
        {
            return false;
        }
        if ( program != null ? !program.equals( request.program ) : request.program != null )
        {
            return false;
        }
        if ( requestSequenceNumber != null ? !requestSequenceNumber.equals( request.requestSequenceNumber ) :
             request.requestSequenceNumber != null )
        {
            return false;
        }
        if ( runAs != null ? !runAs.equals( request.runAs ) : request.runAs != null )
        {
            return false;
        }
        if ( source != null ? !source.equals( request.source ) : request.source != null )
        {
            return false;
        }
        if ( stdErr != request.stdErr )
        {
            return false;
        }
        if ( stdErrPath != null ? !stdErrPath.equals( request.stdErrPath ) : request.stdErrPath != null )
        {
            return false;
        }
        if ( stdOut != request.stdOut )
        {
            return false;
        }
        if ( stdOutPath != null ? !stdOutPath.equals( request.stdOutPath ) : request.stdOutPath != null )
        {
            return false;
        }
        if ( taskUuid != null ? !taskUuid.equals( request.taskUuid ) : request.taskUuid != null )
        {
            return false;
        }
        if ( timeout != null ? !timeout.equals( request.timeout ) : request.timeout != null )
        {
            return false;
        }
        if ( type != request.type )
        {
            return false;
        }
        if ( uuid != null ? !uuid.equals( request.uuid ) : request.uuid != null )
        {
            return false;
        }
        if ( workingDirectory != null ? !workingDirectory.equals( request.workingDirectory ) :
             request.workingDirectory != null )
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
        result = 31 * result + ( uuid != null ? uuid.hashCode() : 0 );
        result = 31 * result + ( taskUuid != null ? taskUuid.hashCode() : 0 );
        result = 31 * result + ( requestSequenceNumber != null ? requestSequenceNumber.hashCode() : 0 );
        result = 31 * result + ( workingDirectory != null ? workingDirectory.hashCode() : 0 );
        result = 31 * result + ( program != null ? program.hashCode() : 0 );
        result = 31 * result + ( stdOut != null ? stdOut.hashCode() : 0 );
        result = 31 * result + ( stdErr != null ? stdErr.hashCode() : 0 );
        result = 31 * result + ( stdOutPath != null ? stdOutPath.hashCode() : 0 );
        result = 31 * result + ( stdErrPath != null ? stdErrPath.hashCode() : 0 );
        result = 31 * result + ( runAs != null ? runAs.hashCode() : 0 );
        result = 31 * result + ( args != null ? args.hashCode() : 0 );
        result = 31 * result + ( environment != null ? environment.hashCode() : 0 );
        result = 31 * result + ( pid != null ? pid.hashCode() : 0 );
        result = 31 * result + ( timeout != null ? timeout.hashCode() : 0 );
        result = 31 * result + ( confPoints != null ? confPoints.hashCode() : 0 );
        return result;
    }
}
