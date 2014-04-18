/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

/**
 *
 * @author dilshat
 */
public class TestUtils {

    public static Response getResponse(ResponseType responseType, Integer exitCode, Task task) {
        Response response = new Response();
        response.setUuid(task.getRequests().get(0).getUuid());
        response.setTaskUuid(task.getUuid());
        response.setType(responseType);
        response.setExitCode(exitCode);

        return response;
    }

    public static Task getTask(int timeout) {
        Task task = new Task();
        UUID agentID = UUID.randomUUID();
        Agent agent = new Agent(agentID, "testhost");
        Request req = getRequestTemplate();
        req.setTimeout(timeout);
        task.addRequest(req, agent);
        return task;
    }

    public static Request getRequestTemplate() {
        return CommandFactory.newRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                null, //                        source
                null, //                        !! task uuid 
                1, //                           !! request sequence number
                "/", //                         cwd
                "pwd", //                        program
                OutputRedirection.RETURN, //    std output redirection 
                OutputRedirection.RETURN, //    std error redirection
                null, //                        stdout capture file path
                null, //                        stderr capture file path
                "root", //                      runas
                null, //                        arg
                null, //                        env vars
                30); //  
    }

    public static TaskCallback getCallback() {
        TaskCallback callback = new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                return null;
            }
        };
        return callback;
    }

}
