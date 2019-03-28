package io.subutai.bazaar.share.dto.snapshots;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties( ignoreUnknown = true )
public class PeerSnapshotsDto
{
    private List<SnapshotDto> snapshots = new ArrayList<>();

    /*
    List of containers, whose snapshots state is unknown due to error or Resource Host being offline.
     */
    private Set<String> offlineContainers = new HashSet<>();


    public PeerSnapshotsDto()
    {
    }


    public List<SnapshotDto> getSnapshots()
    {
        return snapshots;
    }


    public void addSnapshot( final SnapshotDto snapshot )
    {
        this.snapshots.add( snapshot );
    }


    public Set<String> getOfflineContainers()
    {
        return offlineContainers;
    }


    public void addOfflineContainer( final String offlineContainer )
    {
        this.offlineContainers.add( offlineContainer );
    }
}
