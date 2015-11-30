package io.subutai.common.host;


/**
 * Null object for HostInfo interface
 */
public class NullHostInfo implements HostInfo
{
    private static final String NOT_AVAILABLE_TEXT = "N/A";
    private static HostInfo instance = new NullHostInfo();


    private NullHostInfo()
    {
    }


    public static HostInfo getInstance()
    {
        return instance;
    }


    @Override
    public String getId()
    {
        return NOT_AVAILABLE_TEXT;
    }


    @Override
    public String getHostname()
    {
        return NOT_AVAILABLE_TEXT;
    }


    @Override
    public HostInterfaces getHostInterfaces()
    {
        return new HostInterfaces();
    }


    @Override
    public HostArchitecture getArch()
    {
        return HostArchitecture.UNKNOWN;
    }


    @Override
    public int compareTo( final HostInfo o )
    {
        return -1;
    }
}
