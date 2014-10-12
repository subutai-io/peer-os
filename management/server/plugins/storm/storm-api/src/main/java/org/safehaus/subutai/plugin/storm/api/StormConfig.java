package org.safehaus.subutai.plugin.storm.api;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;


public class StormConfig implements ConfigBase
{

    public static final String PRODUCT_NAME = "Storm2";
    public static final String TEMPLATE_NAME_NIMBUS = "stormnimbus";
    public static final String TEMPLATE_NAME_WORKER = "storm";

    private String clusterName;
    private int supervisorsCount;
    private boolean externalZookeeper;
    private String zookeeperClusterName;
    private Agent nimbus; // master node
    private Set<Agent> supervisors = new HashSet(); // worker nodes


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
    public String getProductKey() {
        return PRODUCT_NAME;
    }


    public void setClusterName( String clusterName )
    {
        this.clusterName = clusterName;
    }


    public Agent getNimbus()
    {
        return nimbus;
    }


    public void setNimbus( Agent nimbus )
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


    public Set<Agent> getSupervisors()
    {
        return supervisors;
    }


    public void setSupervisors( Set<Agent> supervisors )
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
        if ( obj instanceof StormConfig )
        {
            StormConfig other = ( StormConfig ) obj;
            return clusterName.equals( other.clusterName );
        }
        return false;
    }
}
