package org.safehaus.subutai.plugin.oozie.impl;


import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.AgentUtil;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;

import com.google.common.collect.Sets;


/**
 * Created by bahadyr on 9/1/14.
 */
public class ClusterConfiguration
{

    private TrackerOperation po;
    private OozieImpl manager;


    public ClusterConfiguration( final TrackerOperation trackerOperation, final OozieImpl oozieManager )
    {

        this.po = trackerOperation;
        this.manager = oozieManager;
    }


    public void configureCluster( OozieClusterConfig config ) throws ClusterConfigurationException
    {

        /*po.addLog( "Configuring root hosts..." );
        Agent server = config.getServer();
        HadoopClusterConfig hadoopClusterConfig =
                manager.getHadoopManager().getCluster( config.getHadoopClusterName() );
        //        Set<Agent> hadoopNodes = new HashSet<Agent>();
        //        for ( String hadoopNode : config.ge) {
        //            Agent hadoopNodeAgent = manager.getAgentManager().getAgentByHostname( hadoopNode );
        //            hadoopNodes.add( hadoopNodeAgent );
        //        }

        Command configureRootHostsCommand = manager.getCommands().getConfigureRootHostsCommand(
                Sets.newHashSet( hadoopClusterConfig.getAllNodes() ),
                AgentUtil.getAgentIpByMask( server, Common.IP_MASK ) );
        manager.getCommandRunner().runCommand( configureRootHostsCommand );

        if ( configureRootHostsCommand.hasSucceeded() )
        {
            po.addLog( "Configuring root hosts successful." );
        }
        else
        {
            po.addLogFailed( String.format( "Configuration failed, %s", configureRootHostsCommand.getAllErrors() ) );
            return;
        }

        po.addLog( "Configuring root groups..." );
        Command configureRootGroupsCommand = manager.getCommands().getConfigureRootGroupsCommand(
                Sets.newHashSet( hadoopClusterConfig.getAllNodes() ) );
        manager.getCommandRunner().runCommand( configureRootGroupsCommand );

        if ( configureRootGroupsCommand.hasSucceeded() )
        {
            po.addLog( "Configuring root groups successful." );
        }
        else
        {
            po.addLogFailed( String.format( "Configuring failed, %s", configureRootGroupsCommand.getAllErrors() ) );
            return;
        }
        po.addLogDone( "Oozie installation succeeded" );*/
    }
}
