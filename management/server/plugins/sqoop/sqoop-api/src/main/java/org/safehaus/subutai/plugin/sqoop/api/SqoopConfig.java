package org.safehaus.subutai.plugin.sqoop.api;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;


public class SqoopConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Sqoop";
    public static final String TEMPLATE_NAME = "hadoopsqoop";

    private String clusterName = "";
    private SetupType setupType;
    private int nodesCount;
    private Set<Agent> nodes = new HashSet();
    private String hadoopClusterName = "";
    private Set<Agent> hadoopNodes = new HashSet<>();


    @Override
    public String getClusterName()
    {
        return clusterName;
    }


    public void setClusterName( String clusterName )
    {
        this.clusterName = clusterName;
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


    public SetupType getSetupType()
    {
        return setupType;
    }


    public void setSetupType( SetupType setupType )
    {
        this.setupType = setupType;
    }


    public int getNodesCount()
    {
        return nodesCount;
    }


    public void setNodesCount( int nodesCount )
    {
        this.nodesCount = nodesCount;
    }


    public Set<Agent> getNodes()
    {
        return nodes;
    }


    public void setNodes( Set<Agent> nodes )
    {
        this.nodes = nodes;
    }


    public String getHadoopClusterName()
    {
        return hadoopClusterName;
    }


    public void setHadoopClusterName( String hadoopClusterName )
    {
        this.hadoopClusterName = hadoopClusterName;
    }


    public Set<Agent> getHadoopNodes()
    {
        return hadoopNodes;
    }


    public void setHadoopNodes( Set<Agent> hadoopNodes )
    {
        this.hadoopNodes = hadoopNodes;
    }


    @Override
    public String toString()
    {
        return "Config{" + "clusterName=" + clusterName + ", nodes=" + ( nodes != null ? nodes.size() : 0 ) + '}';
    }
}
