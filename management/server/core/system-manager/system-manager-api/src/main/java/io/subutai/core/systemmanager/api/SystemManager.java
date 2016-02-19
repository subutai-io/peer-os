package io.subutai.core.systemmanager.api;


import io.subutai.core.systemmanager.api.pojo.KurjunSettings;
import io.subutai.core.systemmanager.api.pojo.NetworkSettings;
import io.subutai.core.systemmanager.api.pojo.PeerSettings;
import io.subutai.core.systemmanager.api.pojo.SystemInfo;


/**
 * Created by ermek on 2/6/16.
 */
public interface SystemManager
{

    KurjunSettings getKurjunSettings();


    NetworkSettings getNetworkSettings();

    SystemInfo getSystemInfo();

    void setPeerSettings();

    PeerSettings getPeerSettings();

    void setNetworkSettings( String securePortX1, String securePortX2,
                             String securePortX3);

    boolean setKurjunSettings( String globalKurjunUrls, long publicDiskQuota, long publicThreshold,
                               long publicTimeFrame, long trustDiskQuota, long trustThreshold, long trustTimeFrame );
}
