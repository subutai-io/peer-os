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
class CommandImpl implements Command {

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
    private int requestsCompleted = 0;
    private int requestsSucceeded = 0;

    public CommandImpl(RequestBuilder requestBuilder, Set<Agent> agents) {

        Preconditions.checkNotNull(requestBuilder, "Request Builder is null");
        Preconditions.checkArgument(agents != null && !agents.isEmpty(), "Agents are null or empty");

        this.commandUUID = Util.generateTimeBasedUUID();
        this.requestsToRun = agents.size();
        this.timeout = requestBuilder.getTimeout();

        for (Agent agent : agents) {
            requests.add(requestBuilder.build(agent.getUuid(), commandUUID));
        }
    }

    public boolean hasCompleted() {
        return commandStatus == CommandStatus.FAILED || commandStatus == CommandStatus.SUCCEEDED;
    }

    public CommandStatus getCommandStatus() {
        return commandStatus;
    }

    public Map<UUID, AgentResult> getResults() {
        return Collections.unmodifiableMap(results);
    }

    void appendResult(Response response) {
        if (response != null && response.getUuid() != null) {

            AgentResultImpl agentResult = (AgentResultImpl) results.get(response.getUuid());
            if (agentResult == null) {
                agentResult = new AgentResultImpl(response.getUuid());
                results.put(agentResult.getAgentUUID(), agentResult);
            }

            agentResult.appendResults(response);

            if (response.isFinal()) {
                requestsCompleted++;
                if (response.hasSucceeded()) {
                    requestsSucceeded++;
                }
                if (requestsCompleted == requestsToRun) {
                    if (requestsCompleted == requestsSucceeded) {
                        commandStatus = CommandStatus.SUCCEEDED;
                    } else {
                        commandStatus = CommandStatus.FAILED;
                    }
                }
            }
        }
    }

    void waitCompletion() {
        try {
            completionSemaphore.acquire();
        } catch (InterruptedException ex) {
        }
    }

    void notifyWaitingThreads() {
        completionSemaphore.release();
    }

    void getUpdateLock() {
        updateLock.lock();
    }

    void releaseUpdateLock() {
        updateLock.unlock();
    }

    int getTimeout() {
        return timeout;
    }

    Set<Request> getRequests() {
        return Collections.unmodifiableSet(requests);
    }

    void setCommandStatus(CommandStatus commandStatus) {
        this.commandStatus = commandStatus;
    }

    int getRequestsRun() {
        return requestsToRun;
    }

    int getRequestsCompleted() {
        return requestsCompleted;
    }

    int getRequestsSucceeded() {
        return requestsSucceeded;
    }

    UUID getCommandUUID() {
        return commandUUID;
    }
}
