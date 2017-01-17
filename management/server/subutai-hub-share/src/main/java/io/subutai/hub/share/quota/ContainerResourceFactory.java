package io.subutai.hub.share.quota;


import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;
import io.subutai.hub.share.resource.NumericValueResource;
import io.subutai.hub.share.resource.ResourceValue;


/**
 * Factory method for container resource classes
 */
public class ContainerResourceFactory
{
    private ContainerResourceFactory()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static ContainerResource createContainerResource( ContainerResourceType containerResourceType,
                                                             ResourceValue resourceValue )
    {
        switch ( containerResourceType )
        {
            case NET:
                return new ContainerNetResource( ( NumericValueResource ) resourceValue );
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
