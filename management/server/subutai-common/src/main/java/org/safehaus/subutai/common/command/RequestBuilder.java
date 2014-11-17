/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.common.command;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.NumUtil;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Represents command to agent. This class is used when the same command should be run on a set of agents
 * simultaneously
 */
public class RequestBuilder
{

    //SOURCE of command
    private static final String SOURCE = "COMMAND-RUNNER";

    //the same for all commands
    private static final Integer REQUEST_SEQUENCE_NUMBER = 1;

    //the command to execute, e.g. ls
    private final String command;

    //current working directory
    private String cwd = "/";

    //type of command
    private RequestType type = RequestType.EXECUTE_REQUEST;

    //std out redirection
    private OutputRedirection outputRedirection = OutputRedirection.RETURN;

    //std err redirection
    private OutputRedirection errRedirection = OutputRedirection.RETURN;

    //command timeout interval
    private Integer timeout = 30;

    //file path for std out redirection if any
    private String stdOutPath;

    //file path for std err redirection if any
    private String stdErrPath;

    //user under which to run the command
    private String runAs = "root";

    //command arguments
    private List<String> cmdArgs;

    //environment variables
    private Map<String, String> envVars;

    //PID for terminate_request
    private int pid;

    // Config points for inotify
    private String[] confPoints;

    private int isDaemon = 0;


    /**
     * Constructor
     *
     * @param command - command to run
     */
    public RequestBuilder( String command )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( command ), "Command is null or empty" );

        this.command = command;
    }


    /**
     * Returns command explicit timeout in seconds
     *
     * @return - timeout {@code Integer}
     */
    public Integer getTimeout()
    {
        return timeout;
    }


    public RequestBuilder withCwd( String cwd )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( cwd ), "Current working directory is null or empty" );

        this.cwd = cwd;

        return this;
    }


    /**
     * Sets command type
     *
     * @param type - {@code RequestType}
     */
    public RequestBuilder withType( RequestType type )
    {
        Preconditions.checkNotNull( type, "Request Type is null" );

        this.type = type;

        return this;
    }


    /**
     * Sets command std output redirection
     *
     * @param outputRedirection - {@code OutputRedirection}
     */
    public RequestBuilder withStdOutRedirection( OutputRedirection outputRedirection )
    {
        Preconditions.checkNotNull( outputRedirection, "Std Out Redirection is null" );

        this.outputRedirection = outputRedirection;

        return this;
    }


    /**
     * Sets command err output redirection
     *
     * @param errRedirection - {@code OutputRedirection}
     */
    public RequestBuilder withStdErrRedirection( OutputRedirection errRedirection )
    {
        Preconditions.checkNotNull( errRedirection, "Std Err Redirection is null" );

        this.errRedirection = errRedirection;

        return this;
    }


    /**
     * Sets command timeout
     *
     * @param timeout - command timeout in seconds
     */
    public RequestBuilder withTimeout( int timeout )
    {
        Preconditions.checkArgument(
                NumUtil.isIntBetween( timeout, Common.MIN_COMMAND_TIMEOUT_SEC, Common.MAX_COMMAND_TIMEOUT_SEC ),
                String.format( "Timeout is not in range %d to %d", Common.MIN_COMMAND_TIMEOUT_SEC,
                        Common.MAX_COMMAND_TIMEOUT_SEC ) );

        this.timeout = timeout;

        return this;
    }


    /**
     * Sets command std output redirection file path Only actual if {@code outputRedirection} is  CAPTURE or
     * CAPTURE_AND_RETURN
     *
     * @param stdOutPath - path to file to redirect std output
     */
    public RequestBuilder withStdOutPath( String stdOutPath )
    {

        this.stdOutPath = stdOutPath;

        return this;
    }


    /**
     * Sets command err output redirection file path Only actual if {@code errRedirection} is  CAPTURE or
     * CAPTURE_AND_RETURN
     *
     * @param stdErrPath - path to file to redirect err output
     */
    public RequestBuilder withErrPath( String stdErrPath )
    {

        this.stdErrPath = stdErrPath;

        return this;
    }


    /**
     * Sets user under which to run command
     *
     * @param runAs - user
     */
    public RequestBuilder withRunAs( String runAs )
    {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( runAs ), "Run As is null or empty" );

        this.runAs = runAs;

        return this;
    }


    /**
     * Sets command line arguments for command
     *
     * @param cmdArgs - command line arguments
     */
    public RequestBuilder withCmdArgs( List<String> cmdArgs )
    {

        this.cmdArgs = cmdArgs;

        return this;
    }


    /**
     * Sets environment variables for command
     *
     * @param envVars - environment variables
     */
    public RequestBuilder withEnvVars( Map<String, String> envVars )
    {

        this.envVars = envVars;

        return this;
    }


    /**
     * Sets PID of process to terminate. This is actual only for command with type TERMINATE_REQUEST
     *
     * @param pid - pid of process to terminate
     */
    public RequestBuilder withPid( int pid )
    {
        Preconditions.checkArgument( pid > 0, "PID is less then or equal to 0" );

        this.pid = pid;

        return this;
    }


    /**
     * Sets configuration points to track. This is actual for command with type INOTIFY_CREATE_REQUEST or
     * INOTIFY_REMOVE_REQUEST
     */
    public RequestBuilder withConfPoints( String[] confPoints )
    {

        this.confPoints = confPoints.clone();

        return this;
    }


    public RequestBuilder daemon()
    {
        this.isDaemon = 1;

        return this;
    }


    /**
     * Builds and returns Request object
     *
     * @param agentUUID - target agent UUID
     * @param taskUUID - command UUID
     */
    public org.safehaus.subutai.common.protocol.Request build( UUID agentUUID, UUID taskUUID )
    {

        return new org.safehaus.subutai.common.protocol.Request( SOURCE, type, agentUUID, taskUUID,
                REQUEST_SEQUENCE_NUMBER, cwd, command, outputRedirection, errRedirection, stdOutPath, stdErrPath, runAs,
                cmdArgs, envVars, pid, timeout ).setConfPoints( confPoints );
    }


    public org.safehaus.subutai.common.command.Request build2( UUID id )
    {
        //TODO pass proper arguments after migration to new agent
        return new RequestImpl( org.safehaus.subutai.common.command.RequestType.EXECUTE_REQUEST, id, cwd, command,
                cmdArgs, envVars, org.safehaus.subutai.common.command.OutputRedirection.RETURN,
                org.safehaus.subutai.common.command.OutputRedirection.RETURN, runAs, timeout, isDaemon );
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof RequestBuilder ) )
        {
            return false;
        }

        final RequestBuilder that = ( RequestBuilder ) o;

        if ( pid != that.pid )
        {
            return false;
        }
        if ( cmdArgs != null ? !cmdArgs.equals( that.cmdArgs ) : that.cmdArgs != null )
        {
            return false;
        }
        if ( command != null ? !command.equals( that.command ) : that.command != null )
        {
            return false;
        }
        if ( !Arrays.equals( confPoints, that.confPoints ) )
        {
            return false;
        }
        if ( cwd != null ? !cwd.equals( that.cwd ) : that.cwd != null )
        {
            return false;
        }
        if ( envVars != null ? !envVars.equals( that.envVars ) : that.envVars != null )
        {
            return false;
        }
        if ( errRedirection != that.errRedirection )
        {
            return false;
        }
        if ( outputRedirection != that.outputRedirection )
        {
            return false;
        }
        if ( runAs != null ? !runAs.equals( that.runAs ) : that.runAs != null )
        {
            return false;
        }
        if ( stdErrPath != null ? !stdErrPath.equals( that.stdErrPath ) : that.stdErrPath != null )
        {
            return false;
        }
        if ( stdOutPath != null ? !stdOutPath.equals( that.stdOutPath ) : that.stdOutPath != null )
        {
            return false;
        }
        if ( timeout != null ? !timeout.equals( that.timeout ) : that.timeout != null )
        {
            return false;
        }
        if ( type != that.type )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result = command != null ? command.hashCode() : 0;
        result = 31 * result + ( cwd != null ? cwd.hashCode() : 0 );
        result = 31 * result + ( type != null ? type.hashCode() : 0 );
        result = 31 * result + ( outputRedirection != null ? outputRedirection.hashCode() : 0 );
        result = 31 * result + ( errRedirection != null ? errRedirection.hashCode() : 0 );
        result = 31 * result + ( timeout != null ? timeout.hashCode() : 0 );
        result = 31 * result + ( stdOutPath != null ? stdOutPath.hashCode() : 0 );
        result = 31 * result + ( stdErrPath != null ? stdErrPath.hashCode() : 0 );
        result = 31 * result + ( runAs != null ? runAs.hashCode() : 0 );
        result = 31 * result + ( cmdArgs != null ? cmdArgs.hashCode() : 0 );
        result = 31 * result + ( envVars != null ? envVars.hashCode() : 0 );
        result = 31 * result + pid;
        result = 31 * result + ( confPoints != null ? Arrays.hashCode( confPoints ) : 0 );
        return result;
    }


    static class RequestImpl implements org.safehaus.subutai.common.command.Request
    {
        private org.safehaus.subutai.common.command.RequestType type;
        private UUID id;
        private UUID commandId;
        private String workingDirectory;
        private String command;
        private List<String> args;
        private Map<String, String> environment;
        private org.safehaus.subutai.common.command.OutputRedirection stdOut;
        private org.safehaus.subutai.common.command.OutputRedirection stdErr;
        private String runAs;
        private Integer timeout;
        private Integer isDaemon;


        RequestImpl( final org.safehaus.subutai.common.command.RequestType type, final UUID id,
                     final String workingDirectory, final String command, final List<String> args,
                     final Map<String, String> environment,
                     final org.safehaus.subutai.common.command.OutputRedirection stdOut,
                     final org.safehaus.subutai.common.command.OutputRedirection stdErr, final String runAs,
                     final Integer timeout, final Integer isDaemon )
        {
            this.type = type;
            this.id = id;
            this.commandId = UUID.randomUUID();
            this.workingDirectory = workingDirectory;
            this.command = command;
            this.args = args;
            this.environment = environment;
            this.stdOut = stdOut;
            this.stdErr = stdErr;
            this.runAs = runAs;
            this.timeout = timeout;
            this.isDaemon = isDaemon;
        }


        @Override
        public org.safehaus.subutai.common.command.RequestType getType()
        {
            return type;
        }


        @Override
        public UUID getId()
        {
            return id;
        }


        @Override
        public UUID getCommandId()
        {
            return commandId;
        }


        @Override
        public String getWorkingDirectory()
        {
            return workingDirectory;
        }


        @Override
        public String getCommand()
        {
            return command;
        }


        @Override
        public List<String> getArgs()
        {
            return args;
        }


        @Override
        public Map<String, String> getEnvironment()
        {
            return environment;
        }


        @Override
        public org.safehaus.subutai.common.command.OutputRedirection getStdOut()
        {
            return stdOut;
        }


        @Override
        public org.safehaus.subutai.common.command.OutputRedirection getStdErr()
        {
            return stdErr;
        }


        @Override
        public String getRunAs()
        {
            return runAs;
        }


        @Override
        public Integer getTimeout()
        {
            return timeout;
        }


        @Override
        public Integer isDaemon()
        {
            return isDaemon;
        }
    }
}
