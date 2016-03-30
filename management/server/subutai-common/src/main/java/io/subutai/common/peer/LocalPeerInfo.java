package io.subutai.common.peer;


import io.subutai.common.settings.SystemSettings;


public class LocalPeerInfo extends PeerInfo
{

    @Override
    public String getPublicUrl()
    {
        if ( SystemSettings.DEFAULT_PUBLIC_URL.equalsIgnoreCase( SystemSettings.getPublicUrl() ) )
        {
            return super.getPublicUrl();
        }
        else
        {
            return SystemSettings.getPublicUrl();
        }
    }


    @Override
    public int getPublicSecurePort()
    {
        if ( SystemSettings.DEFAULT_PUBLIC_SECURE_PORT == SystemSettings.getPublicSecurePort() )
        {
            return super.getPublicSecurePort();
        }
        else
        {
            return SystemSettings.getPublicSecurePort();
        }
    }
}
