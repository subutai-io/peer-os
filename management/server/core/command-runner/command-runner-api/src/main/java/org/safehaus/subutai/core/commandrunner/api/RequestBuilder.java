/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.commandrunner.api;


import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.enums.OutputRedirection;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.protocol.Request;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.safehaus.subutai.common.settings.Common;


/**
 * Represents command to agent. This class is used when the same command should be run on a set of agents
 * simultaneously
 */
public class RequestBuilder {

    //source of command
    private final static String source = "COMMAND-RUNNER";

    //the same for all commands
    private final Integer requestSequenceNumber = 1;

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


    /**
     * Constructor
     *
     * @param command - command to run
     */
    public RequestBuilder( String command ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( command ), "Command is null or empty" );

        this.command = command;
    }


    /**
     * Returns command explicit timeout in seconds
     *
     * @return - timeout {@code Integer}
     */
    public Integer getTimeout() {
        return timeout;
    }


    public RequestBuilder withCwd( String cwd ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( cwd ), "Current working directory is null or empty" );

        this.cwd = cwd;

        return this;
    }


    /**
     * Sets command type
     *
     * @param type - {@code RequestType}
     */
    public RequestBuilder withType( RequestType type ) {
        Preconditions.checkNotNull( type, "Request Type is null" );

        this.type = type;

        return this;
    }


    /**
     * Sets command std output redirection
     *
     * @param outputRedirection - {@code OutputRedirection}
     */
    public RequestBuilder withStdOutRedirection( OutputRedirection outputRedirection ) {
        Preconditions.checkNotNull( outputRedirection, "Std Out Redirection is null" );

        this.outputRedirection = outputRedirection;

        return this;
    }


    /**
     * Sets command err output redirection
     *
     * @param errRedirection - {@code OutputRedirection}
     */
    public RequestBuilder withStdErrRedirection( OutputRedirection errRedirection ) {
        Preconditions.checkNotNull( errRedirection, "Std Err Redirection is null" );

        this.errRedirection = errRedirection;

        return this;
    }


    /**
     * Sets command timeout
     *
     * @param timeout - command timeout in seconds
     */
    public RequestBuilder withTimeout( int timeout ) {
        Preconditions.checkArgument( timeout > 0 && timeout <= Common.MAX_COMMAND_TIMEOUT_SEC,
                String.format( "Timeout is not in range 1 to %s", Common.MAX_COMMAND_TIMEOUT_SEC ) );

        this.timeout = timeout;

        return this;
    }


    /**
     * Sets command std output redirection file path Only actual if {@code outputRedirection} is  CAPTURE or
     * CAPTURE_AND_RETURN
     *
     * @param stdOutPath - path to file to redirect std output
     */
    public RequestBuilder withStdOutPath( String stdOutPath ) {

        this.stdOutPath = stdOutPath;

        return this;
    }


    /**
     * Sets command err output redirection file path Only actual if {@code errRedirection} is  CAPTURE or
     * CAPTURE_AND_RETURN
     *
     * @param stdErrPath - path to file to redirect err output
     */
    public RequestBuilder withErrPath( String stdErrPath ) {

        this.stdErrPath = stdErrPath;

        return this;
    }


    /**
     * Sets user under which to run command
     *
     * @param runAs - user
     */
    public RequestBuilder withRunAs( String runAs ) {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( runAs ), "Run As is null or empty" );

        this.runAs = runAs;

        return this;
    }


    /**
     * Sets command line arguments for command
     *
     * @param cmdArgs - command line arguments
     */
    public RequestBuilder withCmdArgs( List<String> cmdArgs ) {

        this.cmdArgs = cmdArgs;

        return this;
    }


    /**
     * Sets environment variables for command
     *
     * @param envVars - environment variables
     */
    public RequestBuilder withEnvVars( Map<String, String> envVars ) {

        this.envVars = envVars;

        return this;
    }


    /**
     * Sets PID of process to terminate. This is actual only for command with type TERMINATE_REQUEST
     *
     * @param pid - pid of process to terminate
     */
    public RequestBuilder withPid( int pid ) {
        Preconditions.checkArgument( pid > 0, "PID is less then or equal to 0" );

        this.pid = pid;

        return this;
    }


    /**
     * Sets configuration points to track. This is actual for command with type INOTIFY_CREATE_REQUEST or
     * INOTIFY_REMOVE_REQUEST
     */
    public RequestBuilder withConfPoints( String confPoints[] ) {

        this.confPoints = confPoints.clone();

        return this;
    }


    /**
     * Builds and returns Request object
     *
     * @param agentUUID - target agent UUID
     * @param taskUUID - command UUID
     */
    public Request build( UUID agentUUID, UUID taskUUID ) {

        return new Request( source, type, agentUUID, taskUUID, requestSequenceNumber, cwd, command, outputRedirection,
                errRedirection, stdOutPath, stdErrPath, runAs, cmdArgs, envVars, pid, timeout )
                .setConfPoints( confPoints );
    }
}
