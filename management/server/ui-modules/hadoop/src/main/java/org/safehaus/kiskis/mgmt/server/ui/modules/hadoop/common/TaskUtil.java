/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.common;

import java.util.HashMap;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.hadoop.HadoopModule;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;

/**
 *
 * @author dilshat
 */
public class TaskUtil {

    public static Request createRequest(String command, String taskName, HashMap<String, String> map, TaskCallback callback, TaskType taskType) {
        Task task = new Task(taskName);
        if (taskType != null) {
            task.setData(taskType);
        }
        String json = command;

        json = json.replaceAll(":taskUuid", task.getUuid().toString());
        json = json.replaceAll(":requestSequenceNumber", task.getIncrementedReqSeqNumber().toString());

        for (String key : map.keySet()) {
            json = json.replaceAll(key, map.get(key));
        }

        Request request = CommandJson.getRequest(json);
        HadoopModule.getTaskRunner().executeTask(task, callback);
        return request;
    }

    public static Request createRequest(String command, String taskName, HashMap<String, String> map, TaskCallback callback) {
        return createRequest(command, taskName, map, callback, null);
    }

    public static Request createRequest(String command, Task task, HashMap<String, String> map) {
        String json = command;

        json = json.replaceAll(":taskUuid", task.getUuid().toString());
        json = json.replaceAll(":requestSequenceNumber", task.getIncrementedReqSeqNumber().toString());

        for (String key : map.keySet()) {
            json = json.replaceAll(key, map.get(key));
        }

        return CommandJson.getRequest(json);
    }
}
