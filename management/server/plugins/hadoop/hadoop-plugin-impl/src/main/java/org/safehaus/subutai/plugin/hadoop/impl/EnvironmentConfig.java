package org.safehaus.subutai.plugin.hadoop.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.Node;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;

import com.google.common.collect.Lists;


public class EnvironmentConfig {
    private HadoopClusterConfig config;
    private EnvironmentBlueprint blueprint;
    private Environment environment;


    public EnvironmentConfig( HadoopClusterConfig config, EnvironmentBlueprint blueprint ) {
        this.config = config;
        this.blueprint = blueprint;
    }


    public EnvironmentConfig( HadoopClusterConfig config, Environment environment ) {
        this.config = config;
        this.environment = environment;
    }


    public HadoopClusterConfig init() throws EnvironmentBuildException, ClusterSetupException {
        if ( environment == null && blueprint != null ) {
            environment = HadoopImpl.getEnvironmentManager().buildEnvironmentAndReturn( blueprint );
        }

        setMasterNodes( environment );
        setSlaveNodes( environment );

        return config;
    }


    private void setMasterNodes( Environment environment ) throws ClusterSetupException {
        Set<Agent> masterNodes = new HashSet<>();

        for ( Node node : environment.getNodes() ) {
            if ( NodeType.MASTER_NODE.name().equalsIgnoreCase( node.getNodeGroupName() ) ) {
                if ( node.getTemplate().getProducts().contains( Common.PACKAGE_PREFIX + config.getTemplateName() ) ) {
                    masterNodes.add( node.getAgent() );
                }
            }
        }

        if ( masterNodes.size() != HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY ) {
            throw new ClusterSetupException( String.format( "Hadoop master nodes must be %d in count",
                    HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY ) );
        }

        Iterator<Agent> masterIterator = masterNodes.iterator();
        config.setNameNode( masterIterator.next() );
        config.setSecondaryNameNode( masterIterator.next() );
        config.setJobTracker( masterIterator.next() );
    }


    private void setSlaveNodes( Environment environment ) throws ClusterSetupException {
        Set<Agent> slaveNodes = new HashSet<>();

        for ( Node node : environment.getNodes() ) {
            if ( NodeType.SLAVE_NODE.name().equalsIgnoreCase( node.getNodeGroupName() ) ) {
                if ( node.getTemplate().getProducts().contains( Common.PACKAGE_PREFIX + config.getTemplateName() ) ) {
                    slaveNodes.add( node.getAgent() );
                }
            }
        }

        if ( slaveNodes.isEmpty() ) {
            throw new ClusterSetupException( "Hadoop slave nodes are empty" );
        }

        config.setDataNodes( Lists.newArrayList( slaveNodes ) );
        config.setTaskTrackers( Lists.newArrayList( slaveNodes ) );
    }
}
