package org.safehaus.subutai.plugin.hadoop.cli;


import org.safehaus.subutai.plugin.hadoop.api.Hadoop;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command( scope = "hadoop", name = "describe-clusters", description = "Shows the details of Hadoop cluster" )
public class DescribeClusterCommand extends OsgiCommandSupport
{

    @Argument( index = 0, name = "clusterName", required = true, multiValued = false,
            description = "The name of the Hadoop cluster" )
    String clusterName;
    private Hadoop hadoopManager;


    public Hadoop getHadoopManager()
    {
        return hadoopManager;
    }


    public void setHadoopManager( Hadoop hadoopManager )
    {
        this.hadoopManager = hadoopManager;
    }


    protected Object doExecute()
    {
        //        HadoopClusterConfig hadoopClusterConfig = hadoopManager.getCluster( clusterName );
        //        if ( hadoopClusterConfig != null )
        //        {
        //            StringBuilder sb = new StringBuilder();
        //            sb.append( "Cluster name: " ).append( hadoopClusterConfig.getClusterName() ).append( "\n" );
        //            sb.append( "Domain name: " ).append( hadoopClusterConfig.getDomainName() ).append( "\n" );
        //            sb.append( "All nodes:" ).append( "\n" );
        //            for ( Agent agent : hadoopClusterConfig.getAllNodes() )
        //            {
        //                sb.append( "Hostname: " ).append( agent.getHostname() ).append( "\n" );
        //            }
        //            sb.append( "Slave nodes:" ).append( "\n" );
        //            for ( Agent agent : hadoopClusterConfig.getAllSlaveNodes() )
        //            {
        //                sb.append( "Hostname: " ).append( agent.getHostname() ).append( "\n" );
        //            }
        //            sb.append( "Data nodes:" ).append( "\n" );
        //            for ( Agent agent : hadoopClusterConfig.getDataNodes() )
        //            {
        //                sb.append( "Hostname: " ).append( agent.getHostname() ).append( "\n" );
        //            }
        //            sb.append( "Task trackers:" ).append( "\n" );
        //            for ( Agent agent : hadoopClusterConfig.getTaskTrackers() )
        //            {
        //                sb.append( "Hostname: " ).append( agent.getHostname() ).append( "\n" );
        //            }
        //            Agent jt = hadoopClusterConfig.getJobTracker();
        //            sb.append( "Job tracker" ).append( "\n" );
        //            sb.append( "Hostname:" ).append( jt.getHostname() ).append( "\n" );
        //            sb.append( "IPs:" ).append( jt.getListIP() ).append( "\n" );
        //            sb.append( "MAC address:" ).append( jt.getMacAddress() ).append( "\n" );
        //            sb.append( "Parent hostname:" ).append( jt.getParentHostName() ).append( "\n" );
        //            sb.append( "UUID:" ).append( jt.getUuid() ).append( "\n" );
        //            System.out.println( sb.toString() );
        //        }
        //        else
        //        {
        //            System.out.println( "No clusters found..." );
        //        }

        return null;
    }
}
