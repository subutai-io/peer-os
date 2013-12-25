/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author bahadyr
 */
public class Task implements Serializable {

    private UUID uuid;
    private String description;
    private TaskStatus taskStatus;
    private Integer reqSeqNumber;
    private final List<Command> commands;

    public void addCommand(Command command) {
        commands.add(command);
    }

    public Task() {
        uuid = java.util.UUID.fromString(new com.eaio.uuid.UUID().toString());
        reqSeqNumber = 0;
        commands = new ArrayList<Command>();
    }

    public List<Command> getCommands() {
        return commands;
    }

    public Integer getIncrementedReqSeqNumber() {
        return ++reqSeqNumber;
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

    @Override
    public String toString() {
        return "Task{" + "uuid=" + uuid + ", description=" + description + ", taskStatus=" + taskStatus + ", reqSeqNumber=" + reqSeqNumber + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Task task = (Task) o;

        return !(uuid != null ? !uuid.equals(task.uuid) : task.uuid != null);
    }

    @Override
    public int hashCode() {
        return uuid != null ? 1 : 0;
    }
}
