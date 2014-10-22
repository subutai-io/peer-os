package org.safehaus.subutai.core.environment.api.helper;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.util.UUIDUtil;

//import org.safehaus.subutai.core.peer.api.helpers.CloneContainersMessage;


/**
 * Created by bahadyr on 9/14/14.
 */
public class EnvironmentBuildProcess
{
    private UUID uuid;
    private ProcessStatusEnum processStatusEnum;
    private long timestamp;
    private Map<String, CloneContainersMessage> messageMap;
    private String bluerpintName;


    public EnvironmentBuildProcess( String name )
    {
        this.bluerpintName = name;
        this.uuid = UUIDUtil.generateTimeBasedUUID();
        this.processStatusEnum = ProcessStatusEnum.NEW_PROCESS;
        this.messageMap = new HashMap<>();
    }


    public String getBluerpintName()
    {
        return bluerpintName;
    }


    public long getTimestamp()
    {
        return timestamp;
    }


    public void setTimestamp( final int timestamp )
    {
        this.timestamp = timestamp;
    }


    public void setTimestamp( final long timestamp )
    {
        this.timestamp = timestamp;
    }


    public void putCloneContainerMessage( String key, CloneContainersMessage cloneContainersMessage )
    {
        this.messageMap.put( key, cloneContainersMessage );
    }


    public Map<String, CloneContainersMessage> getMessageMap()
    {
        return messageMap;
    }


    public void setMessageMap( final Map<String, CloneContainersMessage> messageMap )
    {
        this.messageMap = messageMap;
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
}
