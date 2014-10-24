package org.safehaus.subutai.plugin.jetty.api;


import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


public class JettyConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Jetty";
    public static final String PRODUCT_NAME = "Jetty";
    private String templateName = PRODUCT_NAME.toLowerCase();
    private String clusterName = "";
    private String domainName = Common.DEFAULT_DOMAIN_NAME;
    private String baseDirectory = "/var/web/base";
    private int port = 8080;
    private int numberOfNodes;
    private Set<Agent> nodes;


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
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


    public String getDomainName()
    {
        return domainName;
    }


    public void setDomainName( String domainName )
    {
        this.domainName = domainName;
    }


    public int getNumberOfNodes()
    {
        return numberOfNodes;
    }


    public void setNumberOfNodes( int numberOfNodes )
    {
        this.numberOfNodes = numberOfNodes;
    }


    public String getBaseDirectory()
    {
        return baseDirectory;
    }


    public void setBaseDirectory( final String baseDirectory )
    {
        this.baseDirectory = baseDirectory;
    }


    public int getPort()
    {
        return port;
    }


    public void setPort( final int port )
    {
        this.port = port;
    }


    @Override
    public String toString()
    {
        return new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE ).append( "clusterName", clusterName )
                                                                            .append( "domainName", domainName )
                                                                            .append( "numberOfNodes", numberOfNodes )
                                                                            .toString();
    }
}
