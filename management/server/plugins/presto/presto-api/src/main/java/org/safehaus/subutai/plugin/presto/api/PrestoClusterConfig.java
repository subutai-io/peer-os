package org.safehaus.subutai.plugin.presto.api;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;


public class PrestoClusterConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Presto";
    public static final String TEMPLATE_NAME = "presto";

    private String clusterName = "";
    private SetupType setupType;
    // over-Hadoop params
    private String hadoopClusterName = "";
    private Set<UUID> workers = new HashSet<>();
    private Set<UUID> hadoopNodes = new HashSet<>();
    private UUID coordinatorNode;
    private UUID environmentId;


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
    public String getProductKey()
    {
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


    public Set<UUID> getWorkers()
    {
        return workers;
    }


    public void setWorkers( Set<UUID> workers )
    {
        this.workers = workers;
    }


    public UUID getCoordinatorNode()
    {
        return coordinatorNode;
    }


    public void setCoordinatorNode( UUID coordinatorNode )
    {
        this.coordinatorNode = coordinatorNode;
    }


    public Set<UUID> getAllNodes()
    {
        Set<UUID> allNodes = new HashSet<>();
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


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    public Set<UUID> getHadoopNodes()
    {
        return hadoopNodes;
    }


    public void setHadoopNodes( final Set<UUID> hadoopNodes )
    {
        this.hadoopNodes = hadoopNodes;
    }
}
