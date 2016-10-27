package io.subutai.common.protocol;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.settings.Common;
import io.subutai.common.util.NumUtil;


/**
 * Represents tunnel between peers
 */
public class Tunnel
{
    @JsonProperty( "tunnelName" )
    private final String tunnelName;
    @JsonProperty( "tunnelIp" )
    private final String tunnelIp;
    @JsonProperty( "vlan" )
    private final int vlan;
    @JsonProperty( "vni" )
    private final long vni;


    public Tunnel( @JsonProperty( "tunnelName" ) final String tunnelName,
                   @JsonProperty( "tunnelIp" ) final String tunnelIp, @JsonProperty( "vlan" ) final int vlan,
                   @JsonProperty( "vni" ) final long vni )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( tunnelName ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( tunnelIp ) );
        Preconditions.checkArgument( tunnelIp.matches( Common.IP_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );
        Preconditions.checkArgument( NumUtil.isLongBetween( vni, Common.MIN_VNI_ID, Common.MAX_VNI_ID ) );

        this.tunnelName = tunnelName;
        this.tunnelIp = tunnelIp;
        this.vlan = vlan;
        this.vni = vni;
    }


    public String getTunnelName()
    {
        return tunnelName;
    }


    public String getTunnelIp()
    {
        return tunnelIp;
    }


    public int getVlan()
    {
        return vlan;
    }


    public long getVni()
    {
        return vni;
    }
}