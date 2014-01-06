/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.command;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author dilshat
 */
public class SerialExecutor implements Executor {

    private final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
    private final ExecutorService executor;
    private Runnable active;

    public SerialExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public synchronized void execute(final Runnable r) {
        tasks.add(new Runnable() {
            public void run() {
                try {
                    r.run();
                } finally {
                    scheduleNext();
                }
            }
        });
        if (active == null) {
            scheduleNext();
        }
    }

    protected synchronized void scheduleNext() {
        if ((active = tasks.poll()) != null) {
            executor.execute(active);
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
