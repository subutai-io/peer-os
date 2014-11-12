/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.api;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.ConfigBase;


public class AccumuloClusterConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Accumulo";
    public static final String PRODUCT_NAME = "accumulo";
    private String clusterName = "";
    private String instanceName = "";
    private String password = "";
    private UUID masterNode;
    private UUID gcNode;
    private UUID monitor;
    private Set<UUID> tracers;
    private Set<UUID> slaves;
    private int numberOfTracers = 1;
    private int numberOfSlaves = 3;
    private SetupType setupType;
    private String hadoopClusterName;
    private String zookeeperClusterName;
    private String templateName;
    private UUID environmentId;


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public String getHadoopClusterName()
    {
        return hadoopClusterName;
    }


    public void setHadoopClusterName( final String hadoopClusterName )
    {
        this.hadoopClusterName = hadoopClusterName;
    }


    public String getZookeeperClusterName()
    {
        return zookeeperClusterName;
    }


    public void setZookeeperClusterName( final String zookeeperClusterName )
    {
        this.zookeeperClusterName = zookeeperClusterName;
    }


    public SetupType getSetupType()
    {
        return setupType;
    }


    public void setSetupType( final SetupType setupType )
    {
        this.setupType = setupType;
    }


    public int getNumberOfTracers()
    {
        return numberOfTracers;
    }


    public void setNumberOfTracers( final int numberOfTracers )
    {
        this.numberOfTracers = numberOfTracers;
    }


    public int getNumberOfSlaves()
    {
        return numberOfSlaves;
    }


    public void setNumberOfSlaves( final int numberOfSlaves )
    {
        this.numberOfSlaves = numberOfSlaves;
    }


    public Set<UUID> getAllNodes()
    {
        Set<UUID> allNodes = new HashSet<>();

        if ( masterNode != null )
        {
            allNodes.add( masterNode );
        }
        if ( gcNode != null )
        {
            allNodes.add( gcNode );
        }
        if ( monitor != null )
        {
            allNodes.add( monitor );
        }
        if ( tracers != null )
        {
            allNodes.addAll( tracers );
        }
        if ( slaves != null )
        {
            allNodes.addAll( slaves );
        }

        return allNodes;
    }


    public UUID getMasterNode()
    {
        return masterNode;
    }


    public void setMasterNode( UUID masterNode )
    {
        this.masterNode = masterNode;
    }


    public UUID getGcNode()
    {
        return gcNode;
    }


    public void setGcNode( UUID gcNode )
    {
        this.gcNode = gcNode;
    }


    public UUID getMonitor()
    {
        return monitor;
    }


    public void setMonitor( UUID monitor )
    {
        this.monitor = monitor;
    }


    public Set<UUID> getTracers()
    {
        return tracers;
    }


    public void setTracers( Set<UUID> tracers )
    {
        this.tracers = tracers;
    }


    public Set<UUID> getSlaves()
    {
        return slaves;
    }


    public void setSlaves( Set<UUID> slaves )
    {
        this.slaves = slaves;
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


    public String getInstanceName()
    {
        return instanceName;
    }


    public void setInstanceName( String instanceName )
    {
        this.instanceName = instanceName;
    }


    public String getPassword()
    {
        return password;
    }


    public void setPassword( String password )
    {
        this.password = password;
    }


    @Override
    public String toString()
    {
        return "AccumuloClusterConfig{" +
                "clusterName='" + clusterName + '\'' +
                ", instanceName='" + instanceName + '\'' +
                ", password='" + password + '\'' +
                ", masterNode=" + masterNode +
                ", gcNode=" + gcNode +
                ", monitor=" + monitor +
                ", tracers=" + tracers +
                ", slaves=" + slaves +
                ", numberOfTracers=" + numberOfTracers +
                ", numberOfSlaves=" + numberOfSlaves +
                ", setupType=" + setupType +
                ", hadoopClusterName='" + hadoopClusterName + '\'' +
                ", zookeeperClusterName='" + zookeeperClusterName + '\'' +
                ", templateName='" + templateName + '\'' +
                '}';
    }
}
