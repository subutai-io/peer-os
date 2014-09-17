package org.safehaus.subutai.core.environment.api.helper;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by bahadyr on 9/14/14.
 */
public class EnvironmentBuildProcess {

    String environmentName;
    private UUID uuid;
    private boolean completeStatus;
    private ProcessStatusEnum processStatusEnum;
    private int timestamp;
    private List<ContainerBuildMessage> containerBuildMessages;


    public EnvironmentBuildProcess() {
        this.uuid = UUID.randomUUID();
        this.containerBuildMessages = new ArrayList<ContainerBuildMessage>();
        this.processStatusEnum = ProcessStatusEnum.NEW_PROCESS;
    }


    public ProcessStatusEnum getProcessStatusEnum() {
        return processStatusEnum;
    }


    public void setProcessStatusEnum( final ProcessStatusEnum processStatusEnum ) {
        this.processStatusEnum = processStatusEnum;
    }


    public UUID getUuid() {
        return uuid;
    }


    public void setUuid( final UUID uuid ) {
        this.uuid = uuid;
    }


    public int getTimestamp() {
        return timestamp;
    }


    public void setTimestamp( final int timestamp ) {
        this.timestamp = timestamp;
    }


    public boolean isCompleteStatus() {
        return completeStatus;
    }


    public void setCompleteStatus( final boolean completeStatus ) {
        this.completeStatus = completeStatus;
    }


    public List<ContainerBuildMessage> getContainerBuildMessages() {
        return containerBuildMessages;
    }


    public void setContainerBuildMessages( final List<ContainerBuildMessage> containerBuildMessages ) {
        this.containerBuildMessages = containerBuildMessages;
    }


    public void addBuildBlock( final ContainerBuildMessage containerBuildMessage ) {
        this.containerBuildMessages.add( containerBuildMessage );
    }


    public String getEnvironmentName() {
        return environmentName;
    }


    public void setEnvironmentName( final String environmentName ) {
        this.environmentName = environmentName;
    }
}
