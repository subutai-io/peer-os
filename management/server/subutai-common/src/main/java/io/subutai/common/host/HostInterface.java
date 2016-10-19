package io.subutai.common.host;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * Host network interface
 */
@JsonSerialize( as = HostInterfaceModel.class )
@JsonDeserialize( as = HostInterfaceModel.class )
public interface HostInterface
{
    /**
     * returns network interface name
     */
    public String getName();

    /**
     * returns ip address
     */
    public String getIp();
}
