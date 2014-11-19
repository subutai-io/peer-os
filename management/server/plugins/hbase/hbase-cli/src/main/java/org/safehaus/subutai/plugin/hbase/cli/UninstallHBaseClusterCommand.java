package org.safehaus.subutai.plugin.hbase.cli;


import java.util.UUID;

import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command( scope = "hbase", name = "uninstall-cluster", description = "Command to uninstall HBase cluster" )
public class UninstallHBaseClusterCommand extends OsgiCommandSupport
{

    @Argument( index = 0, name = "clusterName", required = true, multiValued = false, description = "Delete cluster" )
    String clusterName;
    private HBase hbaseManager;
    private Tracker tracker;


    public Tracker getTracker()
    {
        return tracker;
    }


    public void setTracker( Tracker tracker )
    {
        this.tracker = tracker;
    }


    public HBase getHbaseManager()
    {
        return hbaseManager;
    }


    public void setHbaseManager( HBase hbaseManager )
    {
        this.hbaseManager = hbaseManager;
    }


    protected Object doExecute()
    {

        UUID uuid = hbaseManager.uninstallCluster( clusterName );
        tracker.printOperationLog( HBaseConfig.PRODUCT_KEY, uuid, 30000 );
        return null;
    }
}
