package io.subutai.common.resource;


/**
 * Container resource types
 */
public enum ContainerResourceType
{
    RAM( "ram" ), CPU( "cpu" ), OPT( "diskOpt" ),
    HOME( "diskHome" ), VAR( "diskVar" ),
    ROOTFS( "diskRootfs" );

    private String key;
//    private MeasureUnit defaultMeasureUnit;


    ContainerResourceType( String key )
    {
        this.key = key;
//        this.defaultMeasureUnit = defaultMeasureUnit;
    }


    public String getKey()
    {
        return key;
    }


//    public MeasureUnit getDefaultMeasureUnit()
    //    {
    //        return defaultMeasureUnit;
    //    }
}
