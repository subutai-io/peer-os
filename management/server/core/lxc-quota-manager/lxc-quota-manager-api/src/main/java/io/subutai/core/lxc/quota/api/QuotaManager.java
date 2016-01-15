package io.subutai.core.lxc.quota.api;


import java.util.Set;

import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.quota.QuotaException;
import io.subutai.common.resource.ContainerResourceType;
import io.subutai.common.resource.PeerResources;
import io.subutai.common.resource.ResourceValueParser;


/**
 * Quota Manager Api layer for reading/setting quota on a container host. {@code QuotaManager} identifies its {@code
 * ResourceHost} and executes relevant subutai quota command.
 */
public interface QuotaManager
{
    /**
     * Returns available quota values of container.
     *
     * @param containerId container id
     *
     * @return quota value
     */

    ContainerQuota getAvailableQuota( ContainerId containerId ) throws QuotaException;

    PeerResources getResourceLimits( String peerId );

    /**
     * Returns current quota values of container.
     *
     * @param containerId container id
     *
     * @return quota value
     */
    ContainerQuota getQuota( final ContainerId containerId ) throws QuotaException;

    //    MeasureUnit getDefaultMeasureUnit( ContainerResourceType type );

    /**
     * Sets quota values of container.
     *
     * @param containerId container id
     * @param containerQuota new quota value
     */

    void setQuota( ContainerId containerId, ContainerQuota containerQuota ) throws QuotaException;

    ResourceValueParser getResourceValueParser( ContainerResourceType containerResourceType ) throws QuotaException;


    /**
     * Returns resource value parser by resource type.
     *
     * @param containerResourceType resource type
     *
     * @return resource value parser
     */
    //    ResourceValueParser getResourceValueParser( ContainerResourceType containerResourceType ) throws
    // QuotaException;

    //    <T extends ContainerResource> T getAvailableQuota( ContainerId containerId, Class<T> type ) throws
    // QuotaException;

    /**
     * Returns predefined quotas of container type
     *
     * @param containerSize @see ContainerType container type
     *
     * @return @see ContainerQuota
     */
    ContainerQuota getDefaultContainerQuota( ContainerSize containerSize );

    //    <T extends ContainerResource> T getQuota( ContainerId containerId, Class<T> type ) throws QuotaException;

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
     * @param containerId
     */
    void removeQuota( ContainerId containerId );
}
