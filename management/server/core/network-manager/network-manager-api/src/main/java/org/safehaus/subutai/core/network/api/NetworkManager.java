package org.safehaus.subutai.core.network.api;


public interface NetworkManager
{
    public void setupN2NConnection( String superNodeIp, int superNodePort, String tapInterfaceName,
                                    String communityName, String localIp ) throws NetworkManagerException;

    public void removeN2NConnection( String tapInterfaceName, String communityName ) throws NetworkManagerException;

    public void setupTunnel( String tunnelName, String peerIp, String connectionType ) throws NetworkManagerException;

    public void removeTunnel( String tunnelName ) throws NetworkManagerException;

    public void setContainerIp( String containerName, String ip, int netMask, int vLanId )
            throws NetworkManagerException;

    public void removeContainerIp( String containerName ) throws NetworkManagerException;
}

