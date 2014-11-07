/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.zookeeper.api;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.core.peer.api.ContainerHost;


public class ZookeeperClusterConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Zookeeper";
    public static final String PRODUCT_NAME = "zookeeper";
    private String templateName = PRODUCT_NAME;
    private String clusterName = "";
    private int numberOfNodes = 3;
    private Set<ContainerHost> nodes;
    private SetupType setupType;
    private String hadoopClusterName;
    private UUID environmentId;


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    public ZookeeperClusterConfig()
    {
        nodes = new HashSet<>();
    }


    public String getHadoopClusterName()
    {
        return hadoopClusterName;
    }


    public void setHadoopClusterName( final String hadoopClusterName )
    {
        this.hadoopClusterName = hadoopClusterName;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public SetupType getSetupType()
    {
        return setupType;
    }


    public void setSetupType( final SetupType setupType )
    {
        this.setupType = setupType;
    }


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
        return PRODUCT_NAME;
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


    public Set<ContainerHost> getNodes()
    {
        return nodes;
    }


    public void setNodes( Set<ContainerHost> nodes )
    {
        this.nodes = nodes;
    }


    @Override
    public String toString()
    {
        return "ZookeeperClusterConfig{" +
                "templateName='" + templateName + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", numberOfNodes=" + numberOfNodes +
                ", nodes=" + nodes +
                ", setupType=" + setupType +
                ", hadoopClusterName='" + hadoopClusterName + '\'' +
                '}';
    }
}
