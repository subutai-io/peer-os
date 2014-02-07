/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.async.runner;

import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ChainedTaskCallback;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class ChainedTaskListener {

    private final Task task;
    private final ChainedTaskCallback taskCallback;
    private final StringBuilder stdOut;
    private final StringBuilder stdErr;

    public ChainedTaskListener(Task task, ChainedTaskCallback taskCallback) {
        this.task = task;
        this.taskCallback = taskCallback;
        stdOut = new StringBuilder();
        stdErr = new StringBuilder();
    }

    public Task getTask() {
        return task;
    }

    public ChainedTaskCallback getTaskCallback() {
        return taskCallback;
    }

    public void appendOut(String stdOutStr) {
        if (!Util.isStringEmpty(stdOutStr)) {
            stdOut.append(stdOutStr);
            if (stdOut.length() > Common.MAX_COLLECTED_RESPONSE_LENGTH) {
                stdOut.delete(0, stdOut.length() - Common.MAX_COLLECTED_RESPONSE_LENGTH);
            }
        }
    }

    public void appendErr(String stdErrStr) {
        if (!Util.isStringEmpty(stdErrStr)) {
            stdErr.append(stdErrStr);
            if (stdErr.length() > Common.MAX_COLLECTED_RESPONSE_LENGTH) {
                stdErr.delete(0, stdErr.length() - Common.MAX_COLLECTED_RESPONSE_LENGTH);
            }
        }
    }

    public String getStdOut() {
        return stdOut.toString();
    }

    public String getStdErr() {
        return stdErr.toString();
    }

}
