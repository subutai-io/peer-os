package io.subutai.bazaar.share.resource;


import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Peer resources class
 */
public class PeerResources
{
    @JsonProperty( "peerId" )
    private String peerId;
    @JsonProperty( "environmentLimit" )
    private int environmentLimit;
    @JsonProperty( "containerLimit" )
    private int containerLimit;
    @JsonProperty( "networkLimit" )
    private int networkLimit;
    @JsonProperty( "hostResources" )
    private List<HostResources> hostResources;


    public PeerResources( @JsonProperty( "peerId" ) final String peerId,
                          @JsonProperty( "environmentLimit" ) final int environmentLimit,
                          @JsonProperty( "containerLimit" ) final int containerLimit,
                          @JsonProperty( "networkLimit" ) final int networkLimit,
                          @JsonProperty( "hostResources" ) final List<HostResources> hostResources )
    {
        this.peerId = peerId;
        this.environmentLimit = environmentLimit;
        this.containerLimit = containerLimit;
        this.networkLimit = networkLimit;
        this.hostResources = hostResources;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public int getEnvironmentLimit()
    {
        return environmentLimit;
    }


    public int getContainerLimit()
    {
        return containerLimit;
    }


    public int getNetworkLimit()
    {
        return networkLimit;
    }


    public List<HostResources> getHostResources()
    {
        return hostResources;
    }

    public HostResources findHostResources( final String hostId )
      {
          HostResources result = null;
          for ( HostResources h : this.hostResources )
          {
              if ( h.getHostId().equals( hostId ) )
              {
                  result = h;
                  break;
              }
          }
          return result;
      }


      public List<HostResources> getEnabledHostResources()
      {
          return getHostResources().stream().filter( r -> r.isEnabled() ).collect( Collectors.toList() );
      }

}
