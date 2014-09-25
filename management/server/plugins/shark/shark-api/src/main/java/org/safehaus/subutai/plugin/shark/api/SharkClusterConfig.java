package org.safehaus.subutai.plugin.shark.api;


import java.util.Set;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;


public class SharkClusterConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Shark";
    public static final String TEMPLATE_NAME = "shark";
    private String clusterName = "";

    private Set<Agent> nodes;


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


    public Set<Agent> getNodes()
    {
        return nodes;
    }


    public void setNodes( Set<Agent> nodes )
    {
        this.nodes = nodes;
    }


    @Override
    public String toString()
    {
        return "Config{" + "clusterName=" + clusterName + ", nodes=" + nodes + '}';
    }


}

