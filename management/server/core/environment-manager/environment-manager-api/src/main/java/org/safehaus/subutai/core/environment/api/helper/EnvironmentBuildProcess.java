package org.safehaus.subutai.core.environment.api.helper;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;

//import org.safehaus.subutai.core.peer.api.helpers.CloneContainersMessage;


/**
 * Created by bahadyr on 9/14/14.
 */
public class EnvironmentBuildProcess
{

    private String environmentName;
    private UUID uuid;
    private boolean completeStatus;
    private ProcessStatusEnum processStatusEnum;
    private int timestamp;
    private List<CloneContainersMessage> cloneContainersMessages;
    private Map<String, CloneContainersMessage> messageMap;


    public EnvironmentBuildProcess( final String name )
    {
        this.environmentName = name;
        this.uuid = UUID.randomUUID();
        this.processStatusEnum = ProcessStatusEnum.NEW_PROCESS;
        this.cloneContainersMessages = new ArrayList<>();
        this.messageMap = new HashMap<>();
    }


    public void putCloneContainerMessage( String peerId, CloneContainersMessage cloneContainersMessage )
    {
        this.messageMap.put( peerId, cloneContainersMessage );
    }


    public void setCloneContainersMessages( final List<CloneContainersMessage> cloneContainersMessages )
    {
        this.cloneContainersMessages = cloneContainersMessages;
    }


    public Map<String, CloneContainersMessage> getMessageMap()
    {
        return messageMap;
    }


    public void setMessageMap( final Map<String, CloneContainersMessage> messageMap )
    {
        this.messageMap = messageMap;
    }


    public List<CloneContainersMessage> getCloneContainersMessages()
    {
        return cloneContainersMessages;
    }


    public void addCloneContainerMessage( CloneContainersMessage ccm )
    {
        this.cloneContainersMessages.add( ccm );
    }


    public ProcessStatusEnum getProcessStatusEnum()
    {
        return processStatusEnum;
    }


    public void setProcessStatusEnum( final ProcessStatusEnum processStatusEnum )
    {
        this.processStatusEnum = processStatusEnum;
    }


    public UUID getUuid()
    {
        return uuid;
    }


    public void setUuid( final UUID uuid )
    {
        this.uuid = uuid;
    }


    public int getTimestamp()
    {
        return timestamp;
    }


    public void setTimestamp( final int timestamp )
    {
        this.timestamp = timestamp;
    }


    public boolean isCompleteStatus()
    {
        return completeStatus;
    }


    public void setCompleteStatus( final boolean completeStatus )
    {
        this.completeStatus = completeStatus;
    }


    public String getEnvironmentName()
    {
        return environmentName;
    }


    public void setEnvironmentName( final String environmentName )
    {
        this.environmentName = environmentName;
    }
}
