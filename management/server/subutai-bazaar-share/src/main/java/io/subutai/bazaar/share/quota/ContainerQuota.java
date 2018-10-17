package io.subutai.bazaar.share.quota;


import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import io.subutai.bazaar.share.resource.ContainerResourceType;


/**
 * Container quota class
 */
public class ContainerQuota
{
    @JsonProperty( value = "containerSize" )
    private ContainerSize containerSize;

    @JsonProperty( value = "resources" )
    private Map<ContainerResourceType, Quota> resources;


    public ContainerQuota( @JsonProperty( value = "containerSize" ) final ContainerSize containerSize,
                           @JsonProperty( value = "resources" ) final Map<ContainerResourceType, Quota> resources )
    {
        this.containerSize = containerSize;
        this.resources = resources;
    }


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


    protected Map<ContainerResourceType, Quota> getResources()
    {
        if ( this.resources == null )
        {
            this.resources = new HashMap<>();
        }
        return this.resources;
    }


    public void add( Quota quota )
    {
        Preconditions.checkNotNull( quota );
        Preconditions.checkNotNull( quota.getResource() );
        Preconditions.checkNotNull( quota.getResource().getContainerResourceType() );

        getResources().put( quota.getResource().getContainerResourceType(), quota );
    }


    public void addAll( final List<Quota> quota )
    {
        for ( Quota q : quota )
        {
            add( q );
        }
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
        return getResources().get( containerResourceType );
    }


    public ContainerSize getContainerSize()
    {
        return containerSize;
    }


    @JsonIgnore
    public Collection<Quota> getAll()
    {
        return getResources().values();
    }


    @Override
    public String toString()
    {
        return "ContainerQuota{" + "resources=" + getResources() + '}';
    }
}
