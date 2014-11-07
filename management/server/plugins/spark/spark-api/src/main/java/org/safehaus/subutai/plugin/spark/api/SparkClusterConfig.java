package org.safehaus.subutai.plugin.spark.api;


import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.util.CollectionUtil;

import com.google.common.collect.Lists;


public class SparkClusterConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Spark";
    public static final String TEMPLATE_NAME = "spark";

    private String clusterName = "";
    private String hadoopClusterName = "";
    private SetupType setupType;
    private UUID masterNodeId;
    private Set<UUID> slaveIds = new HashSet<>();
    // for with-Hadoop installation
    private Set<UUID> hadoopNodeIds = new HashSet<>();
    // for environment blueprint
    private int slaveNodesCount;
    private UUID environmentId;


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    public UUID getMasterNodeId()
    {
        return masterNodeId;
    }


    public Set<UUID> getSlaveIds()
    {
        return slaveIds;
    }


    public Set<UUID> getHadoopNodeIds()
    {
        return hadoopNodeIds;
    }


    public void setMasterNodeId( final UUID masterNodeId )
    {
        this.masterNodeId = masterNodeId;
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


    public String getHadoopClusterName()
    {
        return hadoopClusterName;
    }


    public void setHadoopClusterName( String hadoopClusterName )
    {
        this.hadoopClusterName = hadoopClusterName;
    }


    public SetupType getSetupType()
    {
        return setupType;
    }


    public void setSetupType( SetupType setupType )
    {
        this.setupType = setupType;
    }


    public int getSlaveNodesCount()
    {
        return slaveNodesCount;
    }


    public void setSlaveNodesCount( int slaveNodesCount )
    {
        this.slaveNodesCount = slaveNodesCount;
    }


    public List<UUID> getAllNodesIds()
    {
        List<UUID> allNodesIds = Lists.newArrayList();
        if ( !CollectionUtil.isCollectionEmpty( slaveIds ) )
        {
            allNodesIds.addAll( slaveIds );
        }
        if ( masterNodeId != null )
        {
            allNodesIds.add( masterNodeId );
        }

        return allNodesIds;
    }


    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode( this.clusterName );
        return hash;
    }


    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof SparkClusterConfig )
        {
            SparkClusterConfig other = ( SparkClusterConfig ) obj;
            return clusterName.equals( other.clusterName );
        }
        return false;
    }


    @Override
    public String toString()
    {
        return "Config{" + "clusterName=" + clusterName + ", masterNode=" + masterNodeId + ", slaves=" + slaveIds + '}';
    }
}

