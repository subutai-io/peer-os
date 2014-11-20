package org.safehaus.subutai.plugin.hbase.cli;


import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hbase.api.HBase;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command( scope = "hbase", name = "install-cluster", description = "Command to install HBase cluster" )
public class InstallHBaseClusterCommand extends OsgiCommandSupport
{

    private Tracker tracker;
    private HBase hbaseManager;


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

        System.out.println( "install" );
        return null;
    }
}
