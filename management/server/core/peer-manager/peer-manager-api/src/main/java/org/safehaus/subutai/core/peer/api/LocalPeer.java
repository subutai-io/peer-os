package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.container.api.ContainerCreateException;


/**
 * Local peer interface
 */
public interface LocalPeer extends Peer
{
    public ResourceHost getResourceHostByName( String hostname ) throws PeerException;

    public ContainerHost getContainerHostByName( String hostname ) throws PeerException;

    public ManagementHost getManagementHost() throws PeerException;

    public Set<ResourceHost> getResourceHosts() throws PeerException;

    /**
     * Returns the templates list
     */
    public List<String> getTemplates();

    void init();

    void shutdown();

    public void clean();

    public ContainerHost createContainer( String hostName, String templateName, String cloneName, UUID envId )
            throws ContainerCreateException;

}
