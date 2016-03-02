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
 * Resource allocator class
 */
public class ResourceAllocator extends PeerResources
{
    private Collection<HostResources> result;

    private List<AllocatedContainer> containers = new ArrayList<>();


    public ResourceAllocator( final PeerResources peerResources )
    {
        super( peerResources.getPeerId(), peerResources.getEnvironmentLimit(), peerResources.getContainerLimit(),
                peerResources.getNetworkLimit(), peerResources.getHostResources() );
    }


    public boolean allocate( final String containerName, final String templateName, final ContainerSize size,
                             ContainerQuota containerQuota )
    {
        final Collection<HostResources> preferredHosts = getPreferredHosts();
        for ( HostResources hostResources : preferredHosts )
        {
            if ( hostResources.allocate( containerQuota ) )
            {
                AllocatedContainer container = new AllocatedContainer( containerName, templateName, size, getPeerId(),
                        hostResources.getHostId() );
                containers.add( container );
                return true;
            }
        }

        return false;
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

    //
    //    class AllocatedResource
    //    {
    //        String containerName;
    //        BigDecimal cpu;
    //        BigDecimal ram;
    //        BigDecimal disk;
    //    }


    //    public boolean reserveResource( ContainerResourceType containerResourceType, ByteValueResource resourceValue )
    //    {
    //        boolean result = false;
    //        switch ( containerResourceType )
    //        {
    //            case CPU:
    //                final double cpuLimit = resourceValue.getValue().doubleValue();
    //                if ( getUsedCpu() + cpuLimit < 100 )
    //                {
    //                    reservedCpu += cpuLimit;
    //                    result = true;
    //                }
    //                break;
    //            case RAM:
    //                final double ramLimit = resourceValue.getValue( ByteUnit.MB ).doubleValue();
    //                if ( getFreeRam() > ramLimit )
    //                {
    //                    reservedRam += ramLimit;
    //                    result = true;
    //                }
    //                break;
    //            case OPT:
    //            case VAR:
    //            case HOME:
    //            case ROOTFS:
    //                final double diskLimit = resourceValue.getValue( ByteUnit.GB ).doubleValue();
    //                if ( getAvailableSpace() > diskLimit )
    //                {
    //                    reservedDisk += diskLimit;
    //                    result = true;
    //                }
    //                break;
    //        }
    //
    //        return result;
    //    }


    //    public synchronized boolean reserveResources( ContainerQuota defaultQuotas, int amount )
    //    {
    //        Preconditions.checkArgument( amount > 0 );
    //
    //        boolean result = true;
    //        double cpuQuota = 0.0;
    //        double ramQuota = 0.0;
    //        double diskQuota = 0.0;
    //        for ( Iterator<ContainerResource> i = defaultQuotas.getAllResources().iterator(); result && i.hasNext(); )
    //        {
    //            ContainerResource quota = i.next();
    //            switch ( quota.getType() )
    //            {
    //                case CPU:
    //                    cpuQuota = amount * quota.getValue().getValue().doubleValue();
    //                    if ( getUsedCpu() + cpuQuota > 100 )
    //                    {
    //                        result = false;
    //                    }
    //                    break;
    //                case RAM:
    //                    ramQuota = amount * quota.getValue().getValue( MeasureUnit.MB ).doubleValue();
    //                    if ( getAvailableRam() <= ramQuota )
    //                    {
    //                        result = false;
    //                    }
    //                    break;
    //                case ROOTFS:
    //                case HOME:
    //                case OPT:
    //                case VAR:
    //                    diskQuota += amount * quota.getValue().getValue( MeasureUnit.GB ).doubleValue();
    //                    if ( getAvailableSpace() <= diskQuota )
    //                    {
    //                        result = false;
    //                    }
    //                    break;
    //            }
    //        }
    //
    //        if ( result )
    //        {
    //            reservedCpu += cpuQuota;
    //            reservedRam += ramQuota;
    //            reservedDisk += diskQuota;
    //        }
    //        return result;
    //    }


    //    public boolean releaseResources( ContainerQuota defaultQuotas, int amount )
    //    {
    //        Preconditions.checkArgument( amount > 0 );
    //
    //        boolean result = true;
    //        double cpuQuota = 0.0;
    //        double ramQuota = 0.0;
    //        double diskQuota = 0.0;
    //        for ( Iterator<ContainerResource> i = defaultQuotas.getAllResources().iterator(); result && i.hasNext(); )
    //        {
    //            ContainerResource quota = i.next();
    //            switch ( quota.getType() )
    //            {
    //                case CPU:
    //                    cpuQuota = amount * quota.getValue().getValue().doubleValue();
    //                    if ( reservedCpu < cpuQuota )
    //                    {
    //                        result = false;
    //                    }
    //                    break;
    //                case RAM:
    //                    ramQuota = amount * quota.getValue().getValue( MeasureUnit.MB ).doubleValue();
    //                    if ( reservedRam < ramQuota )
    //                    {
    //                        result = false;
    //                    }
    //                    break;
    //                case ROOTFS:
    //                case HOME:
    //                case OPT:
    //                case VAR:
    //                    diskQuota += amount * quota.getValue().getValue( MeasureUnit.GB ).doubleValue();
    //                    if ( reservedDisk < diskQuota )
    //                    {
    //                        result = false;
    //                    }
    //                    break;
    //            }
    //        }
    //
    //        if ( result )
    //        {
    //            reservedCpu -= cpuQuota;
    //            reservedRam -= ramQuota;
    //            reservedDisk -= diskQuota;
    //        }
    //        return result;
    //    }


    //    /**
    //     * This method returns the amount of containers can be placed in the resource host
    //     *
    //     * @return amount of free slots
    //     *//*
    //    public Integer getAvailableSlots( ContainerQuota defaultQuotas )
    //    {
    //        Double diskQuota = null;
    //        Integer result = null;
    //        for ( ContainerResource containerResource : defaultQuotas.getAllResources() )
    //        {
    //            switch ( containerResource.getContainerResourceType() )
    //            {
    //                case CPU:
    //                    containerResource.getResource( ContainerCpuResource.class );
    //                    double cpuQuota = 0.0;
    //                    int cpuSlots = ( int ) ( getAvailableCpu() / cpuQuota );
    //                    if ( result == null )
    //                    {
    //                        result = cpuSlots;
    //                    }
    //                    else
    //                    {
    //                        result = Math.min( result, cpuSlots );
    //                    }
    //                    break;
    //                case RAM:
    //                    double ramQuota = containerResource.getValue().getValue( MeasureUnit.MB ).doubleValue();
    //                    int ramSlots = ( int ) ( getAvailableRam() / ramQuota );
    //                    if ( result == null )
    //                    {
    //                        result = ramSlots;
    //                    }
    //                    else
    //                    {
    //                        result = Math.min( result, ramSlots );
    //                    }
    //                    break;
    //                case ROOTFS:
    //                case HOME:
    //                case OPT:
    //                case VAR:
    //                    if ( diskQuota == null )
    //                    {
    //                        diskQuota = containerResource.getResource().getValue().getValue()
    //                    }
    //                    else
    //                    {
    //                        diskQuota += containerResource.getValue().getValue( MeasureUnit.GB ).doubleValue();
    //                    } break;
    //            }
    //        }
    //
    //        if ( diskQuota == null )
    //        {
    //            return result;
    //        }
    //        int diskSlots = ( int ) ( getAvailableSpace() / diskQuota );
    //        if ( result == null )
    //        {
    //            return diskSlots;
    //        }
    //        return Math.min( result, diskSlots );
    //    }*/
}
