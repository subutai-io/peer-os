package org.safehaus.subutai.plugin.hbase.cli;


import java.util.UUID;

import org.safehaus.subutai.plugin.hbase.api.HBase;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command( scope = "hbase", name = "describe-cluster", description = "Shows the details of the Cassandra cluster." )
public class DescribeClusterCommand extends OsgiCommandSupport
{

    @Argument( index = 0, name = "clusterName", description = "The name of the cluster.", required = true,
            multiValued = false )
    String clusterName = null;
    private HBase hbaseManager;


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
        HBaseConfig config = hbaseManager.getCluster( clusterName );
        if ( config != null )
        {
            StringBuilder sb = new StringBuilder();
            sb.append( "Cluster name: " ).append( config.getClusterName() ).append( "\n" );
            sb.append( "Domain name: " ).append( config.getDomainName() ).append( "\n" );
            sb.append( "Master node: " ).append( config.getHbaseMaster() ).append( "\n" );
            sb.append( "Backup master node: " ).append( config.getBackupMasters() ).append( "\n" );
            sb.append( "Region nodes:" ).append( "\n" );
            for ( UUID agent : config.getRegionServers() )
            {
                sb.append( agent ).append( "\n" );
            }
            sb.append( "Quorum nodes:" ).append( "\n" );
            for ( UUID agent : config.getQuorumPeers() )
            {
                sb.append( agent ).append( "\n" );
            }
            System.out.println( sb.toString() );
        }
        else
        {
            System.out.println( "No clusters found..." );
        }

        return null;
    }
}
