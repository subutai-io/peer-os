/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.commandrunner;

import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentRequestBuilder;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandStatus;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 *
 * @author dilshat
 */
public class CommandImpl implements Command {

    private final Map<UUID, AgentResult> results = new HashMap<UUID, AgentResult>();
    /**
     * Status of command
     */
    private final int requestsToRun;
    private final UUID commandUUID;
    private final Set<Request> requests = new HashSet<Request>();
    private final int timeout;
    private final Semaphore completionSemaphore = new Semaphore(0);
    private final Lock updateLock = new ReentrantLock(true);

    private volatile CommandStatus commandStatus = CommandStatus.NEW;
    private volatile int requestsCompleted = 0;
    private volatile int requestsSucceeded = 0;
    private Object data;
    private final String description;

    public CommandImpl(String description, RequestBuilder requestBuilder, Set<Agent> agents) {

        Preconditions.checkNotNull(requestBuilder, "Request Builder is null");
        Preconditions.checkArgument(agents != null && !agents.isEmpty(), "Agents are null or empty");

        this.description = description;
        this.commandUUID = Util.generateTimeBasedUUID();
        this.requestsToRun = agents.size();
        this.timeout = requestBuilder.getTimeout();

        for (Agent agent : agents) {
            requests.add(requestBuilder.build(agent.getUuid(), commandUUID));
        }
    }

    public CommandImpl(String description, Set<AgentRequestBuilder> requestBuilders) {
        Preconditions.checkArgument(requestBuilders != null && !requestBuilders.isEmpty(), "Request Builders are null or empty");

        this.description = description;
        this.commandUUID = Util.generateTimeBasedUUID();
        this.requestsToRun = requestBuilders.size();

        int maxTimeout = 0;
        for (AgentRequestBuilder requestBuilder : requestBuilders) {
            requests.add(requestBuilder.build(requestBuilder.getAgent().getUuid(), commandUUID));
            if (requestBuilder.getTimeout() > maxTimeout) {
                maxTimeout = requestBuilder.getTimeout();
            }
        }

        this.timeout = maxTimeout;
    }

    public boolean hasCompleted() {
        return commandStatus == CommandStatus.FAILED || commandStatus == CommandStatus.SUCCEEDED;
    }

    public boolean hasSucceeded() {
        return commandStatus == CommandStatus.SUCCEEDED;
    }

    public CommandStatus getCommandStatus() {
        return commandStatus;
    }

    public Map<UUID, AgentResult> getResults() {
        return Collections.unmodifiableMap(results);
    }

    public void appendResult(Response response) {
        if (response != null && response.getUuid() != null) {

            AgentResultImpl agentResult = (AgentResultImpl) results.get(response.getUuid());
            if (agentResult == null) {
                agentResult = new AgentResultImpl(response.getUuid());
                results.put(agentResult.getAgentUUID(), agentResult);
            }

            agentResult.appendResults(response);

            if (response.isFinal()) {
                incrementCompletedRequestsCount();
                if (response.hasSucceeded()) {
                    incrementSucceededRequestsCount();
                }
                if (getRequestsCompleted() == getRequestsRun()) {
                    if (getRequestsCompleted() == getRequestsSucceeded()) {
                        setCommandStatus(CommandStatus.SUCCEEDED);
                    } else {
                        setCommandStatus(CommandStatus.FAILED);
                    }
                }
            }
        }
    }

    public void incrementCompletedRequestsCount() {
        requestsCompleted++;
    }

    public void incrementSucceededRequestsCount() {
        requestsSucceeded++;
    }

    public void waitCompletion() {
        try {
            completionSemaphore.acquire();
        } catch (InterruptedException ex) {
        }
    }

    public void notifyWaitingThreads() {
        completionSemaphore.release();
    }

    public void getUpdateLock() {
        updateLock.lock();
    }

    public void releaseUpdateLock() {
        updateLock.unlock();
    }

    public int getTimeout() {
        return timeout;
    }

    public Set<Request> getRequests() {
        return Collections.unmodifiableSet(requests);
    }

    public void setCommandStatus(CommandStatus commandStatus) {
        this.commandStatus = commandStatus;
    }

    public int getRequestsRun() {
        return requestsToRun;
    }

    public int getRequestsCompleted() {
        return requestsCompleted;
    }

    public int getRequestsSucceeded() {
        return requestsSucceeded;
    }

    public UUID getCommandUUID() {
        return commandUUID;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public String getDescription() {
        return description;
    }

    public String getAllErrors() {
        StringBuilder errors = new StringBuilder();
        for (Map.Entry<UUID, AgentResult> result : results.entrySet()) {
            AgentResult agentResult = result.getValue();
            errors.append(agentResult.getAgentUUID()).
                    append(": ").
                    append(agentResult.getStdErr()).
                    append("; Exit code: ").
                    append(agentResult.getExitCode()).
                    append("\n");
        }
        return errors.toString();
    }

    @Override
    public String toString() {
        return "CommandImpl{" + "results=" + results + ", requestsToRun=" + requestsToRun + ", commandUUID=" + commandUUID + ", requests=" + requests + ", timeout=" + timeout + ", commandStatus=" + commandStatus + ", requestsCompleted=" + requestsCompleted + ", requestsSucceeded=" + requestsSucceeded + '}';
    }

}
