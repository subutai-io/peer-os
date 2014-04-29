/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author dilshat
 */
public class TestUtils {

    public static Response getResponse(ResponseType responseType, Integer exitCode, Task task) {
        Response response = mock(Response.class);
        when(response.getUuid()).thenReturn(task.getRequests().get(0).getUuid());
        when(response.getTaskUuid()).thenReturn(task.getUuid());
        when(response.getType()).thenReturn(responseType);
        when(response.getExitCode()).thenReturn(exitCode);

        return response;
    }

    public static Task getTask(int timeout) {
        Task task = new Task();
        UUID agentID = UUID.randomUUID();
        Agent agent = new Agent(agentID, "testhost", "parent", null, null, true, null);
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
        return new TaskCallback() {

            public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                return null;
            }
        };
    }

}
