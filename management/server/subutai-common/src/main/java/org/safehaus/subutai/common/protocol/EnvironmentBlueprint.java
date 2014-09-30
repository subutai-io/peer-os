package org.safehaus.subutai.common.protocol;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.settings.Common;


/**
 * Environment Blueprint class
 */
public class EnvironmentBlueprint
{

    private Set<NodeGroup> nodeGroups;
    private String name;
    private String domainName = Common.DEFAULT_DOMAIN_NAME;
    private boolean linkHosts;
    private boolean exchangeSshKeys;


    public EnvironmentBlueprint()
    {
        this.nodeGroups = new HashSet<>();
    }


    public EnvironmentBlueprint( final String name, final String domainName, final boolean linkHosts,
                                 final boolean exchangeSshKeys )
    {
        this.name = name;
        this.domainName = domainName;
        this.linkHosts = linkHosts;
        this.exchangeSshKeys = exchangeSshKeys;
        this.nodeGroups = new HashSet<>();
    }


    public String getDomainName()
    {
        return domainName;
    }


    public void setDomainName( final String domainName )
    {
        this.domainName = domainName;
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


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public Set<NodeGroup> getNodeGroups()
    {
        return nodeGroups;
    }


    public void setNodeGroups( final Set<NodeGroup> nodeGroups )
    {
        this.nodeGroups = nodeGroups;
    }


    @Override
    public String toString()
    {
        return "EnvironmentBlueprint{" +
                "name='" + name + '\'' +
                ", nodeGroups=" + nodeGroups +
                ", linkHosts=" + linkHosts +
                ", exchangeSshKeys=" + exchangeSshKeys +
                ", domainName='" + domainName + '\'' +
                '}';
    }


    public void addNodeGroup( final NodeGroup ng )
    {
        this.nodeGroups.add( ng );
    }
}
