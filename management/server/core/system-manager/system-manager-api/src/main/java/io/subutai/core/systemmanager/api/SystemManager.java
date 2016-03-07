package io.subutai.core.systemmanager.api;


import org.apache.commons.configuration.ConfigurationException;

import io.subutai.common.command.CommandException;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.core.systemmanager.api.pojo.AdvancedSettings;
import io.subutai.core.systemmanager.api.pojo.KurjunSettings;
import io.subutai.core.systemmanager.api.pojo.NetworkSettings;
import io.subutai.core.systemmanager.api.pojo.PeerSettings;
import io.subutai.core.systemmanager.api.pojo.SystemInfo;


public interface SystemManager
{

    KurjunSettings getKurjunSettings() throws ConfigurationException;


    NetworkSettings getNetworkSettings() throws ConfigurationException;

    SystemInfo getSystemInfo() throws ConfigurationException, HostNotFoundException, CommandException;

    void setPeerSettings();

    PeerSettings getPeerSettings();

    void setNetworkSettings( String securePortX1, String securePortX2, String securePortX3, final String publicUrl,
                             final String agentPort ) throws ConfigurationException;

    boolean setKurjunSettings( String[] globalKurjunUrls, long publicDiskQuota, long publicThreshold,
                               long publicTimeFrame, long trustDiskQuota, long trustThreshold, long trustTimeFrame )
            throws ConfigurationException;

    AdvancedSettings getAdvancedSettings();

    void setKurjunSettingsUrls( String[] globalKurjunUrls ) throws ConfigurationException;

    boolean setKurjunSettingsQuotas( long publicDiskQuota, long publicThreshold, long publicTimeFrame,
                                     long trustDiskQuota, long trustThreshold, long trustTimeFrame );
}
