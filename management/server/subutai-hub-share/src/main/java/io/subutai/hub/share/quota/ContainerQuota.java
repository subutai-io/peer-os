package io.subutai.hub.share.quota;


import java.util.Collection;
import java.util.EnumMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import io.subutai.hub.share.resource.ContainerResourceType;


/**
 * Container quota class
 */
public class ContainerQuota
{
    @JsonProperty
    private ContainerSize containerSize;

    @JsonProperty
    private EnumMap<ContainerResourceType, Quota> resources = new EnumMap<>( ContainerResourceType.class );


    public ContainerQuota( ContainerSize containerSize )
    {
        this.containerSize = containerSize;
    }


    public ContainerQuota( ContainerSize containerSize, Quota quota )
    {
        this.containerSize = containerSize;
        add( quota );
    }


    public ContainerQuota( ContainerSize containerSize, Quota... quotas )
    {
        this.containerSize = containerSize;
        for ( final Quota quota : quotas )
        {
            add( quota );
        }
    }


    public void add( Quota quota )
    {
        Preconditions.checkNotNull( quota );
        Preconditions.checkNotNull( quota.getResource() );
        Preconditions.checkNotNull( quota.getResource().getContainerResourceType() );

        resources.put( quota.getResource().getContainerResourceType(), quota );
    }


    public void copyValues( final ContainerQuota containerQuota )
    {
        this.containerSize = containerQuota.getContainerSize();
        for ( Quota quota : containerQuota.getAll() )
        {
            add( quota );
        }
    }


    public Quota get( ContainerResourceType containerResourceType )
    {
        return resources.get( containerResourceType );
    }


    public ContainerSize getContainerSize()
    {
        return containerSize;
    }


    @JsonIgnore
    public Collection<Quota> getAll()
    {
        return resources.values();
    }


    @Override
    public String toString()
    {
        return "ContainerQuota{" + "resources=" + resources + '}';
    }
}
