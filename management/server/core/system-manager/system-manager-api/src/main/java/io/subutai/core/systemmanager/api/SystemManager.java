package io.subutai.core.systemmanager.api;


import java.util.List;

import org.apache.commons.configuration.ConfigurationException;

import io.subutai.core.systemmanager.api.pojo.AdvancedSettings;
import io.subutai.core.systemmanager.api.pojo.NetworkSettings;
import io.subutai.core.systemmanager.api.pojo.PeerSettings;
import io.subutai.core.systemmanager.api.pojo.SystemInfo;
import io.subutai.core.systemmanager.api.pojo.UpdateDto;


public interface SystemManager
{

    NetworkSettings getNetworkSettings() throws ConfigurationException;

    SystemInfo getSystemInfo();

    void setPeerSettings();

    PeerSettings getPeerSettings();

    void setNetworkSettings( final String publicUrl, final String publicSecurePort, final boolean userRhIp )
            throws ConfigurationException;

    AdvancedSettings getAdvancedSettings( String logFile );

    SystemInfo getManagementUpdates();

    boolean updateManagement();

    List<UpdateDto> getUpdates();

    boolean isUpdateInProgress();

    boolean isEnvironmentWorkflowInProgress();

    String getBazaarIp();
}
