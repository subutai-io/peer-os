package io.subutai.common.host;


import java.util.Set;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;


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
    public Set<ContainerHostInfo> getContainers();

    public InstanceType getInstanceType();
}
