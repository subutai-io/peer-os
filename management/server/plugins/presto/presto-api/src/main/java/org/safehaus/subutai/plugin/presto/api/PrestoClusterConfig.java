package org.safehaus.subutai.plugin.presto.api;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;


public class PrestoClusterConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Presto";
    public static final String TEMAPLTE_NAME = "presto";

    private String clusterName = "";
    private SetupType setupType;
    // over-Hadoop params
    private String hadoopClusterName = "";
    private Set<Agent> workers = new HashSet<>();
    private Agent coordinatorNode;


    @Override
    public String getClusterName()
    {
        return clusterName;
    }


    @Override
    public String getProductName()
    {
        return PRODUCT_KEY;
    }

    @Override
    public String getProductKey() {
        return PRODUCT_KEY;
    }


    public void setClusterName( String clusterName )
    {
        this.clusterName = clusterName;
    }


    public SetupType getSetupType()
    {
        return setupType;
    }


    public void setSetupType( SetupType setupType )
    {
        this.setupType = setupType;
    }


    public String getHadoopClusterName()
    {
        return hadoopClusterName;
    }


    public void setHadoopClusterName( String hadoopClusterName )
    {
        this.hadoopClusterName = hadoopClusterName;
    }


    public Set<Agent> getWorkers()
    {
        return workers;
    }


    public void setWorkers( Set<Agent> workers )
    {
        this.workers = workers;
    }


    public Agent getCoordinatorNode()
    {
        return coordinatorNode;
    }


    public void setCoordinatorNode( Agent coordinatorNode )
    {
        this.coordinatorNode = coordinatorNode;
    }


    public Set<Agent> getAllNodes()
    {
        Set<Agent> allNodes = new HashSet<>();
        if ( workers != null )
        {
            allNodes.addAll( workers );
        }
        if ( coordinatorNode != null )
        {
            allNodes.add( coordinatorNode );
        }
        return allNodes;
    }


    @Override
    public String toString()
    {
        return "Config{" + "clusterName=" + clusterName + ", coordinatorNode=" + coordinatorNode + ", workers="
                + workers + '}';
    }
}
