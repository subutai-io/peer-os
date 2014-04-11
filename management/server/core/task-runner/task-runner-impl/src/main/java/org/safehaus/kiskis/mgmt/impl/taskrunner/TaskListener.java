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
import org.safehaus.kiskis.mgmt.api.taskrunner.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 * This class is used by {@code TaskMediator}. Holds task along with its
 * callback
 *
 * @author dilshat
 */
class TaskListener {

    /**
     * {@code Task}
     */
    private final Task task;
    /**
     * task callback
     */
    private final TaskCallback taskCallback;
    /**
     * map which holds all cumulated stdouts for this task where key is UUID of
     * agent/node. Each std out contains last 10000 cumulated symbols
     */
    private final Map<UUID, StringBuilder> stdOut;
    /**
     * map which holds all cumulated stderrs for this task where key is UUID of
     * agent/node. Each std err contains last 10000 cumulated symbols
     *
     */
    private final Map<UUID, StringBuilder> stdErr;

    /**
     * Initializes the {@code TaskListener}
     *
     * @param task - task
     * @param taskCallback - task callback
     */
    public TaskListener(Task task, TaskCallback taskCallback) {
        this.task = task;
        this.taskCallback = taskCallback;
        stdOut = new HashMap<UUID, StringBuilder>();
        stdErr = new HashMap<UUID, StringBuilder>();
    }

    /**
     * Returns task
     *
     * @return task
     */
    public Task getTask() {
        return task;
    }

    /**
     * Returns task callback
     *
     * @return task callback
     */
    public TaskCallback getTaskCallback() {
        return taskCallback;
    }

    /**
     * appends std out and std err to memory map
     *
     * @param response response which std out and std err streams to append
     */
    public void appendStreams(Response response) {
        appendOut(response);
        appendErr(response);
    }

    /**
     * appends std out stream to memory map
     *
     * @param response response which std out stream to append
     */
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

    /**
     * appends std err stream to memory map
     *
     * @param response response which std err stream to append
     */
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

    /**
     * returns cumulated last 10000 symbols of std out stream for the agent/node
     * to whom relates the supplied response
     *
     * @param response response for which to return cumulated std out
     * @return cumulated last 10000 symbols of std out stream
     */
    public String getStdOut(Response response) {
        if (stdOut.get(response.getUuid()) != null) {
            return stdOut.get(response.getUuid()).toString();
        }
        return "".intern();
    }

    /**
     * returns cumulated last 10000 symbols of std err stream for the agent/node
     * to whom relates the supplied response
     *
     * @param response response for which to return cumulated std err
     * @return cumulated last 10000 symbols of std err stream
     */
    public String getStdErr(Response response) {
        if (stdErr.get(response.getUuid()) != null) {
            return stdErr.get(response.getUuid()).toString();
        }
        return "".intern();
    }

}
