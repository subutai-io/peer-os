package io.subutai.common.host;


import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;


/**
 * Container host info.
 */
@JsonSerialize( as = ContainerHostInfoModel.class )
@JsonDeserialize( as = ContainerHostInfoModel.class )
public interface ContainerHostInfo extends HostInfo
{
    /**
     * Returns status/state of container
     */
    ContainerHostState getState();

    String getContainerName();
}
