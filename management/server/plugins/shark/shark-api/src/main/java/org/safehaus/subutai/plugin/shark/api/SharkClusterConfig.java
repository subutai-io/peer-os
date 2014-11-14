package org.safehaus.subutai.plugin.shark.api;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.ConfigBase;

import com.google.common.collect.Sets;


public class SharkClusterConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Shark";
    public static final String TEMPLATE_NAME = "shark";

    private SetupType setupType;
    private String clusterName = "";
    private String sparkClusterName = "";
    private String hadoopClusterName = "";
    private Set<UUID> nodeIds = Sets.newHashSet();
    private UUID environmentId;


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    public SetupType getSetupType()
    {
        return setupType;
    }


    public void setSetupType( SetupType setupType )
    {
        this.setupType = setupType;
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


    public Set<UUID> getNodeIds()
    {
        return nodeIds;
    }


    public String getSparkClusterName()
    {
        return sparkClusterName;
    }


    public void setSparkClusterName( String sparkClusterName )
    {
        this.sparkClusterName = sparkClusterName;
    }


    public String getHadoopClusterName()
    {
        return hadoopClusterName;
    }


    public void setHadoopClusterName( String hadoopClusterName )
    {
        this.hadoopClusterName = hadoopClusterName;
    }


    @Override
    public String toString()
    {
        return "Config{" + "clusterName=" + clusterName + ", nodeIds=" + nodeIds + '}';
    }
}

