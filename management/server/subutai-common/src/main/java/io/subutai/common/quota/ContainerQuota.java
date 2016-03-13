package io.subutai.common.quota;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;

import io.subutai.common.resource.ContainerResourceType;


/**
 * Container resource class
 */
public class ContainerQuota
{
    @JsonProperty
    private Map<ContainerResourceType, ContainerResource> resources = new HashMap<>();


    public ContainerQuota( ContainerResource resource )
    {
        addResource( resource );
    }


    public ContainerQuota( ContainerResource... resources )
    {
        for ( int i = 0; i < resources.length; i++ )
        {
            addResource( resources[i] );
        }
    }


    public void addResource( ContainerResource containerResource )
    {
        Preconditions.checkNotNull( containerResource );
        Preconditions.checkNotNull( containerResource.getContainerResourceType() );
        Preconditions.checkNotNull( containerResource.getResource() );

        resources.put( containerResource.getContainerResourceType(), containerResource );
    }


    public ContainerCpuResource getCpu()
    {
        return ( ContainerCpuResource ) resources.get( ContainerResourceType.CPU );
    }


    public ContainerRamResource getRam()
    {
        return ( ContainerRamResource ) resources.get( ContainerResourceType.RAM );
    }


    public ContainerHomeResource getHome()
    {
        return ( ContainerHomeResource ) resources.get( ContainerResourceType.HOME );
    }


    public ContainerOptResource getOpt()
    {
        return ( ContainerOptResource ) resources.get( ContainerResourceType.OPT );
    }


    public ContainerVarResource getVar()
    {
        return ( ContainerVarResource ) resources.get( ContainerResourceType.VAR );
    }


    public ContainerRootfsResource getRootfs()
    {
        return ( ContainerRootfsResource ) resources.get( ContainerResourceType.ROOTFS );
    }


    public Collection<ContainerResource> getAllResources()
    {
        return resources.values();
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "ContainerQuota{" );
        sb.append( "resources=" ).append( resources );
        sb.append( '}' );
        return sb.toString();
    }
}
