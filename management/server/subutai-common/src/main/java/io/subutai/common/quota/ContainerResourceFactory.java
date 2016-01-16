package io.subutai.common.quota;


import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.ContainerResourceType;
import io.subutai.common.resource.NumericValueResource;
import io.subutai.common.resource.ResourceValue;


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
