package io.subutai.hub.share.quota;


import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;
import io.subutai.hub.share.resource.NumericValueResource;
import io.subutai.hub.share.resource.ResourceValue;

import static io.subutai.hub.share.resource.ContainerResourceType.*;


/**
 * Factory method for container resource classes
 */
public class ContainerResourceFactory
{
    public static ContainerResource createContainerResource( ContainerResourceType containerResourceType,
                                                             ResourceValue resourceValue )
    {
        switch ( containerResourceType )
        {
            case CPU:
                return new ContainerCpuResource( ( NumericValueResource ) resourceValue );
            case RAM:
                return new ContainerRamResource( ( ByteValueResource ) resourceValue );
            case HOME:
                return new ContainerHomeResource( ( ByteValueResource ) resourceValue );
            case OPT:
                return new ContainerOptResource( ( ByteValueResource ) resourceValue );
            case VAR:
                return new ContainerVarResource( ( ByteValueResource ) resourceValue );
            case ROOTFS:
                return new ContainerRootfsResource( ( ByteValueResource ) resourceValue );
            default:
                return null;
        }
    }
}
