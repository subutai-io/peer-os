/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandImpl;
import org.safehaus.kiskis.mgmt.shared.protocol.RequestUtil;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommunicationService;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

/**
 *
 * @author dilshat
 */
public class CommandManagerImpl implements ResponseListener, org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManager {

    private static final Logger LOG = Logger.getLogger(CommandManagerImpl.class.getName());
    private CommunicationService communicationService;
    private final Map<CommandListener, ExecutorService> listeners = new ConcurrentHashMap<CommandListener, ExecutorService>();
    private ExecutorService notifierExecService;
    private CommandNotifier commandNotifier;

    @Override
    public boolean executeCommand(CommandImpl command) {
        try {
            RequestUtil.saveCommand(command);//temporary until parseTask is removed
            communicationService.sendCommand(command);
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in executeCommand", ex);
        }
        return false;
    }

    @Override
    public void onResponse(Response response) {
        switch (response.getType()) {
            case EXECUTE_TIMEOUTED:
            case EXECUTE_RESPONSE:
            case EXECUTE_RESPONSE_DONE: {
                RequestUtil.saveResponse(response);//temporary until parseTask is removed
                commandNotifier.addResponse(response);
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void addListener(CommandListener listener) {
        try {
            LOG.log(Level.INFO, "Adding module listener : {0}", listener.getName());
            listeners.put(listener, Executors.newSingleThreadExecutor());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in addListener", ex);
        }
    }

    @Override
    public void removeListener(CommandListener listener) {
        try {
            LOG.log(Level.INFO, "Removing module listener : {0}", listener.getName());
            ExecutorService exec = listeners.remove(listener);
            if (exec != null) {
                exec.shutdown();
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in removeListener", ex);
        }
    }

    public void init() {
        try {
            if (communicationService != null) {
                notifierExecService = Executors.newSingleThreadExecutor();
                commandNotifier = new CommandNotifier(listeners);
                notifierExecService.execute(commandNotifier);
                communicationService.addListener(this);
            } else {
                throw new Exception("Missing communication service");
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in init", ex);
        }
    }

    public void destroy() {
        try {
            notifierExecService.shutdown();

            for (Map.Entry<CommandListener, ExecutorService> entry : listeners.entrySet()) {
                entry.getValue().shutdown();
            }

            if (communicationService != null) {
                communicationService.removeListener(this);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in destroy", ex);
        }
    }

    public void setCommunicationService(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

}
