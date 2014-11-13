package org.safehaus.subutai.plugin.elasticsearch.api;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.protocol.PlacementStrategy;


public class ElasticsearchClusterConfiguration implements ConfigBase
{

    public static final String PRODUCT_KEY = "Elasticsearch";
    public static final String PRODUCT_NAME = "elasticsearch";

    public static final String templateName = "elasticsearch";

    private String clusterName = "";
    private int numberOfNodes;
    private UUID environmentId;

    private Set<UUID> nodes = new HashSet<>();
    private Set<UUID> masterNodes = new HashSet<>();
    private Set<UUID> dataNodes = new HashSet<>();


    public static PlacementStrategy getNodePlacementStrategy()
    {
        return new PlacementStrategy( "ROUND_ROBIN" );
    }


    public String getTemplateName()
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


    public Set<UUID> getNodes()
    {
        return nodes;
    }


    public void setNodes( Set<UUID> nodes )
    {
        this.nodes = nodes;
    }


    public Set<UUID> getMasterNodes()
    {
        return masterNodes;
    }


    public void setMasterNodes( Set<UUID> nodes )
    {
        this.masterNodes = nodes;
    }


    public Set<UUID> getDataNodes()
    {
        return dataNodes;
    }


    public void setDataNodes( Set<UUID> nodes )
    {
        this.dataNodes = nodes;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }
}
