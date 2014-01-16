/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

/**
 *
 * @author dilshat
 */
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommandFactory {

    public static CommandInterface createRequest(RequestType type, UUID uuid, String source, UUID taskUuid,
            Integer reqSeqNum, String workDir, String program,
            OutputRedirection stdOut, OutputRedirection stdErr, String stdOutPath,
            String stdErrPath, String runAs, List<String> args,
            Map<String, String> envVars, Integer timeout) {
        Request req = new Request();
        req.setSource(source);
        req.setType(type);
        req.setUuid(uuid);
        req.setTaskUuid(taskUuid);
        req.setRequestSequenceNumber(reqSeqNum);
        req.setWorkingDirectory(workDir);
        req.setProgram(program);
        req.setStdOut(stdOut);
        req.setStdErr(stdErr);
        req.setStdOutPath(stdOutPath);
        req.setStdErrPath(stdErrPath);
        req.setRunAs(runAs);
        req.setArgs(args);
        req.setEnvironment(envVars);
        req.setTimeout(timeout);

        return new Command(req);
    }

    public static CommandInterface createResponse(ResponseType type, UUID uuid, String source, UUID taskUuid,
            Integer exitCode, String stdOut, String stdErr, Integer reqSeqNum,
            Integer resSeqnum, Integer pid, String macAddress, String hostname,
            List<String> ips, Boolean isLxc) {
        Response res = new Response();
        res.setSource(source);
        res.setType(type);
        res.setUuid(uuid);
        res.setTaskUuid(taskUuid);
        res.setExitCode(exitCode);
        res.setStdOut(stdOut);
        res.setStdErr(stdErr);
        res.setRequestSequenceNumber(reqSeqNum);
        res.setResponseSequenceNumber(resSeqnum);
        res.setPid(pid);
        res.setMacAddress(macAddress);
        res.setHostname(hostname);
        res.setIps(ips);
        res.setIsLxc(isLxc);

        return new Command(res);
    }
}
