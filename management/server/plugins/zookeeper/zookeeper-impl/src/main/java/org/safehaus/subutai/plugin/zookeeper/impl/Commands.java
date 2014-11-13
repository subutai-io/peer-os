/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.zookeeper.impl;


import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;


/**
 * <p/> <p/> * @todo refactor addPropertyCommand & removePropertyCommand to not use custom scripts
 */
public class Commands
{

    public static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + ZookeeperClusterConfig.PRODUCT_KEY.toLowerCase();


    public static String getCheckInstalledCommand()
    {
        return "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH;
    }


    public static String getInstallCommand()
    {
        return "apt-get --force-yes --assume-yes install " + PACKAGE_NAME;
    }


    public static String getUninstallCommand()
    {
        return "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME;
    }


    public static String getStartCommand()
    {
        return "service zookeeper start &";
    }


    public static String getRestartCommand()
    {
        return "service zookeeper restart &";
    }


    public static String getStopCommand()
    {
        return "service zookeeper stop";
    }


    public static String getStatusCommand()
    {
        return "service zookeeper status";
    }


    public static String getConfigureClusterCommand( String zooCfgFileContents, String zooCfgFilePath, int id )
    {
        return String.format( ". /etc/profile && zookeeper-setID.sh %s && echo '%s' > %s", id,
                            zooCfgFileContents, zooCfgFilePath );
    }


    public static String getAddPropertyCommand( String fileName, String propertyName, String propertyValue )
    {
        return
                String.format( ". /etc/profile && zookeeper-property.sh add %s %s %s", fileName, propertyName,
                        propertyValue );
    }


    public static String getRemovePropertyCommand( String fileName, String propertyName )
    {
        return String.format( ". /etc/profile && zookeeper-property.sh remove %s %s", fileName, propertyName );
    }
}
