package org.safehaus.subutai.plugin.sqoop.api;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.ConfigBase;


public class SqoopConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Sqoop";
    public static final String TEMPLATE_NAME = "hadoopsqoop";

    private String clusterName = "";
    private SetupType setupType;
    private UUID environmentId;
    private int nodesCount;
    private Set<UUID> nodes = new HashSet();
    private String hadoopClusterName = "";
    private Set<UUID> hadoopNodes = new HashSet<>();


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


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( UUID environmenId )
    {
        this.environmentId = environmenId;
    }


    public int getNodesCount()
    {
        return nodesCount;
    }


    public void setNodesCount( int nodesCount )
    {
        this.nodesCount = nodesCount;
    }


    public Set<UUID> getNodes()
    {
        return nodes;
    }


    public void setNodes( Set<UUID> nodeIds )
    {
        this.nodes = nodeIds;
    }


    public String getHadoopClusterName()
    {
        return hadoopClusterName;
    }


    public void setHadoopClusterName( String hadoopClusterName )
    {
        this.hadoopClusterName = hadoopClusterName;
    }


    public Set<UUID> getHadoopNodes()
    {
        return hadoopNodes;
    }


    public void setHadoopNodes( Set<UUID> hadoopNodes )
    {
        this.hadoopNodes = hadoopNodes;
    }


    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode( this.clusterName );
        return hash;
    }


    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof SqoopConfig )
        {
            SqoopConfig other = ( SqoopConfig ) obj;
            return Objects.equals( this.clusterName, other.clusterName );
        }
        return false;
    }


    @Override
    public String toString()
    {
        return "Config{" + "clusterName=" + clusterName + ", nodes=" + ( nodes != null ? nodes.size() : 0 ) + '}';
    }
}

