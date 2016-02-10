package io.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.subutai.common.peer.ContainerSize;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.resource.HostResources;
import io.subutai.common.resource.PeerResources;


/**
 * Random container allocator class
 */
public class RandomAllocator extends PeerResources
{
    private Collection<HostResources> result;

    private List<AllocatedContainer> containers = new ArrayList<>();


    public RandomAllocator( final PeerResources peerResources )
    {
        super( peerResources.getPeerId(), peerResources.getEnvironmentLimit(), peerResources.getContainerLimit(),
                peerResources.getNetworkLimit(), peerResources.getHostResources() );
    }


    public boolean allocate( final String containerName, final String templateName, final ContainerSize size,
                             ContainerQuota containerQuota )
    {
        final Collection<HostResources> preferredHosts = getPreferredHosts();

        if ( preferredHosts.size() < 1 )
        {
            return false;
        }

        AllocatedContainer container = new AllocatedContainer( containerName, templateName, size, getPeerId(),
                getPreferredHosts().iterator().next().getHostId() );
        containers.add( container );

        return true;
    }


    public Collection<HostResources> getPreferredHosts()
    {
        List<HostResources> result = new ArrayList<>( getHostResources() );
        Collections.shuffle( result );
        return result;
    }


    public class AllocatedContainer
    {
        private final String name;
        private String templateName;
        private ContainerSize size;
        private String hostId;
        private String peerId;


        public AllocatedContainer( final String name, final String templateName, final ContainerSize size,
                                   final String peerId, final String hostId )
        {
            this.name = name;
            this.templateName = templateName;
            this.size = size;
            this.hostId = hostId;
            this.peerId = peerId;
        }


        public String getName()
        {
            return name;
        }


        public String getTemplateName()
        {
            return templateName;
        }


        public ContainerSize getSize()
        {
            return size;
        }


        public String getHostId()
        {
            return hostId;
        }


        public String getPeerId()
        {
            return peerId;
        }
    }


    public List<AllocatedContainer> getContainers()
    {
        return containers;
    }
}
