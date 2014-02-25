/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.taskrunner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class ChainedTaskListener {

    private final Task task;
    private final TaskCallback taskCallback;
    private final Map<UUID, StringBuilder> stdOut;
    private final Map<UUID, StringBuilder> stdErr;

    public ChainedTaskListener(Task task, TaskCallback taskCallback) {
        this.task = task;
        this.taskCallback = taskCallback;
        stdOut = new HashMap<UUID, StringBuilder>();
        stdErr = new HashMap<UUID, StringBuilder>();
    }

    public Task getTask() {
        return task;
    }

    public TaskCallback getTaskCallback() {
        return taskCallback;
    }

    public void appendStreams(Response response) {
        appendOut(response);
        appendErr(response);
    }

    private void appendOut(Response response) {
        if (!Util.isStringEmpty(response.getStdOut())) {
            StringBuilder sb = stdOut.get(response.getUuid());
            if (sb == null) {
                sb = new StringBuilder();
                stdOut.put(response.getUuid(), sb);
            }
            sb.append(response.getStdOut());
            if (sb.length() > Common.MAX_COLLECTED_RESPONSE_LENGTH) {
                sb.delete(0, sb.length() - Common.MAX_COLLECTED_RESPONSE_LENGTH);
            }
        }
    }

    private void appendErr(Response response) {
        if (!Util.isStringEmpty(response.getStdErr())) {
            StringBuilder sb = stdErr.get(response.getUuid());
            if (sb == null) {
                sb = new StringBuilder();
                stdErr.put(response.getUuid(), sb);
            }
            sb.append(response.getStdErr());
            if (sb.length() > Common.MAX_COLLECTED_RESPONSE_LENGTH) {
                sb.delete(0, sb.length() - Common.MAX_COLLECTED_RESPONSE_LENGTH);
            }
        }
    }

    public String getStdOut(Response response) {
        if (stdOut.get(response.getUuid()) != null) {
            return stdOut.get(response.getUuid()).toString();
        }
        return "".intern();
    }

    public String getStdErr(Response response) {
        if (stdErr.get(response.getUuid()) != null) {
            return stdErr.get(response.getUuid()).toString();
        }
        return "".intern();
    }

}
