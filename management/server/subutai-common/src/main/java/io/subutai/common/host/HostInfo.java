package io.subutai.common.host;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * Parent interface for host info
 */
@JsonSerialize( as = HostInfoModel.class )
@JsonDeserialize( as = HostInfoModel.class )
public interface HostInfo extends Comparable<HostInfo>
{
    /**
     * Returns id of host
     */
    public String getId();


    /**
     * Returns hostname of host
     */
    public String getHostname();


    /**
     * Returns network interfaces of host
     */
    public HostInterfaces getHostInterfaces();


    /**
     * Returns architecture of host
     */
    public HostArchitecture getArch();
}
