/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.command;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;
import java.util.Iterator;
import java.util.Queue;
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
    private final EvictingQueue<Response> queue = EvictingQueue.create(Common.MAX_PENDING_MESSAGE_QUEUE_LENGTH);
    protected final Queue<Response> messagesQueue = Queues.synchronizedQueue(queue);
    private final Queue<CommandListener> listeners;

    public CommandNotifier(Queue<CommandListener> listeners) {
        this.listeners = listeners;
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

    private void notifyListeners(Response response) {
        try {
            for (Iterator<CommandListener> it = listeners.iterator(); it.hasNext();) {
                CommandListener listener = it.next();
                if (listener != null && listener.getName() != null) {
                    if (response != null && response.getSource() != null && listener.getName().equals(response.getSource())) {
                        listener.onCommand(response);
                    }
                } else {
                    it.remove();
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in notifyListeners", ex);
        }
    }
}
