package io.subutai.common.quota;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;

import io.subutai.common.resource.ContainerResourceType;


/**
 * Container quota class
 */
public class ContainerQuota
{
    @JsonProperty
    private Map<ContainerResourceType, Quota> resources = new HashMap<>();


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
