/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.subutai.common.command;


import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.settings.Common;
import io.subutai.common.util.NumUtil;


/**
 * Represents command to agent. This class is used when the same command should be run on a set of agents
 * simultaneously
 */
public class RequestBuilder
{

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
    private Integer timeout = Common.DEFAULT_EXECUTOR_REQUEST_TIMEOUT_SEC;

    //user under which to run the command
    private String runAs = "root";

    //command arguments
    private List<String> cmdArgs;

    //environment variables
    private Map<String, String> envVars;


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


    public RequestBuilder daemon()
    {
        this.isDaemon = 1;

        return this;
    }


    public Request build( String id )
    {
        return new RequestImpl( type, id, cwd, command, cmdArgs, envVars, outputRedirection, errRedirection, runAs,
                timeout, isDaemon );
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

        if ( cmdArgs != null ? !cmdArgs.equals( that.cmdArgs ) : that.cmdArgs != null )
        {
            return false;
        }
        if ( command != null ? !command.equals( that.command ) : that.command != null )
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

        if ( timeout != null ? !timeout.equals( that.timeout ) : that.timeout != null )
        {
            return false;
        }
        return type == that.type;
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
        result = 31 * result + ( runAs != null ? runAs.hashCode() : 0 );
        result = 31 * result + ( cmdArgs != null ? cmdArgs.hashCode() : 0 );
        result = 31 * result + ( envVars != null ? envVars.hashCode() : 0 );
        return result;
    }


    private static class RequestImpl implements Request
    {
        private RequestType type;
        private String id;
        private UUID commandId;
        private String workingDirectory;
        private String command;
        private List<String> args;
        private Map<String, String> environment;
        private OutputRedirection stdOut;
        private OutputRedirection stdErr;
        private String runAs;
        private Integer timeout;
        private Integer isDaemon;


        RequestImpl( final RequestType type, final String id, final String workingDirectory, final String command,
                     final List<String> args, final Map<String, String> environment, final OutputRedirection stdOut,
                     final OutputRedirection stdErr, final String runAs, final Integer timeout, final Integer isDaemon )
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
        public RequestType getType()
        {
            return type;
        }


        @Override
        public String getId()
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
        public OutputRedirection getStdOut()
        {
            return stdOut;
        }


        @Override
        public OutputRedirection getStdErr()
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


        @Override
        public boolean equals( final Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( !( o instanceof RequestImpl ) )
            {
                return false;
            }

            final RequestImpl request = ( RequestImpl ) o;

            return commandId.equals( request.commandId );
        }


        @Override
        public int hashCode()
        {
            return commandId.hashCode();
        }
    }
}
