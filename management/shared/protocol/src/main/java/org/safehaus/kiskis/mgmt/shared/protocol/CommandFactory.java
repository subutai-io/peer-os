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
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommandFactory {

    public static Request newRequest(RequestType type, UUID uuid, String source, UUID taskUuid,
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

        return req;
    }

}
