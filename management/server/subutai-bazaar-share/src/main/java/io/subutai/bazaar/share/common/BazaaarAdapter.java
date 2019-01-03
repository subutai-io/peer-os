package io.subutai.bazaar.share.common;


import io.subutai.bazaar.share.event.EventMessage;
import io.subutai.bazaar.share.event.payload.Payload;


public interface BazaaarAdapter
{
    //
    // Environments
    //

    String getUserEnvironmentsForPeer();

    String getAllEnvironmentsForPeer();

    String getDeletedEnvironmentsForPeer();

    void destroyContainer( String envId, String containerId );

    void uploadEnvironment( String json );

    boolean uploadPeerOwnerEnvironment( String json );

    void removeEnvironment( String envId );

    void removeSshKey( String envId, String sshKey );

    void addSshKey( String envId, String sshKey );

    void notifyContainerDiskUsageExcess( String peerId, String envId, String contId, long diskUsage,
                                         boolean containerWasStopped );

    Payload getMetaData( final String environmentId, final String type );

    void pushEvent( Payload message );
}
