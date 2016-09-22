package io.subutai.core.lxc.quota.api;


import java.util.Map;
import java.util.Set;

import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.ContainerSize;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.quota.QuotaException;
import io.subutai.hub.share.resource.ContainerResourceType;
import io.subutai.hub.share.resource.PeerResources;
import io.subutai.hub.share.resource.ResourceValueParser;


/**
 * Quota Manager Api layer for reading/setting quota on a container host. {@code QuotaManager} identifies its {@code
 * ResourceHost} and executes relevant subutai quota command.
 */
public interface QuotaManager
{

    PeerResources getResourceLimits( String peerId );

    /**
     * Returns current quota values of container.
     *
     * @param containerId container id
     *
     * @return quota value
     */
    ContainerQuota getQuota( final ContainerId containerId ) throws QuotaException;


    /**
     * Sets quota values of container.
     *
     * @param containerId container id
     * @param containerQuota new quota value
     */

    void setQuota( ContainerId containerId, ContainerQuota containerQuota ) throws QuotaException;

    ResourceValueParser getResourceValueParser( ContainerResourceType containerResourceType ) throws QuotaException;


    /**
     * Returns predefined quotas of container type
     *
     * @param containerSize @see ContainerType container type
     *
     * @return @see ContainerQuota
     */
    ContainerQuota getDefaultContainerQuota( ContainerSize containerSize );


    /**
     * Returns allowed cpus/cores ids on container
     *
     * @param containerId - id of container
     *
     * @return - allowed cpu set
     */
    public Set<Integer> getCpuSet( ContainerId containerId ) throws QuotaException;

    /**
     * Sets allowed cpus/cores on container
     *
     * @param containerId - name of container
     * @param cpuSet - allowed cpu set
     */
    public void setCpuSet( ContainerId containerId, Set<Integer> cpuSet ) throws QuotaException;

    /**
     * Removes quota setting when container destroyed
     */
    void removeQuota( ContainerId containerId );

    Map<ContainerSize, ContainerQuota> getDefaultQuotas();
}
