package org.safehaus.subutai.plugin.pig.api;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;


public class PigConfig implements ConfigBase
{
    public static final String TEMPLATE_NAME = "hadooppig";
    public static final String PRODUCT_KEY = "Pig";
    public static final String PRODUCT_PACKAGE = ( Common.PACKAGE_PREFIX + PRODUCT_KEY ).toLowerCase();

    private String clusterName = "";
    private SetupType setupType;
    private String hadoopClusterName;
    private Set<UUID> nodes = new HashSet<>();
    private Set<UUID> hadoopNodes = new HashSet<>();
    private Hadoop hadoopManager;
    private UUID environmentId;


    public String getClusterName()
    {
        return clusterName;
    }


    public PigConfig setClusterName( String clusterName )
    {
        this.clusterName = clusterName;
        return this;
    }

    public void setHadoopManager(Hadoop hadoopManager)
    {
        this.hadoopManager = hadoopManager;
    }
    public HadoopClusterConfig getHadoopConfig()
    {
        return hadoopManager.getCluster( hadoopClusterName );
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


    public Set<UUID> getNodes()
    {
        return nodes;
    }


    public void setNodes( Set<UUID> nodes )
    {
        this.nodes = nodes;
    }


    @Override
    public String toString()
    {
        return "Config{" + "clusterName=" + clusterName + ", nodes=" + nodes + '}';
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
        this.clusterName = hadoopClusterName;
    }

    public Set<UUID> getHadoopNodes()
    {
        return hadoopNodes;
    }


    public void setHadoopNodes( final Set<UUID> hadoopNodes )
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
}
