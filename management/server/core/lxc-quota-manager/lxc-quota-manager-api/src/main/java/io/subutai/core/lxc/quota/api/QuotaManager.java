package io.subutai.core.lxc.quota.api;


import java.util.Map;

import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.quota.ContainerSize;
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

    @Deprecated
    PeerResources getResourceLimits( String peerId ) throws QuotaException;


    ResourceValueParser getResourceValueParser( ContainerResourceType containerResourceType ) throws QuotaException;

    /**
     * Returns predefined quotas of container type
     *
     * @param containerSize @see ContainerType container type
     *
     * @return @see ContainerQuota
     */
    ContainerQuota getDefaultContainerQuota( ContainerSize containerSize );

    Map<ContainerSize, ContainerQuota> getDefaultQuotas();
}
