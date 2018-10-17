package io.subutai.bazaar.share.dto.domain;


import java.util.Set;


public class DomainDto
{
    public enum DomainState
    {
        RESERVE_NETWORK, SETUP_P2P, PORT_MAP, ENABLE_DOMAIN, WAIT, READY, ERROR
    }


    private DomainState state;

    private Set<String> reservedNetworkResources;


    public DomainState getState()
    {
        return state;
    }


    public void setState( DomainState state )
    {
        this.state = state;
    }


    public Set<String> getReservedNetworkResources()
    {
        return reservedNetworkResources;
    }


    public void setReservedNetworkResources( Set<String> reservedNetworkResources )
    {
        this.reservedNetworkResources = reservedNetworkResources;
    }
}