package org.safehaus.subutai.plugin.hipi.api;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;


public class HipiConfig implements ConfigBase
{
    public static final String TEMPLATE_NAME = "hadoophipi";
    public static final String PRODUCT_KEY = "Hipi";
    public static final String PRODUCT_PACKAGE = ( Common.PACKAGE_PREFIX + PRODUCT_KEY ).toLowerCase();

    private String clusterName = "";
    private SetupType setupType;
    private String hadoopClusterName;
    private Set<Agent> nodes = new HashSet<>();
    private Set<Agent> hadoopNodes = new HashSet<>();


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


    public Set<Agent> getHadoopNodes()
    {
        return hadoopNodes;
    }


    public void setHadoopNodes( final Set<Agent> hadoopNodes )
    {
        this.hadoopNodes = hadoopNodes;
    }
}
