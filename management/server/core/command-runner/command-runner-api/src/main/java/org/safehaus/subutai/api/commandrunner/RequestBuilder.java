/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.commandrunner;


import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.shared.protocol.Request;
import org.safehaus.subutai.shared.protocol.enums.OutputRedirection;
import org.safehaus.subutai.shared.protocol.enums.RequestType;
import org.safehaus.subutai.shared.protocol.settings.Common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


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


    public Integer getTimeout() {
        return timeout;
    }


    public RequestBuilder withCwd( String cwd ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( cwd ), "Current working directory is null or empty" );

        this.cwd = cwd;

        return this;
    }


    public RequestBuilder withType( RequestType type ) {
        Preconditions.checkNotNull( type, "Request Type is null" );

        this.type = type;

        return this;
    }


    public RequestBuilder withStdOutRedirection( OutputRedirection outputRedirection ) {
        Preconditions.checkNotNull( outputRedirection, "Std Out Redirection is null" );

        this.outputRedirection = outputRedirection;

        return this;
    }


    public RequestBuilder withStdErrRedirection( OutputRedirection errRedirection ) {
        Preconditions.checkNotNull( errRedirection, "Std Err Redirection is null" );

        this.errRedirection = errRedirection;

        return this;
    }


    public RequestBuilder withTimeout( int timeout ) {
        Preconditions.checkArgument( timeout > 0 && timeout <= Common.MAX_COMMAND_TIMEOUT_SEC,
                String.format( "Timeout is not in range 1 to %s", Common.MAX_COMMAND_TIMEOUT_SEC ) );

        this.timeout = timeout;

        return this;
    }


    public RequestBuilder withStdOutPath( String stdOutPath ) {

        this.stdOutPath = stdOutPath;

        return this;
    }


    public RequestBuilder withErrPath( String stdErrPath ) {

        this.stdErrPath = stdErrPath;

        return this;
    }


    public RequestBuilder withRunAs( String runAs ) {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( runAs ), "Run As is null or empty" );

        this.runAs = runAs;

        return this;
    }


    public RequestBuilder withCmdArgs( List<String> cmdArgs ) {

        this.cmdArgs = cmdArgs;

        return this;
    }


    public RequestBuilder withEnvVars( Map<String, String> envVars ) {

        this.envVars = envVars;

        return this;
    }


    public RequestBuilder withPid( int pid ) {
        Preconditions.checkArgument( pid > 0, "PID is less then or equal to 0" );

        this.pid = pid;

        return this;
    }


    public RequestBuilder withConfPoints( String confPoints[] ) {

        this.confPoints = confPoints.clone();

        return this;
    }


    public Request build( UUID agentUUID, UUID taskUUID ) {

        return new Request( source, type, agentUUID, taskUUID, requestSequenceNumber, cwd, command, outputRedirection,
                errRedirection, stdOutPath, stdErrPath, runAs, cmdArgs, envVars, pid, timeout )
                .setConfPoints( confPoints );
    }
}
