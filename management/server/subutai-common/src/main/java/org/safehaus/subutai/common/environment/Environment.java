package org.safehaus.subutai.common.environment;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Peer;


/**
 * Environment
 */
public interface Environment
{

    public Long getUserId();

    public UUID getId();

    public String getName();

    public EnvironmentStatus getStatus();

    public long getCreationTimestamp();

    public String getSshKey();

    public Set<ContainerHost> getContainerHosts();


    public void destroyContainer( ContainerHost containerHost, boolean async )
            throws EnvironmentNotFoundException, EnvironmentModificationException;


    public Set<ContainerHost> growEnvironment( Topology topology, boolean async )
            throws EnvironmentModificationException;


    public void setSshKey( String sshKey, boolean async ) throws EnvironmentModificationException;

    public Set<Peer> getPeers();

    /**
     * Network subnet of the environment in CIDR format notation.
     *
     * @return subnet string in CIDR format notation
     */
    public String getSubnetCidr();


    /**
     * VNI of the environment.
     */
    public Long getVni();


    public ContainerHost getContainerHostById( UUID id ) throws ContainerHostNotFoundException;


    public ContainerHost getContainerHostByHostname( String hostname ) throws ContainerHostNotFoundException;

    @Deprecated
    public Set<ContainerHost> getContainerHostsByIds( Set<UUID> ids ) throws ContainerHostNotFoundException;
}
