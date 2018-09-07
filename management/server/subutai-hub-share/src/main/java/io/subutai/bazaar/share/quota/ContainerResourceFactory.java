package io.subutai.bazaar.share.quota;


import io.subutai.bazaar.share.resource.ByteValueResource;
import io.subutai.bazaar.share.resource.ContainerResourceType;
import io.subutai.bazaar.share.resource.NumericValueResource;
import io.subutai.bazaar.share.resource.ResourceValue;
import io.subutai.bazaar.share.resource.StringValueResource;


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
            case CPUSET:
                return new ContainerCpuSetResource( ( StringValueResource ) resourceValue );
            case RAM:
                return new ContainerRamResource( ( ByteValueResource ) resourceValue );
            case DISK:
                return new ContainerDiskResource( ( ByteValueResource ) resourceValue );
            default:
                return null;
        }
    }


    public static ContainerResource createContainerResource( ContainerResourceType containerResourceType,
                                                             String resourceValue )
    {
        switch ( containerResourceType )
        {
            case NET:
                return new ContainerNetResource( resourceValue );
            case CPU:
                return new ContainerCpuResource( resourceValue );
            case CPUSET:
                return new ContainerCpuSetResource( resourceValue );
            case RAM:
                return new ContainerRamResource( resourceValue );
            case DISK:
                return new ContainerDiskResource( resourceValue );
            default:
                return null;
        }
    }
}
