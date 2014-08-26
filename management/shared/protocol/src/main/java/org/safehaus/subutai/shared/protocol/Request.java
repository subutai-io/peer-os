package org.safehaus.subutai.shared.protocol;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.safehaus.subutai.shared.protocol.enums.OutputRedirection;
import org.safehaus.subutai.shared.protocol.enums.RequestType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class Request implements Serializable {

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
	private String confPoints[];

	public Request(String source, RequestType type, UUID uuid, UUID taskUuid, Integer requestSequenceNumber, String workingDirectory, String program, OutputRedirection stdOut, OutputRedirection stdErr, String stdOutPath, String stdErrPath, String runAs, List<String> args, Map<String, String> environment, Integer pid, Integer timeout) {

		Preconditions.checkArgument(!Strings.isNullOrEmpty(source),
				"Source is null or empty");

		Preconditions.checkNotNull(type, "Request Type is null");

		Preconditions.checkNotNull(uuid, "UUID is null");

		Preconditions.checkNotNull(taskUuid, "TaskUuid is null");

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

	public UUID getTaskUuid() {
		return taskUuid;
	}

	public String getSource() {
		return source;
	}

	public RequestType getType() {
		return type;
	}

	public UUID getUuid() {
		return uuid;
	}

	public Integer getRequestSequenceNumber() {
		return requestSequenceNumber;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public String getProgram() {
		return program;
	}

	public OutputRedirection getStdOut() {
		return stdOut;
	}

	public OutputRedirection getStdErr() {
		return stdErr;
	}

	public String getStdOutPath() {
		return stdOutPath;
	}

	public String getStdErrPath() {
		return stdErrPath;
	}

	public String getRunAs() {
		return runAs;
	}

	public List<String> getArgs() {
		return args;
	}

	public Map<String, String> getEnvironment() {
		return environment;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public Integer getPid() {
		return pid;
	}


	public String[] getConfPoints() {
		return confPoints;
	}


	public Request setConfPoints(String confPoints[]) {
		this.confPoints = confPoints;
		return this;
	}


	@Override
	public String toString() {
		return "Request{" + "source=" + source + ", type=" + type + ", uuid=" + uuid + ", taskUuid=" + taskUuid
				+ ", requestSequenceNumber=" + requestSequenceNumber + ", workingDirectory=" + workingDirectory
				+ ", program=" + program + ", stdOut=" + stdOut + ", stdErr=" + stdErr + ", stdOutPath=" + stdOutPath
				+ ", stdErrPath=" + stdErrPath + ", runAs=" + runAs + ", args=" + args + ", environment=" + environment
				+ ", pid=" + pid + ", timeout=" + timeout + '}';
	}
}
