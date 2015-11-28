package io.subutai.common.host;


/**
 * Null object for interface Interface
 */
public class NullHostInterface implements HostInterface
{
    private static HostInterface instance = new NullHostInterface();


    private NullHostInterface()
    {
    }


    public static HostInterface getInstance()
    {
        return instance;
    }


    @Override
    public String getName()
    {
        return "NULL";
    }


    @Override
    public String getIp()
    {
        return "0.0.0.0";
    }


    @Override
    public String getMac()
    {
        return "00:00:00:00:00";
    }
}
