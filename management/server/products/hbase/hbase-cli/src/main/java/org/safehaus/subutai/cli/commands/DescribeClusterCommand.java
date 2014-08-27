package org.safehaus.subutai.cli.commands;


import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.hbase.HBase;
import org.safehaus.subutai.api.hbase.HBaseConfig;

import java.util.UUID;


/**
 * Displays the last log entries
 */
@Command(scope = "hbase", name = "describe-cluster", description = "Shows the details of the Cassandra cluster.")
public class DescribeClusterCommand extends OsgiCommandSupport
{

    private HBase hbaseManager;


    public HBase getHbaseManager()
    {
        return hbaseManager;
    }


    public void setHbaseManager( HBase hbaseManager )
    {
        this.hbaseManager = hbaseManager;
    }


    @Argument(index = 0, name = "clusterName", description = "The name of the cluster.", required = true,
        multiValued = false)
    String clusterName = null;


    protected Object doExecute()
    {
        HBaseConfig config = hbaseManager.getCluster( clusterName );
        if ( config != null )
        {
            StringBuilder sb = new StringBuilder();
            sb.append( "Cluster name: " ).append( config.getClusterName() ).append( "\n" );
            sb.append( "Domain name: " ).append( config.getDomainInfo() ).append( "\n" );
            sb.append( "Master node: " ).append( config.getMaster() ).append( "\n" );
            sb.append( "Backup master node: " ).append( config.getBackupMasters() ).append( "\n" );
            sb.append( "Region nodes:" ).append( "\n" );
            for ( UUID uuid : config.getRegion() )
            {
                sb.append( uuid ).append( "\n" );
            }
            sb.append( "Quorum nodes:" ).append( "\n" );
            for ( UUID uuid : config.getQuorum() )
            {
                sb.append( uuid ).append( "\n" );
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
