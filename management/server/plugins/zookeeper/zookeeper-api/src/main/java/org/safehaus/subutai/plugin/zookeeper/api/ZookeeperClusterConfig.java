/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.zookeeper.api;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;


/**
 * @author dilshat
 */
public class ZookeeperClusterConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Zookeeper2";
    public static final String PRODUCT_NAME = "zookeeper";
    private String templateName = PRODUCT_NAME;
    private String clusterName = "";
    private int numberOfNodes = 3;
    private Set<Agent> nodes;
    private SetupType setupType;
    private String hadoopClusterName;


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


    public int getNumberOfNodes()
    {
        return numberOfNodes;
    }


    public void setNumberOfNodes( int numberOfNodes )
    {
        this.numberOfNodes = numberOfNodes;
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
