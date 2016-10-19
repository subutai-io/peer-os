package io.subutai.hub.share.quota;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
    private Map<ContainerResourceType, Quota> resources = new HashMap<>();


    public ContainerQuota()
    {
    }


    public ContainerQuota( Quota quota )
    {
        add( quota );
    }


    public ContainerQuota( Quota... quotas )
    {
        for ( int i = 0; i < quotas.length; i++ )
        {
            add( quotas[i] );
        }
    }


    public void add( Quota quota )
    {
        Preconditions.checkNotNull( quota );
        Preconditions.checkNotNull( quota.getResource() );
        Preconditions.checkNotNull( quota.getResource().getContainerResourceType() );

        resources.put( quota.getResource().getContainerResourceType(), quota );
    }


    public Quota get( ContainerResourceType containerResourceType )
    {
        return resources.get( containerResourceType );
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
