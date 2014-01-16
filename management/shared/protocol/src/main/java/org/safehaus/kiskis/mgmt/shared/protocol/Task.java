/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//import java.util.ListIterator;
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
    private boolean ignoreExitCode = false;
    private boolean completed = false;
    private int currentCmdId = -1;
    private int completedCommandsCount = 0;
    private int succeededCommandsCount = 0;
    private Object data;

    public Task() {
        taskStatus = TaskStatus.NEW;
        uuid = java.util.UUID.fromString(new com.eaio.uuid.UUID().toString());
        reqSeqNumber = 0;
        commands = new ArrayList<Command>();
    }

    public Task(String description) {
        this();
        this.description = description;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void incrementCompletedCommandsCount() {
        completedCommandsCount++;
    }

    public void incrementSucceededCommandsCount() {
        succeededCommandsCount++;
    }

    public int getCompletedCommandsCount() {
        return completedCommandsCount;
    }

    public int getSucceededCommandsCount() {
        return succeededCommandsCount;
    }

    public void addCommand(Command command) {
        if (command != null) {
            command.getRequest().setTaskUuid(uuid);
            command.getRequest().setRequestSequenceNumber(getIncrementedReqSeqNumber());
            commands.add(command);
        }
    }

    public int getTotalTimeout() {
        int timeout = 0;
        for (Command cmd : commands) {
            timeout += cmd.getRequest().getTimeout();
        }
        return timeout;
    }

    public int getAvgTimeout() {
        return commands.size() > 0 ? getTotalTimeout() / commands.size() : 0;
    }

    public Command getNextCommand() {
        if (hasNextCommand()) {
            return commands.get(++currentCmdId);
        }

        return null;
    }

    public Command peekNextCommand() {
        if (hasNextCommand()) {
            return commands.get(currentCmdId + 1);
        }
        return null;
    }

    public Command peekPreviousCommand() {
        if (currentCmdId > 0) {
            return commands.get(currentCmdId - 1);
        }
        return null;
    }

    public Command peekCurrentCommand() {
        if (currentCmdId >= 0) {
            return commands.get(currentCmdId);
        }
        return null;
    }

    public boolean hasNextCommand() {
        return currentCmdId < commands.size() - 1;
    }

    public int getLaunchedCommandsCount() {
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

    public List<Command> getCommands() {
        return Collections.unmodifiableList(commands);
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
        if (this.uuid != other.uuid && (this.uuid == null || !this.uuid.equals(other.uuid))) {
            return false;
        }
        return true;
    }

}
