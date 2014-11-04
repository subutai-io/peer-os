package org.safehaus.subutai.plugin.elasticsearch.api;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.protocol.PlacementStrategy;


public class ElasticsearchClusterConfiguration implements ConfigBase
{

    public static final String PRODUCT_KEY = "Elasticsearch";
    public static final String PRODUCT_NAME = "elasticsearch";

    public static final String templateName = "elastic";

    private String clusterName = "";
    private int numberOfNodes;
    private int numberOfMasterNodes;
    private int numberOfDataNodes;
    private int numberOfShards = 5;
    private int numberOfReplicas = 5;

    private Set<Agent> nodes = new HashSet<>();
    private Set<Agent> masterNodes = new HashSet<>();
    private Set<Agent> dataNodes = new HashSet<>();


    public static String getTemplateName()
    {
        return templateName;
    }


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


    public int getNumberOfNodes()
    {
        return numberOfNodes;
    }


    public void setNumberOfNodes( int numberOfNodes )
    {
        this.numberOfNodes = numberOfNodes;
    }


    public int getNumberOfShards()
    {
        return numberOfShards;
    }


    public void setNumberOfShards( final int numberOfShards )
    {
        this.numberOfShards = numberOfShards;
    }


    public int getNumberOfReplicas()
    {
        return numberOfReplicas;
    }


    public void setNumberOfReplicas( final int numberOfReplicas )
    {
        this.numberOfReplicas = numberOfReplicas;
    }


    public int getNumberOfMasterNodes()
    {
        return numberOfMasterNodes;
    }


    public void setNumberOfMasterNodes( int numberOfSeeds )
    {
        this.numberOfMasterNodes = numberOfSeeds;
    }


    public int getNumberOfDataNodes()
    {
        return numberOfDataNodes;
    }


    public void setNumberOfDataNodes( int numberOfDataNodes )
    {
        this.numberOfDataNodes = numberOfDataNodes;
    }


    public Set<Agent> getNodes()
    {
        return nodes;
    }


    public void setNodes( Set<Agent> nodes )
    {
        this.nodes = nodes;
    }


    public Set<Agent> getMasterNodes()
    {
        return masterNodes;
    }


    public void setMasterNodes( Set<Agent> nodes )
    {
        this.masterNodes = nodes;
    }


    public Set<Agent> getDataNodes()
    {
        return dataNodes;
    }


    public void setDataNodes( Set<Agent> nodes )
    {
        this.dataNodes = nodes;
    }


    public static PlacementStrategy getNodePlacementStrategy()
    {
        return PlacementStrategy.ROUND_ROBIN;
    }
}
