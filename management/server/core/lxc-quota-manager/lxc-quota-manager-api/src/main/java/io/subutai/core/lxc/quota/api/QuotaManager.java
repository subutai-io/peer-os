package io.subutai.core.lxc.quota.api;


import java.util.Set;

import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.ContainerType;
import io.subutai.common.quota.ContainerQuotaHolder;
import io.subutai.common.quota.QuotaException;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;
import io.subutai.common.resource.ResourceValueParser;


/**
 * Quota Manager Api layer for reading/setting quota on a container host. {@code QuotaManager} identifies its {@code
 * ResourceHost} and executes relevant subutai quota command.
 */
public interface QuotaManager
{
    /**
     * Returns available quota value of container by resource type.
     *
     * @param containerId container id
     * @param resourceType resource type
     *
     * @return quota value
     */

    ResourceValue getAvailableQuota( ContainerId containerId, ResourceType resourceType ) throws QuotaException;

    /**
     * Returns current quota value of container by resource type.
     *
     * @param containerId container id
     * @param resourceType resource type
     *
     * @return quota value
     */
    ResourceValue getQuota( final ContainerId containerId, final ResourceType resourceType ) throws QuotaException;

    /**
     * Sets quota value of container by resource type.
     *
     * @param containerId container id
     * @param resourceType resource type
     * @param quotaValue new quota value
     */

    void setQuota( final ContainerId containerId, ResourceType resourceType, ResourceValue quotaValue )
            throws QuotaException;

    /**
     * Sets quota values.
     *
     * @param containerId container id
     * @param containerQuota set of quota values
     */
    void setQuota( ContainerId containerId, ContainerQuotaHolder containerQuota ) throws QuotaException;


    /**
     * Returns resource value parser by resource type.
     *
     * @param resourceType resource type
     *
     * @return resource value parser
     */
    ResourceValueParser getResourceValueParser( ResourceType resourceType ) throws QuotaException;

    /**
     * Returns predefined quotas of container type
     *
     * @param containerType @see ContainerType container type
     *
     * @return @see ContainerQuota
     */
    ContainerQuotaHolder getDefaultContainerQuota( ContainerType containerType );

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
}
