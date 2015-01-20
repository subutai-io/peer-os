package org.safehaus.subutai.core.environment.api.helper;


import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.ContainerHost;


public interface Environment
{
    public long getCreationTimestamp();


    public EnvironmentStatusEnum getStatus();


    public void setStatus( final EnvironmentStatusEnum status );


    public void addContainer( ContainerHost container );


    public Set<ContainerHost> getContainerHosts();


    public String getName();


    public UUID getId();


    public String getPublicKey();


    public void setPublicKey( String key );


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
     *
     * @return
     */
    public int getVni();


    public ContainerHost getContainerHostById( UUID uuid );


    public ContainerHost getContainerHostByHostname( String hostname );


    public Set<ContainerHost> getContainerHostsByIds( Set<UUID> ids );


    public void addContainers( final Set<ContainerHost> containerHosts );


    public void removeContainer( final ContainerHost containerHost );
}

