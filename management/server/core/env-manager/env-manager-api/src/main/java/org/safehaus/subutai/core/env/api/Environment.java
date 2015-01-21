package org.safehaus.subutai.core.env.api;


import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.env.api.exception.ContainerHostNotFoundException;


/**
 * Environment
 */
public interface Environment
{
    public UUID getId();

    public String getName();

    public long getCreationTimestamp();

    public Set<ContainerHost> getContainerHosts();

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
