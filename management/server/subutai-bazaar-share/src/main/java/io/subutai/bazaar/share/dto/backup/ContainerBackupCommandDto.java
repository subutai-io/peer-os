package io.subutai.bazaar.share.dto.backup;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class ContainerBackupCommandDto
{
    private String commandId;
    private String containerId;
    private String fromSnapshotLabel; // optional, snapshot from which to start incremental backup
    private String snapshotLabel; // snapshot, on which backup is taken
    private boolean removeCreatedSnapshots;
    private boolean keepBackupFiles;

    private BackupCommandResultDto result = new BackupCommandResultDto();


    public ContainerBackupCommandDto()
    {
    }


    public ContainerBackupCommandDto( final String commandId, final String containerId, final String fromSnapshotLabel,
                                      final String snapshotLabel, final boolean removeCreatedSnapshots,
                                      final boolean keepBackupFiles )
    {
        this.commandId = commandId;
        this.containerId = containerId;
        this.fromSnapshotLabel = fromSnapshotLabel;
        this.snapshotLabel = snapshotLabel;
        this.removeCreatedSnapshots = removeCreatedSnapshots;
        this.keepBackupFiles = keepBackupFiles;
    }


    public String getCommandId()
    {
        return commandId;
    }


    public void setCommandId( final String commandId )
    {
        this.commandId = commandId;
    }


    public String getContainerId()
    {
        return containerId;
    }


    public void setContainerId( final String containerId )
    {
        this.containerId = containerId;
    }


    public String getFromSnapshotLabel()
    {
        return fromSnapshotLabel;
    }


    public void setFromSnapshotLabel( final String fromSnapshotLabel )
    {
        this.fromSnapshotLabel = fromSnapshotLabel;
    }


    public String getSnapshotLabel()
    {
        return snapshotLabel;
    }


    public void setSnapshotLabel( final String snapshotLabel )
    {
        this.snapshotLabel = snapshotLabel;
    }


    public boolean isRemoveCreatedSnapshots()
    {
        return removeCreatedSnapshots;
    }


    public void setRemoveCreatedSnapshots( final boolean removeCreatedSnapshots )
    {
        this.removeCreatedSnapshots = removeCreatedSnapshots;
    }


    public boolean isKeepBackupFiles()
    {
        return keepBackupFiles;
    }


    public void setKeepBackupFiles( final boolean keepBackupFiles )
    {
        this.keepBackupFiles = keepBackupFiles;
    }


    public boolean isIncremental()
    {
        return fromSnapshotLabel != null && !fromSnapshotLabel.trim().isEmpty();
    }


    public BackupCommandResultDto getResult()
    {
        return result;
    }


    public void setResult( BackupCommandResultDto resultDto )
    {
        this.result = resultDto;
    }


    @Override
    public String toString()
    {
        return "ContainerBackupCommandDto{commandId='" + commandId + "', containerId='" + containerId
                + "', fromSnapshotLabel='" + fromSnapshotLabel + "', snapshotLabel='" + snapshotLabel
                + "', removeCreatedSnapshots=" + removeCreatedSnapshots + ", keepBackupFiles=" + keepBackupFiles + '}';
    }


    public String toShortString()
    {
        return "BackupCmd{cont=" + containerId + ",from=" + fromSnapshotLabel + ",snapshot=" + snapshotLabel + "}";
    }
}
