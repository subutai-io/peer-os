package io.subutai.common.host;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


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

    Quota getRawQuota();

    /**
     * Returns network interfaces of host
     */
    HostInterfaces getHostInterfaces();

    String getEnvId();

    Integer getVlan();
}
