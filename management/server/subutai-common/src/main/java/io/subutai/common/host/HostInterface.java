package io.subutai.common.host;


import org.codehaus.jackson.annotate.JsonProperty;


/**
 * Host interface
 */
public class HostInterface implements Interface
{
    @JsonProperty
    private String name;
    @JsonProperty
    private String ip;
    @JsonProperty
    private String mac;


    public HostInterface() {}


    public HostInterface( final String name, final String ip, final String mac )
    {
        this.name = name;
        this.ip = ip;
        this.mac = mac;
    }


    public HostInterface( final Interface s )
    {
        this.name = s.getName();
        this.ip = s.getIp().replace( "addr:", "" );
        this.mac = s.getMac();
    }


    @Override
    public String getName()
    {
        return this.name;
    }


    @Override
    public String getIp()
    {
        return this.ip;
    }


    @Override
    public String getMac()
    {
        return this.mac;
    }
}
