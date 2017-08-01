package io.subutai.common.host;


import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * Resource host info. Can contain info about resource host or management host
 */
@JsonSerialize( as = ResourceHostInfoModel.class )
@JsonDeserialize( as = ResourceHostInfoModel.class )
public interface ResourceHostInfo extends HostInfo
{
    /**
     * returns hosted containers
     */
    Set<ContainerHostInfo> getContainers();

    InstanceType getInstanceType();

    String getAddress();
}
