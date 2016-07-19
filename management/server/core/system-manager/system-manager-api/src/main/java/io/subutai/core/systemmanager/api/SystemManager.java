package io.subutai.core.systemmanager.api;


import org.apache.commons.configuration.ConfigurationException;

import io.subutai.core.systemmanager.api.pojo.AdvancedSettings;
import io.subutai.core.systemmanager.api.pojo.KurjunSettings;
import io.subutai.core.systemmanager.api.pojo.NetworkSettings;
import io.subutai.core.systemmanager.api.pojo.PeerSettings;
import io.subutai.core.systemmanager.api.pojo.SystemInfo;


public interface SystemManager
{

    KurjunSettings getKurjunSettings() throws ConfigurationException;


    NetworkSettings getNetworkSettings() throws ConfigurationException;

    SystemInfo getSystemInfo();

    void setPeerSettings();

    PeerSettings getPeerSettings();

    void setNetworkSettings( final String publicUrl, final String publicSecurePort, final String startRange,
                             final String endRange ) throws ConfigurationException;

    AdvancedSettings getAdvancedSettings();

    void setKurjunSettingsUrls( String[] globalKurjunUrls, final String[] localKurjunUrls )
            throws ConfigurationException;

    boolean setKurjunSettingsQuotas( long publicDiskQuota, long publicThreshold, long publicTimeFrame,
                                     long trustDiskQuota, long trustThreshold, long trustTimeFrame );

    SystemInfo getManagementUpdates();

    boolean updateManagement();
}
