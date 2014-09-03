package org.safehaus.subutai.plugin.hadoop.impl.operation;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopDbSetupStrategy;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.operation.common.AddNodeOperation;

import com.google.common.base.Strings;


public class Adding {
    private HadoopClusterConfig hadoopClusterConfig;
    private String clusterName;


    public Adding( String clusterName ) {
        this.clusterName = clusterName;
    }


    public UUID execute() {
        final ProductOperation po = HadoopImpl.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
                "Adding node to Hadoop" );

        HadoopImpl.getExecutor().execute( new Runnable() {
            @Override
            public void run() {

                try {
                    hadoopClusterConfig = HadoopImpl.getPluginDAO()
                                                    .getInfo( HadoopClusterConfig.PRODUCT_KEY, clusterName,
                                                            HadoopClusterConfig.class );
                }
                catch ( DBException e ) {
                    po.addLogFailed( e.getMessage() );
                }

                if ( hadoopClusterConfig == null ||
                        Strings.isNullOrEmpty( hadoopClusterConfig.getClusterName() ) ||
                        Strings.isNullOrEmpty( hadoopClusterConfig.getDomainName() ) ) {
                    po.addLogFailed( "Malformed configuration\nHadoop adding new node aborted" );
                    return;
                }

                try {
                    po.addLog( String.format( "Creating %d lxc container...", 1 ) );
                    Set<Agent> cfgServers = HadoopImpl.getContainerManager()
                                                      .clone( hadoopClusterConfig.getTemplateName(),
                                                              HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY,
                                                              HadoopImpl.getAgentManager().getPhysicalAgents(),
                                                              HadoopDbSetupStrategy.getNodePlacementStrategyByNodeType(
                                                                      NodeType.SLAVE_NODE ) );
                    Agent agent = null;

                    for ( Agent a : cfgServers ) {
                        agent = a;
                    }
                    po.addLog( "Lxc containers created successfully\nConfiguring network..." );

                    if ( HadoopImpl.getNetworkManager().configHostsOnAgents( hadoopClusterConfig.getAllNodes(), agent,
                            hadoopClusterConfig.getDomainName() ) && HadoopImpl.getNetworkManager().configSshOnAgents(
                            hadoopClusterConfig.getAllNodes(), agent ) ) {
                        po.addLog( "Cluster network configured" );

                        AddNodeOperation addOperation = new AddNodeOperation( hadoopClusterConfig, agent );
                        for ( Command command : addOperation.getCommandList() ) {
                            po.addLog( ( String.format( "%s started...", command.getDescription() ) ) );
                            HadoopImpl.getCommandRunner().runCommand( command );

                            if ( command.hasSucceeded() ) {
                                po.addLogDone( String.format( "%s succeeded", command.getDescription() ) );
                            }
                            else {
                                po.addLogFailed( String.format( "%s failed, %s", command.getDescription(),
                                        command.getAllErrors() ) );
                            }
                        }

                        hadoopClusterConfig.getTaskTrackers().add( agent );
                        hadoopClusterConfig.getDataNodes().add( agent );

                        try {
                            HadoopImpl.getPluginDAO()
                                      .saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
                                              hadoopClusterConfig );
                            po.addLog( "Cluster info saved to DB" );
                        }
                        catch ( DBException e ) {
                            po.addLogFailed( "Could not save cluster info to DB! Please see logs\n"
                                    + "Adding new node aborted" );
                        }
                    }
                    else {
                        po.addLogFailed( "Could not configure network! Please see logs\nLXC creation aborted" );
                    }
                }
                catch ( LxcCreateException ex ) {
                    po.addLogFailed( ex.getMessage() );
                }
            }
        } );

        return po.getId();
    }
}
