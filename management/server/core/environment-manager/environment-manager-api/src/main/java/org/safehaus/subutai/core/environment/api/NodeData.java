package org.safehaus.subutai.core.environment.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;


public class NodeData
{
    String nodeGroupName;
    String templateName;
    PlacementStrategy placementStrategy;
    boolean linkHosts;
    boolean exchangeSshKeys;
    String domainName;
    UUID targetPeerId;
    String environmentName;
    String environmentDomainName;
    boolean environmentLinkHosts;
    boolean environmentExchangeSshKeys;


    public NodeData( final NodeGroup nodeGroup )
    {
        this.nodeGroupName = nodeGroup.getName();
        this.templateName = nodeGroup.getTemplateName();
        this.placementStrategy = nodeGroup.getPlacementStrategy();
        this.linkHosts = nodeGroup.isLinkHosts();
        this.exchangeSshKeys = nodeGroup.isExchangeSshKeys();
        this.domainName = nodeGroup.getDomainName();
    }


    public String getNodeGroupName()
    {
        return nodeGroupName;
    }


    public void setNodeGroupName( final String nodeGroupName )
    {
        this.nodeGroupName = nodeGroupName;
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


    public String getDomainName()
    {
        return domainName;
    }


    public void setDomainName( final String domainName )
    {
        this.domainName = domainName;
    }


    public UUID getTargetPeerId()
    {
        return targetPeerId;
    }


    public void setTargetPeerId( final UUID targetPeerId )
    {
        this.targetPeerId = targetPeerId;
    }


    public String getEnvironmentName()
    {
        return environmentName;
    }


    public void setEnvironmentName( final String environmentName )
    {
        this.environmentName = environmentName;
    }


    public String getEnvironmentDomainName()
    {
        return environmentDomainName;
    }


    public void setEnvironmentDomainName( final String environmentDomainName )
    {
        this.environmentDomainName = environmentDomainName;
    }


    public boolean isEnvironmentLinkHosts()
    {
        return environmentLinkHosts;
    }


    public void setEnvironmentLinkHosts( final boolean environmentLinkHosts )
    {
        this.environmentLinkHosts = environmentLinkHosts;
    }


    public boolean isEnvironmentExchangeSshKeys()
    {
        return environmentExchangeSshKeys;
    }


    public void setEnvironmentExchangeSshKeys( final boolean environmentExchangeSshKeys )
    {
        this.environmentExchangeSshKeys = environmentExchangeSshKeys;
    }


    @Override
    public String toString()
    {
        return "NodeData{" +
                "nodeGroupName='" + nodeGroupName + '\'' +
                ", templateName='" + templateName + '\'' +
                ", placementStrategy=" + placementStrategy +
                ", linkHosts=" + linkHosts +
                ", exchangeSshKeys=" + exchangeSshKeys +
                ", domainName='" + domainName + '\'' +
                ", targetPeerId=" + targetPeerId +
                ", environmentName='" + environmentName + '\'' +
                ", environmentDomainName='" + environmentDomainName + '\'' +
                ", environmentLinkHosts=" + environmentLinkHosts +
                ", environmentExchangeSshKeys=" + environmentExchangeSshKeys +
                '}';
    }
}
