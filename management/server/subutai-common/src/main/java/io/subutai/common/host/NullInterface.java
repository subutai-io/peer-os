package io.subutai.common.host;


/**
 * Null object for interface Interface
 */
public class NullInterface implements Interface
{
    private static Interface instance = new NullInterface();


    private NullInterface()
    {
    }


    public static Interface getInstance()
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
