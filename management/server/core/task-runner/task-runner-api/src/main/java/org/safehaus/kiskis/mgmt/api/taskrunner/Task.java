/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.taskrunner;

import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.doomdark.uuid.UUIDGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;

/**
 * This class represents a task which is essentially a single request to a group
 * (one or more) of agents(nodes) to be executed in parallel.
 *
 * The following code creates a task and adds a request for an agent to it:
 * <p>
 * <blockquote><pre>
 *    Task task = new Task();
 *    task.addRequest(myRequest, someAgent);
 * </pre></blockquote>
 * </p>
 *
 * @author dilshat
 */
public class Task implements Serializable {

    private UUID uuid;
    private String description;
    private volatile TaskStatus taskStatus;
    private Integer reqSeqNumber;
    private final List<Request> requests;
    private boolean ignoreExitCode = false;
    private volatile boolean completed = false;
    private int currentCmdId = -1;
    private int completedCommandsCount = 0;
    private int succeededCommandsCount = 0;
    private Object data;
    private Map<UUID, Result> results;

    /**
     * Initalizes a newly created {@code Task}.
     */
    public Task() {
        taskStatus = TaskStatus.NEW;
        uuid = java.util.UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
        reqSeqNumber = 0;
        requests = new ArrayList<Request>();
        results = new HashMap<UUID, Result>();
    }

    /**
     * Initalizes a newly created {@code Task} with supplied description.
     *
     * @param description - task description
     */
    public Task(String description) {
        this();
        this.description = description;
    }

    /**
     * Returns map where key is UUID of agent/node and value is {@code Result}
     *
     * @return map of results of task execution
     */
    public Map<UUID, Result> getResults() {
        return Collections.unmodifiableMap(results);
    }

    /**
     * Add result to results map.
     *
     * @param id UUID of agent/node
     * @param result {@code Result}
     */
    public void addResult(UUID id, Result result) {
        results.put(id, result);
    }

    /**
     * Returns custom object attached to this task.
     *
     * @return object attached to this task
     */
    public Object getData() {
        return data;
    }

    /**
     * Lets attach custom object to this task
     *
     * @param data
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * Increment count of completed requests. The counter is incremented when
     * EXECUTE_RESPONSE_DONE command is received from agent
     */
    public void incrementCompletedRequestsCount() {
        completedCommandsCount++;
    }

    /**
     * Increment count of succeeded requests. The counter is incremented when
     * EXECUTE_RESPONSE_DONE command is received from agent and the received
     * exit code is 0
     */
    public void incrementSucceededRequestsCount() {
        succeededCommandsCount++;
    }

    /**
     * Returns count of completed requests. Request is considered completed if
     * corresponding EXECUTE_RESPONSE_DONE response has been received.
     *
     * @return count of completed requests
     */
    public int getCompletedRequestsCount() {
        return completedCommandsCount;
    }

    /**
     * Returns count of succeeded requests. Request is considered completed if
     * corresponding EXECUTE_RESPONSE_DONE response has been received and its
     * exit code is 0.
     *
     * @return count of succeeded requests
     */
    public int getSucceededRequestsCount() {
        return succeededCommandsCount;
    }

    /**
     * Add request to this task. This method is deprecated in favor of
     * addRequest(request, agent)
     *
     * @param request
     */
    @Deprecated
    public void addRequest(Request request) {
        if (request != null) {
            request.setTaskUuid(uuid);
            request.setRequestSequenceNumber(getIncrementedReqSeqNumber());
            requests.add(request);
        }
    }

    /**
     * Add request to this task and binds it to the specified agent.
     *
     * @param request
     * @param agent
     */
    public void addRequest(Request request, Agent agent) {
        if (request != null && agent != null) {
            request.setUuid(agent.getUuid());
            request.setTaskUuid(uuid);
            request.setRequestSequenceNumber(getIncrementedReqSeqNumber());
            request.setSource(TaskRunner.MODULE_NAME);
            requests.add(request);
        }
    }

    /**
     * Returns total amount of timeouts. This method returns sum of timeouts of
     * all requests added to this task
     *
     * @return sum of timeouts of all requests
     */
    public int getTotalTimeout() {
        int timeout = 0;
        for (Request cmd : requests) {
            timeout += cmd.getTimeout();
        }
        return timeout;
    }

    /**
     * Returns average amount of timeouts. This method returns average of
     * timeouts of all requests added to this task
     *
     * @return average of timeouts of all requests
     */
    public int getAvgTimeout() {
        return requests.size() > 0 ? getTotalTimeout() / requests.size() : 0;
    }

    /**
     * Return next request from the list of requests added to this task or null
     * if no requests are left. Request is returned in LIFO order
     *
     * @return
     */
    public Request getNextRequest() {
        if (hasNextRequest()) {
            currentCmdId++;
            return requests.get(currentCmdId);
        }

        return null;
    }

    /**
     * Returns next request but does not move pointer in the list of requests.
     * This method is usable when it is needed to modify contents of next
     * request based on the outcome of the previous request(s)
     *
     * @return
     */
    public Request peekNextRequest() {
        if (hasNextRequest()) {
            return requests.get(currentCmdId + 1);
        }
        return null;
    }

    /**
     * Returns previous request.
     *
     * @return
     */
    public Request peekPreviousRequest() {
        if (currentCmdId > 0) {
            return requests.get(currentCmdId - 1);
        }
        return null;
    }

    /**
     * Returns current request.
     *
     * @return
     */
    public Request peekCurrentRequest() {
        if (currentCmdId >= 0) {
            return requests.get(currentCmdId);
        }
        return null;
    }

    /**
     * Returns boolean indicating if any requests not fetched yet by
     * getNextRequest are left.
     *
     * @return boolean indicating if any unstepped request is left
     */
    public boolean hasNextRequest() {
        return currentCmdId < requests.size() - 1;
    }

    /**
     * Return count of requests already sent for execution.
     *
     * @return count of requests already sent for execution
     */
    public int getLaunchedRequestsCount() {
        return currentCmdId + 1;
    }

    /**
     * Returns boolean indicating if ignoreExitCode flag is set
     *
     * @return boolean indicating if ignoreExitCode flag is set
     */
    public boolean isIgnoreExitCode() {
        return ignoreExitCode;
    }

    /**
     * Sets ignoreExitCode flag for this task. If this flag is set all completed
     * commands regardless of their exit code and whether they are timed out
     * will be considered as successful
     *
     * @param ignoreExitCode true/false
     */
    public void setIgnoreExitCode(boolean ignoreExitCode) {
        this.ignoreExitCode = ignoreExitCode;
    }

    /**
     * Returns boolean indicating if this task completed. This method will
     * return true if for all sent requests in this task corresponding
     * EXECURE_RESPONSE_DONE responses have been received
     *
     * @return
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Sets completed flag
     *
     * @param completed
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Returns the list of all request commands added to this task
     *
     * @return list of all requests
     */
    public List<Request> getRequests() {
        return Collections.unmodifiableList(requests);
    }

    /**
     * Gets next incremented request sequence number and increment request
     * sequence number counter
     *
     * @return next incremented request sequence number
     */
    public Integer getIncrementedReqSeqNumber() {
        reqSeqNumber++;
        return reqSeqNumber;
    }

    /**
     * Returns current request sequence number.
     *
     * @return current request sequence number
     */
    public Integer getReqSeqNumber() {
        return reqSeqNumber;
    }

    /**
     * Sets request sequence number
     *
     * @param reqSeqNumber
     */
    public void setReqSeqNumber(Integer reqSeqNumber) {
        this.reqSeqNumber = reqSeqNumber;
    }

    /**
     * Returns UUID of this task
     *
     * @return UUID of this task
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Sets UUID of this task
     *
     * @param uuid
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Return status of this task.
     *
     * @return status of this task {@code TaskStatus}
     */
    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    /**
     * Sets status of this task
     *
     * @param taskStatus {@code TaskStatus}
     */
    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    /**
     * Returns description of this task or null if no description was set
     *
     * @return description of this task or null if no description was set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description of this task
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Return first encountered std err from the list of results of this task.
     * Since task is assumed to contain single request to a set of agents this
     * method can be used to retrieve any first error to show in UI or logs
     *
     * @return first encountered std err from the list of results of this task
     */
    public String getFirstError() {
        String err = "";
        for (Map.Entry<UUID, Result> res : results.entrySet()) {
            if (!Util.isStringEmpty(res.getValue().getStdErr())) {
                err = res.getValue().getStdErr();
                break;
            }
        }
        return err;
    }

    @Override
    public String toString() {
        return "Task{" + "uuid=" + uuid + ", description=" + description + ", taskStatus=" + taskStatus + ", reqSeqNumber=" + reqSeqNumber + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.uuid != null ? this.uuid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Task other = (Task) obj;
        return !(this.uuid != other.getUuid() && (this.uuid == null || !this.uuid.equals(other.getUuid())));
    }

}
