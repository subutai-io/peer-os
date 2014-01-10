/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.command;

import com.google.common.collect.EvictingQueue;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class CommandNotifier implements Runnable {

    private static final Logger LOG = Logger.getLogger(CommandNotifier.class.getName());
    private final EvictingQueue<Response> messagesQueue = EvictingQueue.create(Common.MAX_PENDING_MESSAGE_QUEUE_LENGTH);
    private final Map<CommandListener, ExecutorService> listeners;

    public CommandNotifier(Map<CommandListener, ExecutorService> listeners) {
        this.listeners = listeners;
    }

    public void addResponse(Response response) {
        synchronized (messagesQueue) {
            messagesQueue.add(response);
        }
    }

    public void run() {
        while (!Thread.interrupted()) {
            try {
                Response[] responses = null;
                synchronized (messagesQueue) {
                    if (!messagesQueue.isEmpty()) {
                        responses = messagesQueue.toArray(new Response[messagesQueue.size()]);
                        messagesQueue.clear();
                    }
                }
                if (responses != null) {
                    for (Response response : responses) {
                        notifyListeners(response);
                    }
                }
                Thread.sleep(1000);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Error in CommandNotifier.run", ex);
            }
        }
    }

    private void notifyListeners(final Response response) {
        try {
            for (Iterator<Entry<CommandListener, ExecutorService>> it = listeners.entrySet().iterator(); it.hasNext();) {
                Entry<CommandListener, ExecutorService> listenerEntry = it.next();
                if (listenerEntry != null && listenerEntry.getKey() != null
                        && listenerEntry.getValue() != null && listenerEntry.getKey().getName() != null) {
                    final CommandListener listener = listenerEntry.getKey();
                    ExecutorService exec = listenerEntry.getValue();
                    if (response != null && response.getSource() != null
                            && listener.getName().equals(response.getSource())) {
                        exec.execute(new Runnable() {

                            public void run() {
                                try {
                                    listener.onCommand(response);
                                } catch (Exception e) {
                                    LOG.log(Level.SEVERE, "Error notifying response listener", e);
                                }
                            }
                        });
                    }
                } else {
                    try {
                        it.remove();
                        if (listenerEntry != null && listenerEntry.getValue() != null) {
                            listenerEntry.getValue().shutdown();
                        }
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in notifyListeners", ex);
        }
    }
}
