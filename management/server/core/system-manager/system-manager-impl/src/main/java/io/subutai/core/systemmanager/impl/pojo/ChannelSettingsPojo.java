package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.ChannelSettings;


/**
 * Created by ermek on 2/9/16.
 */
public class ChannelSettingsPojo implements ChannelSettings
{
    public String openPort = "8080";
    public String securePortX1 = "8443";
    public String securePortX2 = "8444";
    public String securePortX3 = "8445";
    public String specialPortX1 = "8551";


    public String getOpenPort()
    {
        return openPort;
    }


    public void setOpenPort( final String openPort )
    {
        this.openPort = openPort;
    }


    public String getSecurePortX1()
    {
        return securePortX1;
    }


    public void setSecurePortX1( final String securePortX1 )
    {
        this.securePortX1 = securePortX1;
    }


    public String getSecurePortX2()
    {
        return securePortX2;
    }


    public void setSecurePortX2( final String securePortX2 )
    {
        this.securePortX2 = securePortX2;
    }


    public String getSecurePortX3()
    {
        return securePortX3;
    }


    public void setSecurePortX3( final String securePortX3 )
    {
        this.securePortX3 = securePortX3;
    }


    public String getSpecialPortX1()
    {
        return specialPortX1;
    }


    public void setSpecialPortX1( final String specialPortX1 )
    {
        this.specialPortX1 = specialPortX1;
    }
}
