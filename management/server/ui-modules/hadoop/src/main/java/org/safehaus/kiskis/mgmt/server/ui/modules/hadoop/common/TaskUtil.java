/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common;

import java.util.HashMap;
import org.safehaus.kiskis.mgmt.api.communication.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;

/**
 *
 * @author dilshat
 */
public class TaskUtil {

    public static Request createRequest(String command, Task task, HashMap<String, String> map) {
        String json = command;

        json = json.replaceAll(":taskUuid", task.getUuid().toString());
        json = json.replaceAll(":requestSequenceNumber", task.getIncrementedReqSeqNumber().toString());

        for (String key : map.keySet()) {
            json = json.replaceAll(key, map.get(key));
        }

        return CommandJson.getRequest(json);
    }

    public static Task getTask(final String command, HashMap<String, String> map) {
        Task task = new Task();
        String json = command;

        json = json.replaceAll(":taskUuid", task.getUuid().toString());
        json = json.replaceAll(":requestSequenceNumber", task.getIncrementedReqSeqNumber().toString());

        for (String key : map.keySet()) {
            json = json.replaceAll(key, map.get(key));
        }

        Request req = CommandJson.getRequest(json);
        task.addRequest(req);

        return task;
    }
}
