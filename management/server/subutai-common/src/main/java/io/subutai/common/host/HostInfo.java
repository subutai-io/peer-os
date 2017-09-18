package io.subutai.common.host;


import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * Parent interface for host info
 */
@JsonSerialize( as = HostInfoModel.class )
@JsonDeserialize( as = HostInfoModel.class )
public interface HostInfo extends Comparable<HostInfo>, Serializable
{
    /**
     * Returns id of host
     */
    String getId();


    /**
     * Returns hostname of host
     */
    String getHostname();


    /**
     * Returns architecture of host
     */
    HostArchitecture getArch();
}
