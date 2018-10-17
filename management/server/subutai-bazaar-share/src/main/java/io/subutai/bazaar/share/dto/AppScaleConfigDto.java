package io.subutai.bazaar.share.dto;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * Copy from AppScale plugin
 */
public class AppScaleConfigDto
{
    private String clusterName = "";


    private String zookeeperName;
    private String cassandraName;
    private String appengine;

    private List<String> zooList = new ArrayList<>();
    private List<String> cassList = new ArrayList<>();
    private List<String> appenList = new ArrayList<>();

    private String domainName = "intra.lan";
    private List<String> nodes = new ArrayList<>();
    private String environmentId;
    private String containerType;
    private String tracker;

    private List<AppScaleConfigDto> clusters;

    private List<String> clusterNames;
    private String userDomain;
    private int vlanNumber;
    private String scaleOption;

    private TunnelInfoDto tunnelInfoDto;

    // <containerName, containerIP>

    private HashMap<String, String> containerAddresses = new HashMap<>();

    private String state = "";


    public Map<String, String> getContainerAddresses()
    {
        return containerAddresses;
    }


    public String getClusterName()
    {
        return this.clusterName;
    }


    public void setClusterName( String clusterName )
    {
        this.clusterName = clusterName;
    }


    public List<String> getNodes()
    {
        return nodes;
    }


    public void setNodes( List<String> nodes )
    {
        this.nodes = nodes;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( String environmentId )
    {
        this.environmentId = environmentId;
    }


    public String getContainerType()
    {
        return containerType;
    }


    public void setContainerType( String containerType )
    {
        this.containerType = containerType;
    }


    public String getTracker()
    {
        return tracker;
    }


    public void setTracker( String tracker )
    {
        this.tracker = tracker;
    }


    public String getZookeeperName()
    {
        return zookeeperName;
    }


    public void setZookeeperName( String zookeeperName )
    {
        this.zookeeperName = zookeeperName;
    }


    public String getCassandraName()
    {
        return cassandraName;
    }


    public void setCassandraName( String cassandraName )
    {
        this.cassandraName = cassandraName;
    }


    public List<AppScaleConfigDto> getClusters()
    {
        return clusters;
    }


    public void setClusters( List<AppScaleConfigDto> clusters )
    {
        this.clusters = clusters;
    }


    public List<String> getclusterNames()
    {
        return clusterNames;
    }


    public void setclusterNames( List<String> clusterNames )
    {
        this.clusterNames = clusterNames;
    }


    public String getUserDomain()
    {
        return userDomain;
    }


    public void setUserDomain( String userDomain )
    {
        this.userDomain = userDomain;
    }


    public Integer getVlanNumber()
    {
        return vlanNumber;
    }


    public void setVlanNumber( Integer vlanNumber )
    {
        this.vlanNumber = vlanNumber;
    }


    public String getAppengine()
    {
        return appengine;
    }


    public void setAppengine( String appengine )
    {
        this.appengine = appengine;
    }


    public List<String> getZooList()
    {
        return zooList;
    }


    public void setZooList( List<String> zooList )
    {
        this.zooList = zooList;
    }


    public List<String> getCassList()
    {
        return cassList;
    }


    public void setCassList( List<String> cassList )
    {
        this.cassList = cassList;
    }


    public List<String> getAppenList()
    {
        return appenList;
    }


    public void setAppenList( List<String> appenList )
    {
        this.appenList = appenList;
    }


    public String getScaleOption()
    {
        return scaleOption;
    }


    public void setScaleOption( String scaleOption )
    {
        this.scaleOption = scaleOption;
    }


    public String getDomainName()
    {
        return domainName;
    }


    public void setDomainName( String domainName )
    {
        this.domainName = domainName;
    }


    public String getState()
    {
        return state;
    }


    public void setState( String state )
    {
        this.state = state;
    }


    public TunnelInfoDto getTunnelInfoDto()
    {
        return tunnelInfoDto;
    }


    public void setTunnelInfoDto( final TunnelInfoDto tunnelInfoDto )
    {
        this.tunnelInfoDto = tunnelInfoDto;
    }


    @Override
    public String toString()
    {
        return new ToStringBuilder( this, ToStringStyle.SHORT_PREFIX_STYLE ).append( "clusterName", clusterName )
                                                                            .append( "userDomain", userDomain )
                                                                            .append( "appenList", appenList )
                                                                            .append( "zooList", zooList )
                                                                            .append( "cassList", cassList )
                                                                            .append( "containerAddresses",
                                                                                    containerAddresses ).toString();
    }
}

