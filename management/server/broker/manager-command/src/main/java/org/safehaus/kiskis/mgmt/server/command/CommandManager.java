package org.safehaus.kiskis.mgmt.server.command;

import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.BrokerListener;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 11:16 PM
 */
public class CommandManager implements CommandManagerInterface, BrokerListener {

    private static final Logger LOG = Logger.getLogger(CommandManager.class.getName());
    private PersistenceInterface persistenceCommand;
    private CommandTransportInterface communicationService;
    private final ConcurrentLinkedQueue<CommandListener> listeners = new ConcurrentLinkedQueue<CommandListener>();

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
    public void onResponse(Response response) {
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
            for (CommandListener ai : listeners) {
                if (ai != null && ai.getName() != null) {
                    if (response != null && response.getSource() != null && ai.getName().equals(response.getSource())) {
                        ai.onCommand(response);
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
    public void addListener(CommandListener listener) {
        try {
            System.out.println("Adding module listener : " + listener.getName());
            listeners.add(listener);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in addListener", ex);
        }
    }

    @Override
    public void removeListener(CommandListener listener) {
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
            } else {
                throw new Exception("Missing communication service");
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

    @Override
    public Integer getResponseCount(UUID taskuuid) {
        return persistenceCommand.getResponsesCount(taskuuid);
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

    public boolean saveCassandraClusterData(CassandraClusterInfo cluster) {
        return persistenceCommand.saveCassandraClusterInfo(cluster);
    }

    public List<CassandraClusterInfo> getCassandraClusterData() {
        return persistenceCommand.getCassandraClusterInfo();
    }

    @Override
    public List<ParseResult> parseTask(Task task, boolean isResponseDone){
        List<ParseResult> result = new ArrayList<ParseResult>();

        if(persistenceCommand != null){
            List<Request> requestList = persistenceCommand.getRequests(task.getUuid());
            Integer responseCount = persistenceCommand.getResponsesCount(task.getUuid());

            if(isResponseDone){
                if(requestList.size() != responseCount){
                    return result;
                }
            }

            Integer exitCode = 0;
            for(Request request : requestList){
                Response response = getResponse(task.getUuid(), request.getRequestSequenceNumber());

                if(response != null){
                    result.add(new ParseResult(request, response));
                    exitCode += response.getExitCode();
                }
            }

            if(isResponseDone){
                if(exitCode == 0){
                    task.setTaskStatus(TaskStatus.SUCCESS);
                } else {
                    task.setTaskStatus(TaskStatus.FAIL);
                }
                persistenceCommand.saveTask(task);
            }
        }

        return result;
    }
}
