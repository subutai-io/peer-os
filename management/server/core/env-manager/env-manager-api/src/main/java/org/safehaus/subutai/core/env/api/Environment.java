package org.safehaus.subutai.core.env.api;


import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.env.api.build.Topology;
import org.safehaus.subutai.core.env.api.exception.ContainerHostNotFoundException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentModificationException;
import org.safehaus.subutai.core.env.api.exception.EnvironmentNotFoundException;


/**
 * Environment
 */
public interface Environment
{
    public UUID getId();

    public String getName();

    public EnvironmentStatus getStatus();

    public long getCreationTimestamp();

    public String getPublicKey();

    public Set<ContainerHost> getContainerHosts();

    public void destroyContainer( ContainerHost containerHost )
            throws EnvironmentNotFoundException, EnvironmentModificationException;

    public void destroyContainerAsync( ContainerHost containerHost ) throws EnvironmentNotFoundException;

    public void growEnvironment( Topology topology ) throws EnvironmentModificationException;

    public void growEnvironmentAsync( Topology topology );


    /**
     * Network subnet of the environment in CIDR format notation.
     *
     * @return subnet string in CIDR format notation
     */
    public String getSubnetCidr();


    /**
     * VLAN ids of each participating peer.
     *
     * @return map of peer ids to VLAN ids
     */
    public Map<UUID, Integer> getPeerVlanInfo();


    /**
     * VNI of the environment.
     */
    public int getVni();


    public ContainerHost getContainerHostById( UUID id ) throws ContainerHostNotFoundException;


    public ContainerHost getContainerHostByHostname( String hostname ) throws ContainerHostNotFoundException;


    public Set<ContainerHost> getContainerHostsByIds( Set<UUID> ids ) throws ContainerHostNotFoundException;
}
