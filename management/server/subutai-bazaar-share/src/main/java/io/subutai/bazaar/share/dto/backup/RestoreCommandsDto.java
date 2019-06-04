package io.subutai.bazaar.share.dto.backup;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class RestoreCommandsDto
{
    private List<ContainerRestoreCommandDto> containerRestoreCommands = new ArrayList<>();


    public RestoreCommandsDto()
    {
    }


    public List<ContainerRestoreCommandDto> getContainerRestoreCommands()
    {
        return containerRestoreCommands;
    }


    public void addContainerRestoreCommand( final ContainerRestoreCommandDto containerRestoreCommand )
    {
        this.containerRestoreCommands.add( containerRestoreCommand );
    }
}
