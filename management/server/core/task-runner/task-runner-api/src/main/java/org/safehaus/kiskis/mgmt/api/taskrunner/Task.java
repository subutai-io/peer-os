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

    public Task() {
        taskStatus = TaskStatus.NEW;
        uuid = java.util.UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
        reqSeqNumber = 0;
        requests = new ArrayList<Request>();
        results = new HashMap<UUID, Result>();
    }

    public Task(String description) {
        this();
        this.description = description;
    }

    public Map<UUID, Result> getResults() {
        return Collections.unmodifiableMap(results);
    }

    public void addResult(UUID id, Result result) {
        results.put(id, result);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void incrementCompletedRequestsCount() {
        completedCommandsCount++;
    }

    public void incrementSucceededRequestsCount() {
        succeededCommandsCount++;
    }

    public int getCompletedRequestsCount() {
        return completedCommandsCount;
    }

    public int getSucceededRequestsCount() {
        return succeededCommandsCount;
    }

    public void addRequest(Request request) {
        if (request != null) {
            request.setTaskUuid(uuid);
            request.setRequestSequenceNumber(getIncrementedReqSeqNumber());
            requests.add(request);
        }
    }

    public void addRequest(Request request, Agent agent) {
        if (request != null && agent != null) {
            request.setUuid(agent.getUuid());
            addRequest(request);
        }
    }

    public int getTotalTimeout() {
        int timeout = 0;
        for (Request cmd : requests) {
            timeout += cmd.getTimeout();
        }
        return timeout;
    }

    public int getAvgTimeout() {
        return requests.size() > 0 ? getTotalTimeout() / requests.size() : 0;
    }

    public Request getNextRequest() {
        if (hasNextRequest()) {
            currentCmdId++;
            return requests.get(currentCmdId);
        }

        return null;
    }

    public Request peekNextRequest() {
        if (hasNextRequest()) {
            return requests.get(currentCmdId + 1);
        }
        return null;
    }

    public Request peekPreviousRequest() {
        if (currentCmdId > 0) {
            return requests.get(currentCmdId - 1);
        }
        return null;
    }

    public Request peekCurrentRequest() {
        if (currentCmdId >= 0) {
            return requests.get(currentCmdId);
        }
        return null;
    }

    public boolean hasNextRequest() {
        return currentCmdId < requests.size() - 1;
    }

    public int getLaunchedRequestsCount() {
        return currentCmdId + 1;
    }

    public boolean isIgnoreExitCode() {
        return ignoreExitCode;
    }

    public void setIgnoreExitCode(boolean ignoreExitCode) {
        this.ignoreExitCode = ignoreExitCode;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public List<Request> getRequests() {
        return Collections.unmodifiableList(requests);
    }

    public Integer getIncrementedReqSeqNumber() {
        reqSeqNumber++;
        return reqSeqNumber;
    }

    public Integer getReqSeqNumber() {
        return reqSeqNumber;
    }

    public void setReqSeqNumber(Integer reqSeqNumber) {
        this.reqSeqNumber = reqSeqNumber;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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
