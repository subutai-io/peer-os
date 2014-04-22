/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.commandrunner;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class RequestBuilder {

    //never change
    private final String source = "COMMAND-RUNNER";
    private final Integer requestSequenceNumber = 1;
    //mandatory
    private final String command;
    //optional
    private String cwd = "/";
    private RequestType type = RequestType.EXECUTE_REQUEST;
    private OutputRedirection outputRedirection = OutputRedirection.RETURN;
    private OutputRedirection errRedirection = OutputRedirection.RETURN;
    private Integer timeout = 30;
    private String stdOutPath;
    private String stdErrPath;
    private String runAs = "root";
    private List<String> cmdArgs;
    private Map<String, String> envVars;

    public Integer getTimeout() {
        return timeout;
    }

    public RequestBuilder(String command) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(command),
                "Command is null or empty");

        this.command = command;
    }

    public RequestBuilder withCwd(String cwd) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(cwd),
                "Current working directory is null or empty");

        this.cwd = cwd;

        return this;
    }

    public RequestBuilder withType(RequestType type) {
        Preconditions.checkNotNull(type, "Request Type is null");

        this.type = type;

        return this;
    }

    public RequestBuilder withStdOutRedirection(OutputRedirection outputRedirection) {
        Preconditions.checkNotNull(outputRedirection,
                "Std Out Redirection is null");

        this.outputRedirection = outputRedirection;

        return this;
    }

    public RequestBuilder withStdErrRedirection(OutputRedirection errRedirection) {
        Preconditions.checkNotNull(errRedirection,
                "Std Err Redirection is null");

        this.errRedirection = errRedirection;

        return this;
    }

    public RequestBuilder withTimeout(int timeout) {
        Preconditions.checkArgument(
                timeout > 0 && timeout <= Common.MAX_COMMAND_TIMEOUT_SEC,
                String.format("Timeout is not in range 1 to %s", Common.MAX_COMMAND_TIMEOUT_SEC));

        this.timeout = timeout;

        return this;
    }

    public RequestBuilder withStdOutPath(String stdOutPath) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(stdOutPath),
                "Std Out path is null or empty");

        this.stdOutPath = stdOutPath;

        return this;
    }

    public RequestBuilder withErrPath(String stdErrPath) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(stdErrPath),
                "Std Err path is null or empty");

        this.stdErrPath = stdErrPath;

        return this;
    }

    public RequestBuilder withRunAs(String runAs) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(runAs),
                "Run As is null or empty");

        this.runAs = runAs;

        return this;
    }

    public RequestBuilder withCmdArgs(List<String> cmdArgs) {
        Preconditions.checkArgument(cmdArgs != null && !cmdArgs.isEmpty(),
                "Args are null or empty");

        this.cmdArgs = cmdArgs;

        return this;
    }

    public RequestBuilder withEnvVars(Map<String, String> envVars) {
        Preconditions.checkArgument(envVars != null && !envVars.isEmpty(),
                "Env vars are null or empty");

        this.envVars = envVars;

        return this;
    }

    public Request build(UUID agentUUID, UUID taskUUID) {
        Request request = new Request();

        request.setSource(source);
        request.setRequestSequenceNumber(requestSequenceNumber);
        request.setProgram(command);
        request.setUuid(agentUUID);
        request.setTaskUuid(taskUUID);
        request.setTimeout(timeout);
        request.setWorkingDirectory(cwd);
        request.setType(type);
        request.setStdOut(outputRedirection);
        request.setStdErr(errRedirection);
        request.setStdOutPath(stdOutPath);
        request.setStdErrPath(stdErrPath);
        request.setRunAs(runAs);
        request.setArgs(cmdArgs);
        request.setEnvironment(envVars);

        return request;
    }

}
