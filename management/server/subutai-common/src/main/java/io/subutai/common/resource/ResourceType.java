package io.subutai.common.resource;


/**
 * Resource types
 */
public enum ResourceType
{
    RAM( "ram", MeasureUnit.MB ), CPU( "cpu", MeasureUnit.PERCENT ), OPT( "diskOpt", MeasureUnit.GB ),
    HOME( "diskHome", MeasureUnit.GB ), VAR( "diskVar", MeasureUnit.GB ),
    ROOTFS( "diskRootfs", MeasureUnit.GB );

    private String key;
    private MeasureUnit defaultMeasureUnit;


    ResourceType( String key, MeasureUnit defaultMeasureUnit )
    {
        this.key = key;
        this.defaultMeasureUnit = defaultMeasureUnit;
    }


    public String getKey()
    {
        return key;
    }


    public MeasureUnit getDefaultMeasureUnit()
    {
        return defaultMeasureUnit;
    }
}
