package io.subutai.core.systemmanager.api.pojo;


/**
 * Created by ermek on 2/9/16.
 */
public interface ChannelSettings
{
    public void setOpenPort( String openPort );

    public String getOpenPort();

    public void setSecurePortX1( final String securePortX1 );

    public String getSecurePortX1();

    public void setSecurePortX2( final String securePortX2 );

    public String getSecurePortX2();

    public void setSecurePortX3( final String securePortX3 );

    public String getSecurePortX3();

    public void setSpecialPortX1( final String specialPortX1 );

    public String getSpecialPortX1();
}
