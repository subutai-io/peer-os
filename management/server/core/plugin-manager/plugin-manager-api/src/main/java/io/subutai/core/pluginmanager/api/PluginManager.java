package io.subutai.core.pluginmanager.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.subutai.common.datatypes.RelationDeclaration;


public interface PluginManager
{
    UUID installPlugin( String pluginName );

    UUID removePlugin( String pluginName );

    UUID upgradePlugin( String pluginName );

    Set<PluginInfo> getInstalledPlugins();

    Set<PluginInfo> getAvailablePlugins();

    PluginInfo getPluginInfo( String pluginName, @RelationDeclaration( context = "plugin" ) String version );

    Set<String> getAvailablePluginNames();

    List<String> getAvaileblePluginVersions();

    List<String> getInstalledPluginVersions();

    Set<String> getInstalledPluginNames();

    String getPluginVersion( String pluginName );

    boolean isUpgradeAvailable( String pluginName );

    String getProductKey();

    boolean isInstalled( String p );

    boolean operationSuccessful( OperationType operationType );
}
