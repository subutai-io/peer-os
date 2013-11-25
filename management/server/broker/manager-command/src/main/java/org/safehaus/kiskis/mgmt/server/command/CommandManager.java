package org.safehaus.kiskis.mgmt.server.command;

//import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.BrokerListener;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 11:16 PM
 */
public class CommandManager implements CommandManagerInterface, BrokerListener {

    private static final Logger LOG = Logger.getLogger(CommandManager.class.getName());
//    private BundleContext context;
    private PersistenceInterface persistenceCommand;
    private CommandTransportInterface communicationService;
    private ArrayList<CommandListener> listeners = new ArrayList<CommandListener>();

//    @Override
//    public List<Command> getCommandList(Agent agent) {
//        System.out.println(this.getClass().getName() + " getCommandList called");
//        System.out.println(agent.toString());
//        return null;
//    }
    @Override
    public void executeCommand(Command command) {
        try {
            if (persistenceCommand.saveCommand(command)) {
                communicationService.sendCommand(command);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in executeCommand", ex);
        }
    }

    @Override
    public synchronized void getCommand(Response response) {
        System.out.println("Response received by CommandManager");
        persistenceCommand.saveResponse(response);

        switch (response.getType()) {
            case EXECUTE_TIMEOUTED:
            case EXECUTE_RESPONSE: {
                notifyListeners(response);
                break;
            }
            case EXECUTE_RESPONSE_DONE: {
                notifyListeners(response);
                break;
            }
            default: {
                break;
            }
        }
    }

    private void notifyListeners(Response response) {
        try {
            System.out.println("Количество модулей:" + listeners.size());
            for (CommandListener ai : (ArrayList<CommandListener>) listeners.clone()) {
                if (ai != null && ai.getName() != null && response.getSource() != null) {
                    if (ai.getName().equals(response.getSource())) {
                        System.out.println("~~~~~~~ Listeners notified");
                        ai.outputCommand(response);
                    } else {
                        System.out.println("~~~~~~~ Notify all");
                        ai.outputCommand(response);
                    }

                } else {
                    listeners.remove(ai);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in notifyListeners", ex);
        }
    }

    @Override
    public synchronized void addListener(CommandListener listener) {
        try {
            System.out.println("Adding module listener : " + listener.getName());
            listeners.add(listener);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in addListener", ex);
        }
    }

    @Override
    public synchronized void removeListener(CommandListener listener) {
        try {
            System.out.println("Removing module listener : " + listener.getName());
            listeners.remove(listener);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in removeListener", ex);
        }
    }

    public void init() {
        try {
            if (communicationService != null) {
                communicationService.addListener(this);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in init", ex);
        }
    }

    public void destroy() {
        try {
            if (communicationService != null) {
                communicationService.removeListener(this);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in destroy", ex);
        }
    }

    public void setPersistenceCommandService(PersistenceInterface persistenceCommand) {
        this.persistenceCommand = persistenceCommand;
    }

    public void setCommunicationService(CommandTransportInterface communicationService) {
        this.communicationService = communicationService;
    }

//    public void setContext(BundleContext context) {
//        this.context = context;
//    }
//    private CommandTransportInterface getCommandTransport() {
//        if (context != null) {
//            ServiceReference reference = context
//                    .getServiceReference(CommandTransportInterface.class.getName());
//            if (reference != null) {
//                return (CommandTransportInterface) context.getService(reference);
//            }
//        }
//
//        return null;
//    }
    public List<Request> getCommands() {
        try {
            return persistenceCommand.getRequests("taskuuid");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getCommands", ex);
        }
        return null;
    }

    public List<Response> getResponses() {
        try {
            return persistenceCommand.getResponses("taskuuid");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getResponses", ex);
        }
        return null;
    }

    public void saveResponse(Response response) {
        persistenceCommand.saveResponse(response);
    }

    public List<Task> getTasks() {
        return persistenceCommand.getTasks();
    }
}
