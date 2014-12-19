package org.safehaus.subutai.common.protocol;


import java.util.Objects;

import org.safehaus.subutai.common.settings.Common;


/**
 * Node Group class
 */
public class NodeGroup
{

    private String name;
    private int numberOfNodes;
    private String templateName;
    private PlacementStrategy placementStrategy;
    private boolean linkHosts;
    private boolean exchangeSshKeys;
    private String domainName = Common.DEFAULT_DOMAIN_NAME;


    public String getDomainName()
    {
        return domainName;
    }


    public void setDomainName( final String domainName )
    {
        this.domainName = domainName;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public int getNumberOfNodes()
    {
        return numberOfNodes;
    }


    public void setNumberOfNodes( final int numberOfNodes )
    {
        this.numberOfNodes = numberOfNodes;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public PlacementStrategy getPlacementStrategy()
    {
        return placementStrategy;
    }


    public void setPlacementStrategy( final PlacementStrategy placementStrategy )
    {
        this.placementStrategy = placementStrategy;
    }


    public boolean isLinkHosts()
    {
        return linkHosts;
    }


    public void setLinkHosts( final boolean linkHosts )
    {
        this.linkHosts = linkHosts;
    }


    public boolean isExchangeSshKeys()
    {
        return exchangeSshKeys;
    }


    public void setExchangeSshKeys( final boolean exchangeSshKeys )
    {
        this.exchangeSshKeys = exchangeSshKeys;
    }


    @Override
    public String toString()
    {
        return "NodeGroup{"
                + "name='" + name + '\''
                + ", numberOfNodes=" + numberOfNodes
                + ", templateName='" + templateName + '\''
                + ", placementStrategy=" + placementStrategy
                + ", linkHosts=" + linkHosts
                + ", exchangeSshKeys=" + exchangeSshKeys
                + ", domainName='" + domainName + '\''
                + '}';
    }


    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode( this.name );
        return hash;
    }


    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof NodeGroup )
        {
            NodeGroup other = ( NodeGroup ) obj;
            return Objects.equals( this.name, other.name );
        }
        return false;
    }

}

