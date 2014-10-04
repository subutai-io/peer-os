package org.safehaus.subutai.plugin.flume.api;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;


public class FlumeConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Flume";
    public static final String TEMPLATE_NAME = "hadoopflume";

    private String clusterName = "";
    private SetupType setupType;
    private String hadoopClusterName;
    private Set<Agent> nodes = new HashSet();
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


    public Set<Agent> getNodes()
    {
        return nodes;
    }


    public void setNodes( Set<Agent> nodes )
    {
        this.nodes = nodes;
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
        int c = nodes != null ? nodes.size() : 0;
        return "Config{" + "clusterName=" + clusterName + ", hadoopClusterName=" + hadoopClusterName + ", nodes=" + c
                + '}';
    }
}
