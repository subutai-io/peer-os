package io.subutai.bazaar.share.dto.snapshots;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class SnapshotOperationsDto
{


    public SnapshotOperationsDto()
    {
    }


    private List<SnapshotOperationDto> operations = new ArrayList<>();


    public List<SnapshotOperationDto> getOperations()
    {
        return operations;
    }


    public void addOperation( final SnapshotOperationDto operation )
    {
        this.operations.add( operation );
    }
}
