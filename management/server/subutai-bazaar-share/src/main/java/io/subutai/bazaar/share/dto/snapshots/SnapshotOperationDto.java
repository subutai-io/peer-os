package io.subutai.bazaar.share.dto.snapshots;


import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class SnapshotOperationDto
{
    public enum Command
    {
        CREATE, DELETE, ROLLBACK
    }


    private String containerId;
    private Command command;
    private UUID commandId;
    private String label;
    private String partition;
    private boolean stopContainer;
    private boolean forceRollback;

    private SnapshotOperationResultDto result = new SnapshotOperationResultDto();


    public SnapshotOperationDto()
    {
    }


    public SnapshotOperationDto( final String containerId, final Command command, final String label,
                                 final String partition )
    {
        this.containerId = containerId;
        this.command = command;
        this.label = label;
        this.partition = partition;
    }


    public String getContainerId()
    {
        return containerId;
    }


    public void setContainerId( final String containerId )
    {
        this.containerId = containerId;
    }


    public Command getCommand()
    {
        return command;
    }


    public void setCommand( final Command command )
    {
        this.command = command;
    }


    public UUID getCommandId()
    {
        return commandId;
    }


    public void setCommandId( final UUID commandId )
    {
        this.commandId = commandId;
    }


    public String getLabel()
    {
        return label;
    }


    public void setLabel( final String label )
    {
        this.label = label;
    }


    public String getPartition()
    {
        return partition;
    }


    public void setPartition( final String partition )
    {
        this.partition = partition;
    }


    public boolean isStopContainer()
    {
        return stopContainer;
    }


    public void setStopContainer( final boolean stopContainer )
    {
        this.stopContainer = stopContainer;
    }


    public boolean isForceRollback()
    {
        return forceRollback;
    }


    public void setForceRollback( final boolean forceRollback )
    {
        this.forceRollback = forceRollback;
    }


    public SnapshotOperationResultDto getResult()
    {
        return result;
    }


    public void setResult( SnapshotOperationResultDto resultDto )
    {
        this.result = resultDto;
    }
}
