package org.safehaus.subutai.plugin.storm.api;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.ConfigBase;


public class StormClusterConfiguration implements ConfigBase
{

    public static final String PRODUCT_NAME = "Storm";
    public static final String TEMPLATE_NAME = "storm";

    private String clusterName;
    private int supervisorsCount;
    private boolean externalZookeeper;
    private String zookeeperClusterName;
    private UUID nimbus; // master node
    private Set<UUID> supervisors = new HashSet(); // worker nodes
    private UUID environmentId;


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }

    @Override
    public String getClusterName()
    {
        return clusterName;
    }


    @Override
    public String getProductName()
    {
        return PRODUCT_NAME;
    }


    @Override
    public String getProductKey()
    {
        return PRODUCT_NAME;
    }


    public void setClusterName( String clusterName )
    {
        this.clusterName = clusterName;
    }


    public UUID getNimbus()
    {
        return nimbus;
    }


    public void setNimbus( UUID nimbus )
    {
        this.nimbus = nimbus;
    }


    public int getSupervisorsCount()
    {
        return supervisorsCount;
    }


    public void setSupervisorsCount( int supervisorsCount )
    {
        this.supervisorsCount = supervisorsCount;
    }


    public Set<UUID> getSupervisors()
    {
        return supervisors;
    }


    public void setSupervisors( Set<UUID> supervisors )
    {
        this.supervisors = supervisors;
    }


    public boolean isExternalZookeeper()
    {
        return externalZookeeper;
    }


    public void setExternalZookeeper( boolean externalZookeeper )
    {
        this.externalZookeeper = externalZookeeper;
    }


    public String getZookeeperClusterName()
    {
        return zookeeperClusterName;
    }


    public void setZookeeperClusterName( String zookeeperClusterName )
    {
        this.zookeeperClusterName = zookeeperClusterName;
    }


    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode( this.clusterName );
        return hash;
    }


    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof StormClusterConfiguration )
        {
            StormClusterConfiguration other = ( StormClusterConfiguration ) obj;
            return clusterName.equals( other.clusterName );
        }
        return false;
    }
}
