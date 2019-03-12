package io.subutai.common.host;


import io.subutai.common.peer.ContainerId;


public interface SnapshotEventListener
{
    void onSnapshotCreate( final ContainerId containerId );

    void onSnapshotRestore( final ContainerId containerId );

    void onSnapshotDelete( final ContainerId containerId );
}
