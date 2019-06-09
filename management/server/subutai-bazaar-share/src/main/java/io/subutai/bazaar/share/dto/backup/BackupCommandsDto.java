package io.subutai.bazaar.share.dto.backup;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class BackupCommandsDto
{
    private String cdnToken;
    private List<ContainerBackupCommandDto> backupCommands = new ArrayList<>();


    public BackupCommandsDto()
    {
    }


    public String getCdnToken()
    {
        return cdnToken;
    }


    public void setCdnToken( final String cdnToken )
    {
        this.cdnToken = cdnToken;
    }


    public List<ContainerBackupCommandDto> getBackupCommands()
    {
        return backupCommands;
    }


    public void addBackup( final ContainerBackupCommandDto backup )
    {
        this.backupCommands.add( backup );
    }
}
