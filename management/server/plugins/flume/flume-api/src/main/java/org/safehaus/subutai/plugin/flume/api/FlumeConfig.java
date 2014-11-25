package org.safehaus.subutai.plugin.flume.api;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.ConfigBase;


public class FlumeConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Flume";
    public static final String TEMPLATE_NAME = "hadoopflume";

    private String clusterName = "";
    private SetupType setupType;
    private String hadoopClusterName;
    private Set<UUID> nodes = new HashSet();
    private Set<UUID> hadoopNodes = new HashSet<>();
    private UUID environmentId;


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


    public String getHadoopClusterName()
    {
        return hadoopClusterName;
    }


    public void setHadoopClusterName( String hadoopClusterName )
    {
        this.hadoopClusterName = hadoopClusterName;
    }


    public Set<UUID> getNodes()
    {
        return nodes;
    }


    public void setNodes( Set<UUID> nodes )
    {
        this.nodes = nodes;
    }


    public Set<UUID> getHadoopNodes()
    {
        return hadoopNodes;
    }


    public void setHadoopNodes( Set<UUID> hadoopNodes )
    {
        this.hadoopNodes = hadoopNodes;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    @Override
    public String toString()
    {
        int c = nodes != null ? nodes.size() : 0;
        return "Config{" + "clusterName=" + clusterName + ", hadoopClusterName=" + hadoopClusterName + ", nodes=" + c
                + '}';
    }
}
