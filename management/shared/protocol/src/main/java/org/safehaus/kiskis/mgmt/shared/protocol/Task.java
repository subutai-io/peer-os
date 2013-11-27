/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author bahadyr
 */
public class Task implements Serializable {

    private UUID uid;
    private String description;
    private TaskStatus taskStatus;
    private long reqSeqNumber;

    public Task() {
        uid = java.util.UUID.fromString(new com.eaio.uuid.UUID().toString());
//        uid = uuid.toString();
    }

    public long getIncrementedReqSeqNumber() {
        return ++reqSeqNumber;
    }

    public long getReqSeqNumber() {
        return reqSeqNumber;
    }

    public void setReqSeqNumber(long reqSeqNumber) {
        this.reqSeqNumber = reqSeqNumber;
    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
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

    @Override
    public String toString() {
        return "Task{" + "uid=" + uid + ", description=" + description + ", taskStatus=" + taskStatus + ", reqSeqNumber=" + reqSeqNumber + '}';
    }

}
