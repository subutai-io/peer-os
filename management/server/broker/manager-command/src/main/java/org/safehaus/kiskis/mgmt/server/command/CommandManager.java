package org.safehaus.kiskis.mgmt.server.command;

//import org.osgi.framework.ServiceReference;

import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.api.BrokerListener;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.CassandraClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 11:16 PM
 */
public class CommandManager implements CommandManagerInterface, BrokerListener {

    private static final Logger LOG = Logger.getLogger(CommandManager.class.getName());

    private PersistenceInterface persistenceCommand;
    private CommandTransportInterface communicationService;
    private final ArrayList<CommandListener> listeners = new ArrayList<CommandListener>();

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
        switch (response.getType()) {
            case EXECUTE_TIMEOUTED:
            case EXECUTE_RESPONSE: {
                persistenceCommand.saveResponse(response);
                notifyListeners(response);
                break;
            }
            case EXECUTE_RESPONSE_DONE: {
                persistenceCommand.saveResponse(response);
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
            System.out.println("Module count: " + listeners.size());
            for (CommandListener ai : (ArrayList<CommandListener>) listeners.clone()) {
                if (ai != null && ai.getName() != null && response.getSource() != null) {
                    if (ai.getName().equals(response.getSource())) {
                        ai.outputCommand(response);
                    } else {
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

    public List<Request> getCommands(UUID taskuuid) {
        try {
            return persistenceCommand.getRequests(taskuuid);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getCommands", ex);
        }
        return null;
    }

    public Response getResponse(UUID taskuuid, Integer requestSequenceNumber) {
        Response response = null;
        try {
            List<Response> list = persistenceCommand.getResponses(taskuuid, requestSequenceNumber);

            String stdOut = "", stdErr = "";
            for (Response r : list) {
                response = r;
                if (r.getStdOut() != null && !r.getStdOut().equalsIgnoreCase("null")) {
                    stdOut += "\n" + r.getStdOut();
                }
                if (r.getStdErr() != null && !r.getStdErr().equalsIgnoreCase("null")) {
                    stdErr += "\n" + r.getStdErr();
                }
            }

            if (response != null) {
                response.setStdOut(stdOut);
                response.setStdErr(stdErr);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getCommands", ex);
        }
        return response;
    }

    public void saveResponse(Response response) {
        persistenceCommand.saveResponse(response);
    }

    public String saveTask(Task task) {
        return persistenceCommand.saveTask(task);
    }

    public List<Task> getTasks() {
        return persistenceCommand.getTasks();
    }

    public boolean truncateTables() {
        return persistenceCommand.truncateTables();
    }

    public boolean saveClusterData(CassandraClusterInfo cluster) {
        return  persistenceCommand.saveCassandraClusterInfo(cluster);
    }

    public List<CassandraClusterInfo> getClusterData() {
        return persistenceCommand.getCassandraClusterInfo();
    }
}
