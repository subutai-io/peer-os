/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.zookeeper.impl;


import java.util.Set;

import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.command.api.command.CommandRunnerBase;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;


/**
 * <p/> <p/> * @todo refactor addPropertyCommand & removePropertyCommand to not use custom scripts
 */
public class Commands
{

    public static final String PACKAGE_NAME = Common.PACKAGE_PREFIX + ZookeeperClusterConfig.PRODUCT_KEY.toLowerCase();


    public String getCheckInstalledCommand()
    {
        return "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH;
    }


    public String getInstallCommand()
    {
        return "apt-get --force-yes --assume-yes install " + PACKAGE_NAME;
    }


    public String getUninstallCommand()
    {
        return "apt-get --force-yes --assume-yes purge " + PACKAGE_NAME;
    }


    public String getStartCommand()
    {
        return "service zookeeper start";
    }


    public String getRestartCommand()
    {
        return "service zookeeper restart";
    }


    public String getStopCommand()
    {
        return "service zookeeper stop";
    }


    public String getStatusCommand()
    {
        return "service zookeeper status";
    }


    public String getConfigureClusterCommand( String zooCfgFileContents, String zooCfgFilePath, int id )
    {
        return String.format( ". /etc/profile && zookeeper-setID.sh %s && echo '%s' > %s", id,
                            zooCfgFileContents, zooCfgFilePath );
    }


    public String getAddPropertyCommand( String fileName, String propertyName, String propertyValue )
    {
        return
                String.format( ". /etc/profile && zookeeper-property.sh add %s %s %s", fileName, propertyName,
                        propertyValue );
    }


    public String getRemovePropertyCommand( String fileName, String propertyName )
    {
        return String.format( ". /etc/profile && zookeeper-property.sh remove %s %s", fileName, propertyName );
    }
}
