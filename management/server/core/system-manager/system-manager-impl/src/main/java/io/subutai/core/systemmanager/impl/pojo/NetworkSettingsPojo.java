package io.subutai.core.systemmanager.impl.pojo;


import io.subutai.core.systemmanager.api.pojo.NetworkSettings;


/**
 * Created by ermek on 2/9/16.
 */
public class NetworkSettingsPojo implements NetworkSettings
{
    public int securePortX1;
    public int securePortX2;
    public int securePortX3;


    public int getSecurePortX1()
    {
        return securePortX1;
    }


    public void setSecurePortX1( final int securePortX1 )
    {
        this.securePortX1 = securePortX1;
    }


    public int getSecurePortX2()
    {
        return securePortX2;
    }


    public void setSecurePortX2( final int securePortX2 )
    {
        this.securePortX2 = securePortX2;
    }


    public int getSecurePortX3()
    {
        return securePortX3;
    }


    public void setSecurePortX3( final int securePortX3 )
    {
        this.securePortX3 = securePortX3;
    }
}
