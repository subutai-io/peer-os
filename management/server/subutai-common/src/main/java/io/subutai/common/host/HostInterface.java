package io.subutai.common.host;


import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;


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

    /**
     * returns MAC address
     */
    public String getMac();
}
