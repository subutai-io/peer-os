package org.safehaus.subutai.core.environment.api.helper;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.network.api.N2NConnection;
import org.safehaus.subutai.core.network.api.Tunnel;


public class EnvironmentBuildProcess
{
    private UUID id;
    private ProcessStatusEnum processStatusEnum;
    private long timestamp;
    private Map<String, CloneContainersMessage> messageMap;
    private UUID blueprintId;

    private N2NConnection n2nConnection;
    private String keyFilePath;
    private Tunnel tunnel;


    public EnvironmentBuildProcess( UUID blueprintId )
    {
        this.blueprintId = blueprintId;
        this.id = UUIDUtil.generateTimeBasedUUID();
        this.processStatusEnum = ProcessStatusEnum.NEW_PROCESS;
        this.messageMap = new HashMap<>();
    }


    public UUID getBlueprintId()
    {
        return blueprintId;
    }


    public long getTimestamp()
    {
        return timestamp;
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


    public UUID getId()
    {
        return id;
    }


    public void setId( final UUID id )
    {
        this.id = id;
    }


    public N2NConnection getN2nConnection()
    {
        return n2nConnection;
    }


    public void setN2nConnection( N2NConnection n2nConnection )
    {
        this.n2nConnection = n2nConnection;
    }


    public String getKeyFilePath()
    {
        return keyFilePath;
    }


    public void setKeyFilePath( String keyFilePath )
    {
        this.keyFilePath = keyFilePath;
    }


    public Tunnel getTunnel()
    {
        return tunnel;
    }


    public void setTunnel( Tunnel tunnel )
    {
        this.tunnel = tunnel;
    }


}

