package org.safehaus.subutai.cli.commands;


import java.util.List;

import org.safehaus.subutai.api.hbase.HBase;
import org.safehaus.subutai.api.hbase.HBaseConfig;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "hbase", name = "list-clusters", description = "mydescription")
public class ListHBaseClustersCommand extends OsgiCommandSupport {

    private HBase hbaseManager;


    public HBase getHbaseManager() {
        return hbaseManager;
    }


    public void setHbaseManager( HBase hbaseManager ) {
        this.hbaseManager = hbaseManager;
    }


    protected Object doExecute() {

        List<HBaseConfig> configs = hbaseManager.getClusters();
        StringBuilder sb = new StringBuilder();

        for ( HBaseConfig config : configs ) {
            sb.append( config.getClusterName() ).append( "\n" );
        }

        System.out.println( sb.toString() );

        return null;
    }
}
