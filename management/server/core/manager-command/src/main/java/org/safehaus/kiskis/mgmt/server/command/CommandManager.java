package org.safehaus.kiskis.mgmt.server.command;

import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.CommandListener;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 11:16 PM
 */
public class CommandManager implements CommandManagerInterface, ResponseListener {

    private static final Logger LOG = Logger.getLogger(CommandManager.class.getName());
    private PersistenceInterface persistenceCommand;
    private CommandTransportInterface communicationService;
    private final Map<CommandListener, ReentrantLock> listeners = new ConcurrentHashMap<CommandListener, ReentrantLock>();
    private ExecutorService notifierExecService;
//    private CommandNotifier commandNotifier;

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
//                commandNotifier.addResponse(response);
                notify(response);
                break;
            }
            case EXECUTE_RESPONSE_DONE: {
                persistenceCommand.saveResponse(response);
//                commandNotifier.addResponse(response);
                notify(response);
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
            listeners.put(listener, new ReentrantLock(true));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in addListener", ex);
        }
    }

    @Override
    public void removeListener(CommandListener listener) {
        try {
            LOG.log(Level.INFO, "Removing module listener : {0}", listener.getName());
            listeners.remove(listener);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in removeListener", ex);
        }
    }

    public void init() {
        try {
            if (communicationService != null) {
                notifierExecService = Executors.newCachedThreadPool();
//                exec = Executors.newSingleThreadExecutor();
//                commandNotifier = new CommandNotifier(listeners);
//                exec.execute(commandNotifier);
                communicationService.addListener(this);
            } else {
                throw new Exception("Missing communication service");
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in init", ex);
        }
    }

    private void notify(Response response) {
        try {
            for (Iterator<Entry<CommandListener, ReentrantLock>> it = listeners.entrySet().iterator(); it.hasNext();) {
                Entry<CommandListener, ReentrantLock> listenerEntry = it.next();
                if (listenerEntry.getKey() != null && listenerEntry.getValue() != null
                        && listenerEntry.getKey().getName() != null) {
                    CommandListener listener = listenerEntry.getKey();
                    ReentrantLock lock = listenerEntry.getValue();
                    if (response != null && response.getSource() != null
                            && listener.getName().equalsIgnoreCase(response.getSource())) {
                        notifierExecService.execute(new ResponseNotifier(lock, listener, response));
                    }
                } else {
                    it.remove();
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in notify", ex);
        }
    }

    public void destroy() {
        try {
            notifierExecService.shutdown();
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

    @Override
    public List<Request> getCommands(UUID taskuuid) {
        return persistenceCommand.getRequests(taskuuid);
    }

    @Override
    public Integer getResponseCount(UUID taskuuid) {
        return persistenceCommand.getResponsesCount(taskuuid);
    }

    @Override
    public Integer getSuccessfullResponseCount(UUID taskuuid) {
        return persistenceCommand.getSuccessfullResponsesCount(taskuuid);
    }

    @Override
    public Response getResponse(UUID taskuuid, Integer requestSequenceNumber) {
        Response response = null;
        try {
            List<Response> list = persistenceCommand.getResponses(taskuuid, requestSequenceNumber);

            String stdOut = "", stdErr = "";
            for (Response r : list) {
                response = r;
                if (r.getStdOut() != null && !r.getStdOut().equalsIgnoreCase("null") && !Util.isStringEmpty(r.getStdOut())) {
                    stdOut += r.getStdOut();
                }
                if (r.getStdErr() != null && !r.getStdErr().equalsIgnoreCase("null") && !Util.isStringEmpty(r.getStdErr())) {
                    stdErr += r.getStdErr();
                }
            }

            if (response != null) {
                response.setStdOut(stdOut);
                response.setStdErr(stdErr);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getResponse", ex);
        }
        return response;
    }

    @Override
    public void saveResponse(Response response) {
        persistenceCommand.saveResponse(response);
    }

    @Override
    public String saveTask(Task task) {
        return persistenceCommand.saveTask(task);
    }

    @Override
    public List<Task> getTasks() {
        return persistenceCommand.getTasks();
    }

    @Override
    public boolean truncateTables() {
        return persistenceCommand.truncateTables();
    }

    @Override
    public boolean saveCassandraClusterData(CassandraClusterInfo cluster) {
        return persistenceCommand.saveCassandraClusterInfo(cluster);
    }

    @Override
    public List<CassandraClusterInfo> getCassandraClusterData() {
        return persistenceCommand.getCassandraClusterInfo();
    }

    @Override
    public List<HadoopClusterInfo> getHadoopClusterData() {
        return persistenceCommand.getHadoopClusterInfo();
    }

    @Override
    public HadoopClusterInfo getHadoopClusterData(String clusterName) {
        return persistenceCommand.getHadoopClusterInfo(clusterName);
    }

    @Override
    public boolean saveHadoopClusterData(HadoopClusterInfo cluster) {
        return persistenceCommand.saveHadoopClusterInfo(cluster);
    }

    @Override
    public Task getTask(UUID uuid) {
        return persistenceCommand.getTask(uuid);
    }

    @Override
    public List<ParseResult> parseTask(UUID taskUuid, boolean isResponseDone) {
        List<ParseResult> result = new ArrayList<ParseResult>();

        if (persistenceCommand != null) {
            List<Request> requestList = persistenceCommand.getRequests(taskUuid);
            Integer responseCount = persistenceCommand.getResponsesCount(taskUuid);

            if (isResponseDone) {
                if (requestList.size() != responseCount) {
                    return result;
                }
            }

            Integer exitCode = 0;
            for (Request request : requestList) {
                Response response = getResponse(taskUuid, request.getRequestSequenceNumber());
                if (response != null) {
                    result.add(new ParseResult(request, response));
                    if (response.getType().compareTo(ResponseType.EXECUTE_RESPONSE_DONE) == 0) {
                        exitCode += response.getExitCode();
                    } else if (response.getType().compareTo(ResponseType.EXECUTE_TIMEOUTED) == 0) {
                        exitCode = 1;
                    }
                }
            }

            if (requestList.size() == responseCount) {
                Task task = getTask(taskUuid);
                if (exitCode == 0) {
                    task.setTaskStatus(TaskStatus.SUCCESS);
                } else {
                    task.setTaskStatus(TaskStatus.FAIL);
                }
                persistenceCommand.saveTask(task);
            }
        }

        return result;
    }

    public boolean deleteCassandraClusterData(UUID uuid) {
        return persistenceCommand.deleteCassandraClusterInfo(uuid);
    }

    public String getSource() {
        return getClass().getName();
    }
}
