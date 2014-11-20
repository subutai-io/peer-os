package org.safehaus.subutai.plugin.cassandra.api;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;


public class CassandraClusterConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Cassandra";
    public static final String PRODUCT_NAME = "Cassandra";
    private String templateName = "cassandra";
    private String clusterName = "";
    private String domainName = Common.DEFAULT_DOMAIN_NAME;
    private int numberOfSeeds;
    private int numberOfNodes;
    private Set<UUID> seedNodes;
    private Set<UUID> nodes;
    private String dataDirectory = "/var/lib/cassandra/data";
    private String commitLogDirectory = "/var/lib/cassandra/commitlog";
    private String savedCachesDirectory = "/var/lib/cassandra/saved_caches";
    private UUID environmentId;


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


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


    public String getDataDirectory()
    {
        return dataDirectory;
    }


    public void setDataDirectory( String dataDirectory )
    {
        this.dataDirectory = dataDirectory;
    }


    public String getCommitLogDirectory()
    {
        return commitLogDirectory;
    }


    public void setCommitLogDirectory( String commitLogDirectory )
    {
        this.commitLogDirectory = commitLogDirectory;
    }


    public String getSavedCachesDirectory()
    {
        return savedCachesDirectory;
    }


    public void setSavedCachesDirectory( String savedCachesDirectory )
    {
        this.savedCachesDirectory = savedCachesDirectory;
    }


    public int getNumberOfSeeds()
    {
        return numberOfSeeds;
    }


    public void setNumberOfSeeds( int numberOfSeeds )
    {
        this.numberOfSeeds = numberOfSeeds;
    }


    public Set<UUID> getSeedNodes()
    {
        return seedNodes;
    }


    public void setSeedNodes( Set<UUID> seedNodes )
    {
        this.seedNodes = seedNodes;
    }


    public Set<UUID> getNodes()
    {
        return nodes;
    }


    public void setNodes( Set<UUID> nodes )
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


    @Override
    public String toString()
    {
        return "Config{" +
                "clusterName='" + clusterName + '\'' +
                ", domainName='" + domainName + '\'' +
                ", numberOfSeeds=" + numberOfSeeds +
                ", numberOfNodes=" + numberOfNodes +
                ", dataDirectory='" + dataDirectory + '\'' +
                ", commitLogDirectory='" + commitLogDirectory + '\'' +
                ", savedCachesDirectory='" + savedCachesDirectory + '\'' +
                '}';
    }
}
