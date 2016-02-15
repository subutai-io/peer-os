package io.subutai.core.systemmanager.api.pojo;


/**
 * Created by ermek on 2/9/16.
 */
public interface NetworkSettings
{
    public String getExternalIpInterface();

    public void setExternalIpInterface( final String externalIpInterface );

    public void setOpenPort( int openPort );

    public int getOpenPort();

    public void setSecurePortX1( final int securePortX1 );

    public int getSecurePortX1();

    public void setSecurePortX2( final int securePortX2 );

    public int getSecurePortX2();

    public void setSecurePortX3( final int securePortX3 );

    public int getSecurePortX3();

    public void setSpecialPortX1( final int specialPortX1 );

    public int getSpecialPortX1();
}
