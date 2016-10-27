package io.subutai.common.host;


/**
 * Null object for interface Interface
 */
public class NullHostInterface extends HostInterfaceModel
{
    private static HostInterfaceModel instance = new NullHostInterface();


    private NullHostInterface()
    {
    }


    public static HostInterfaceModel getInstance()
    {
        return instance;
    }


    @Override
    public String getName()
    {
        throw new UnsupportedOperationException();
    }


    @Override
    public String getIp()
    {
        throw new UnsupportedOperationException();
    }
}
