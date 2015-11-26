package io.subutai.common.quota;


import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;


public class CommonQuota
{
    private ResourceType resourceType;
    private ResourceValue resourceValue;


    public CommonQuota( final ResourceType resourceType, final ResourceValue resourceValue )
    {
        this.resourceType = resourceType;
        this.resourceValue = resourceValue;
    }


    public ResourceValue getValue() {return resourceValue;}


    public ResourceType getType() {return resourceType;}
}
