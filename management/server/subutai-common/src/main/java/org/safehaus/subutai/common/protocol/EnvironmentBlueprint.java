package org.safehaus.subutai.common.protocol;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.xml.bind.annotation.XmlRootElement;

import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.UUIDUtil;


/**
 * Environment Blueprint class
 */
@Entity( name = "EnvironmentBlueprint" )
@NamedQueries( {
        @NamedQuery( name = "EnvironmentBlueprint.getAll", query = "SELECT eb FROM EnvironmentBlueprint eb" )
} )
@XmlRootElement( name = "" )
public class EnvironmentBlueprint
{

    public static final String QUERY_GET_ALL = "EnvironmentBlueprint.getAll";

    private Set<NodeGroup> nodeGroups;
    private String name;
    private String domainName = Common.DEFAULT_DOMAIN_NAME;
    private boolean linkHosts;
    private boolean exchangeSshKeys;
    private UUID id;


    public EnvironmentBlueprint()
    {
        this.id = UUIDUtil.generateTimeBasedUUID();
        this.nodeGroups = new HashSet<>();
    }


    public UUID getId()
    {
        return id;
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
