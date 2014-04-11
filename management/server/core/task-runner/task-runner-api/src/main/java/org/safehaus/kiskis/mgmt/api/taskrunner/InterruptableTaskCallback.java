/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.taskrunner;

import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 * This class is used when task needs to be interrupted earlier then it
 * completes or times out. One can call interrupt() which will cause TaskRunner
 * to return from executeNWait method immediately.
 *
 * @author dilshat
 */
public abstract class InterruptableTaskCallback {

    private final TaskCallback callback;
    private Task lastTask;

    public Task getLastTask() {
        return lastTask;
    }

    public InterruptableTaskCallback() {
        callback = new WrappedCallBack(this);
    }

    /**
     * This should be implemented by client code
     */
    public abstract Task onResponse(Task task, Response response, String stdOut, String stdErr);

    /**
     * Causes TaskRunner to return from executeNWait method call.
     */
    public final void interrupt() {
        synchronized (callback) {
            callback.notifyAll();
        }
    }

    /**
     * Returns wrapped task callback.
     *
     * @return wrapped task callback.
     */
    public TaskCallback getCallback() {
        return callback;
    }

    private static class WrappedCallBack implements TaskCallback {

        private final InterruptableTaskCallback parent;

        public WrappedCallBack(InterruptableTaskCallback parent) {
            this.parent = parent;
        }

        public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
            Task nextTask = parent.onResponse(task, response, stdOut, stdErr);
            if (nextTask != null) {
                parent.lastTask = nextTask;
            }
            return nextTask;
        }
    }
}
