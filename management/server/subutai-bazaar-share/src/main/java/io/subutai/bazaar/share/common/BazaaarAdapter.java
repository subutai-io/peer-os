package io.subutai.bazaar.share.common;


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
}
