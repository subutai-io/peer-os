package org.safehaus.subutai.plugin.accumulo.impl;


import org.safehaus.subutai.common.settings.Common;


public class Commands
{

    public static final String installCommand = "apt-get --force-yes --assume-yes install ";

    public static final String uninstallCommand = "apt-get --force-yes --assume-yes purge ";

    public static final String startCommand = "/etc/init.d/accumulo start";

    public static final String stopCommand = "/etc/init.d/accumulo stop";

    public static final String statusCommand = "/etc/init.d/accumulo status";

    public static final String checkIfInstalled = "dpkg -l | grep '^ii' | grep " + Common.PACKAGE_PREFIX_WITHOUT_DASH;


    public static String getAddMasterCommand( String hostname )
    {
        return ". /etc/profile && accumuloMastersConf.sh masters clear && accumuloMastersConf.sh masters add "
                + hostname;
    }


    public static String getAddTracersCommand( String serializedHostNames )
    {
        return ". /etc/profile && accumuloMastersConf.sh tracers clear && accumuloMastersConf.sh tracers add "
                + serializedHostNames;
    }


    public static String getClearTracerCommand( String hostname )
    {
        return ". /etc/profile && accumuloMastersConf.sh tracers clear " + hostname;
    }


    public static String getAddGCCommand( String hostname )
    {
        return ". /etc/profile && accumuloMastersConf.sh gc clear && accumuloMastersConf.sh gc add " + hostname;
    }


    public static String getAddMonitorCommand( String hostname )
    {
        return ". /etc/profile && accumuloMastersConf.sh monitor clear && accumuloMastersConf.sh monitor add "
                + hostname;
    }


    public static String getAddSlavesCommand( String serializedHostNames )
    {
        return ". /etc/profile && accumuloSlavesConf.sh slaves clear && accumuloSlavesConf.sh slaves add "
                + serializedHostNames;
    }


    public static String getClearSlaveCommand( String hostname )
    {
        return ". /etc/profile && accumuloSlavesConf.sh slaves clear " + hostname;
    }


    public static String getBindZKClusterCommand( String zkNodesCommaSeparated )
    {
        return ". /etc/profile && accumulo-conf.sh remove accumulo-site.xml instance.zookeeper.host && "
                + "accumulo-conf.sh add accumulo-site.xml instance.zookeeper.host " + zkNodesCommaSeparated;
    }


    public static String getInitCommand( String instanceName, String password )
    {
        return ". /etc/profile && accumulo-init.sh " + instanceName + " " + password;
    }


    public static String getAddPropertyCommand( String propertyName, String propertyValue )
    {
        return ". /etc/profile && accumulo-property.sh add " + propertyName + " " + propertyValue;
    }


    public static String getRemovePropertyCommand( String propertyName )
    {
        return ". /etc/profile && accumulo-property.sh clear " + propertyName;
    }


    public static String getRemoveAccumuloFromHFDSCommand()
    {
        return ". /etc/profile && hadoop dfs -rmr /accumulo";
    }
}
