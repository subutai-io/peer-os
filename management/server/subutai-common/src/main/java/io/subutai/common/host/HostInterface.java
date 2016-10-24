package io.subutai.common.host;


import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * Host network interface
 */
@JsonSerialize( as = HostInterfaceModel.class )
@JsonDeserialize( as = HostInterfaceModel.class )
public interface HostInterface extends Serializable
{
    /**
     * returns network interface name
     */
    String getName();

    /**
     * returns ip address
     */
    String getIp();
}
