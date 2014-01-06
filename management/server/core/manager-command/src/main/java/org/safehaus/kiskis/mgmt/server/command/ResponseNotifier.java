/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.command;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

/**
 *
 * @author dilshat
 */
public class ResponseNotifier implements Runnable {

    private static final Logger LOG = Logger.getLogger(ResponseNotifier.class.getName());

    private final ReentrantLock lock;
    private final CommandListener listener;
    private final Response response;

    public ResponseNotifier(ReentrantLock lock, CommandListener listener, Response response) {
        this.lock = lock;
        this.listener = listener;
        this.response = response;
    }

    public void run() {
        lock.lock();
        try {
            System.out.println("PROCESSING RESPONSE 0 " + response);

            listener.onCommand(response);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error notifying response listener", e);
        } finally {
            lock.unlock();
        }
    }

}
