package io.subutai.common.protocol;


import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class ReservedPorts
{
    @JsonProperty( "ports" )
    private Set<ReservedPort> reservedPorts = Sets.newHashSet();


    public ReservedPorts( final Set<ReservedPort> reservedPorts )
    {
        Preconditions.checkNotNull( reservedPorts );

        this.reservedPorts = reservedPorts;
    }


    public ReservedPorts()
    {
    }


    public void addReservedPort( ReservedPort reservedPort )
    {
        Preconditions.checkNotNull( reservedPort );

        reservedPorts.add( reservedPort );
    }


    public Set<ReservedPort> getReservedPorts()
    {
        return reservedPorts;
    }
}
