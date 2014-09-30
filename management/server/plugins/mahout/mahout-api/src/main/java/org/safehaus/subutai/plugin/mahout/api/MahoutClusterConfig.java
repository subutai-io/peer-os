package org.safehaus.subutai.plugin.mahout.api;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;


public class MahoutClusterConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Mahout";
    public static final String PRODUCT_NAME = "Mahout";
    private String templateName = PRODUCT_NAME;
    private String clusterName = "";
    private SetupType setupType;
    private String hadoopClusterName;

    private Set<Agent> nodes = new HashSet();


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


    public Set<Agent> getNodes()
    {
        return nodes;
    }


    public void setNodes( Set<Agent> nodes )
    {
        this.nodes = nodes;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
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


    public void setSetupType( final SetupType setupType )
    {
        this.setupType = setupType;
    }


    public String getHadoopClusterName()
    {
        return hadoopClusterName;
    }


    public void setHadoopClusterName( final String hadoopClusterName )
    {
        this.hadoopClusterName = hadoopClusterName;
    }
}
