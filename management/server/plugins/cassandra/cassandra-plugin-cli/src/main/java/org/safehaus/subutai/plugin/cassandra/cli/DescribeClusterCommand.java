package org.safehaus.subutai.plugin.cassandra.cli;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command(scope = "cassandra", name = "describe-cluster", description = "Shows the details of the Cassandra cluster.")
public class DescribeClusterCommand extends OsgiCommandSupport
{

    @Argument(index = 0, name = "clusterName", description = "The name of the cluster.", required = true,
            multiValued = false)
    String clusterName = null;
    private Cassandra cassandraManager;


    public Cassandra getCassandraManager()
    {
        return cassandraManager;
    }


    public void setCassandraManager( Cassandra cassandraManager )
    {
        this.cassandraManager = cassandraManager;
    }


    protected Object doExecute()
    {
        CassandraClusterConfig config = cassandraManager.getCluster( clusterName );
        if ( config != null )
        {
            StringBuilder sb = new StringBuilder();
            sb.append( "Cluster name: " ).append( config.getClusterName() ).append( "\n" );
            sb.append( "Nodes:" ).append( "\n" );
            for ( Agent agent : config.getNodes() )
            {
                sb.append( "Hostname: " ).append( agent.getHostname() ).append( ", Agent UUID: " )
                  .append( agent.getUuid() ).append( "\n" );
            }
            sb.append( "Seeds:" ).append( "\n" );
            for ( Agent agent : config.getSeedNodes() )
            {
                sb.append( "Hostname: " ).append( agent.getHostname() ).append( ", Agent UUID: " )
                  .append( agent.getUuid() ).append( "\n" );
            }
            sb.append( "Data directory: " ).append( config.getDataDirectory() ).append( "\n" );
            sb.append( "Commit log directory: " ).append( config.getCommitLogDirectory() ).append( "\n" );
            sb.append( "Saved cache directory: " ).append( config.getSavedCachesDirectory() ).append( "\n" );
            sb.append( "Domain name: " ).append( config.getDomainName() ).append( "\n" );
            System.out.println( sb.toString() );
        }
        else
        {
            System.out.println( "No clusters found..." );
        }

        return null;
    }
}
