package org.safehaus.subutai.plugin.oozie.impl;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.common.AgentUtil;
import org.safehaus.subutai.plugin.oozie.api.OozieConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterConfigurationException;
import org.safehaus.subutai.shared.protocol.settings.Common;


/**
 * Created by bahadyr on 9/1/14.
 */
public class ClusterConfiguration {

    private ProductOperation po;
    private OozieImpl manager;


    public ClusterConfiguration( final ProductOperation productOperation, final OozieImpl oozieManager ) {

        this.po = productOperation;
        this.manager = oozieManager;
    }


    public void configureCluster( OozieConfig config ) throws ClusterConfigurationException {

        po.addLog( "Configuring root hosts..." );
        Agent server = manager.getAgentManager().getAgentByHostname( config.getServer() );
        Set<Agent> hadoopNodes = new HashSet<Agent>();
        for ( String hadoopNode : config.getHadoopNodes() ) {
            Agent hadoopNodeAgent = manager.getAgentManager().getAgentByHostname( hadoopNode );
            hadoopNodes.add( hadoopNodeAgent );
        }
        Command configureRootHostsCommand = Commands.getConfigureRootHostsCommand( hadoopNodes,
                AgentUtil.getAgentIpByMask( server, Common.IP_MASK ) );
        manager.getCommandRunner().runCommand( configureRootHostsCommand );

        if ( configureRootHostsCommand.hasSucceeded() ) {
            po.addLog( "Configuring root hosts successful." );
        }
        else {
            po.addLogFailed( String.format( "Configuration failed, %s", configureRootHostsCommand.getAllErrors() ) );
            return;
        }

        po.addLog( "Configuring root groups..." );
        Command configureRootGroupsCommand = Commands.getConfigureRootGroupsCommand( hadoopNodes );
        manager.getCommandRunner().runCommand( configureRootGroupsCommand );

        if ( configureRootGroupsCommand.hasSucceeded() ) {
            po.addLog( "Configuring root groups successful." );
        }
        else {
            po.addLogFailed( String.format( "Configuring failed, %s", configureRootGroupsCommand.getAllErrors() ) );
            return;
        }
        po.addLogDone( "Oozie installation succeeded" );
    }
}
