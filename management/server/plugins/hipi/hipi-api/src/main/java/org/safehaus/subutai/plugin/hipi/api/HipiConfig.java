package org.safehaus.subutai.plugin.hipi.api;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;


public class HipiConfig implements ConfigBase
{
    public static final String TEMPLATE_NAME = "hadoophipi";
    public static final String PRODUCT_KEY = "Hipi";
    public static final String PRODUCT_PACKAGE = ( Common.PACKAGE_PREFIX + PRODUCT_KEY ).toLowerCase();

    private String clusterName;
    private SetupType setupType;
    private UUID environmentId;
    private Set<UUID> nodes = new HashSet<>();
    private String hadoopClusterName;
    private Set<UUID> hadoopNodes = new HashSet<>();


    public String getClusterName()
    {
        return clusterName;
    }


    public HipiConfig setClusterName( String clusterName )
    {
        this.clusterName = clusterName;
        return this;
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
