package io.subutai.bazaar.share.dto.snapshots;


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
    private SnapshotDto snapshot;
    private boolean stopContainer;
    private boolean successful;
    private String errors;


    public SnapshotOperationDto()
    {
    }


    public SnapshotOperationDto( final String containerId, final Command command, final SnapshotDto snapshot,
                                 final boolean stopContainer )
    {
        this.containerId = containerId;
        this.command = command;
        this.snapshot = snapshot;
        this.stopContainer = stopContainer;
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


    public SnapshotDto getSnapshot()
    {
        return snapshot;
    }


    public void setSnapshot( final SnapshotDto snapshot )
    {
        this.snapshot = snapshot;
    }


    public boolean isStopContainer()
    {
        return stopContainer;
    }


    public void setStopContainer( final boolean stopContainer )
    {
        this.stopContainer = stopContainer;
    }


    public boolean isSuccessful()
    {
        return successful;
    }


    public void setSuccessful( final boolean successful )
    {
        this.successful = successful;
    }


    public String getErrors()
    {
        return errors;
    }


    public void setErrors( final String errors )
    {
        this.errors = errors;
    }
}
