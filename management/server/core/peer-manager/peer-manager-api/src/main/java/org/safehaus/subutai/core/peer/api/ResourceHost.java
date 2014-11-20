package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;
import org.safehaus.subutai.core.strategy.api.ServerMetric;


/**
 * Resource host interface.
 */
public interface ResourceHost extends Host
{
    public void createContainer( LocalPeer localPeer, String creatorPeerId, String envId, List<Template> templates,
                                 String cloneName, String nodeGroup );

    public Set<ContainerHost> getContainerHostsByNameList( Set<String> cloneNames );

    public ServerMetric getMetric() throws ResourceHostException;

    public Set<ContainerHost> getContainerHosts();

    public void addContainerHost( ContainerHost containerHost );

    public ContainerHost getContainerHostByName( String hostname );

    public Set<ContainerHost> getContainerHostsByEnvironmentId( UUID environmentId );

    public ContainerHost getContainerHostById( String id );

    public boolean startContainerHost( ContainerHost containerHost ) throws ResourceHostException;

    public boolean stopContainerHost( ContainerHost containerHost ) throws ResourceHostException;

    public void destroyContainerHost( ContainerHost containerHost ) throws ResourceHostException;

    public void removeContainerHost( Host result ) throws ResourceHostException;

    void onHeartbeat( ResourceHostInfo resourceHostInfo );
}
