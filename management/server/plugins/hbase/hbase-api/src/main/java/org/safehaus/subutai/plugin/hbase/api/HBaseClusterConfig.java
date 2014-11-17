package org.safehaus.subutai.plugin.hbase.api;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.UUIDUtil;

import com.google.common.collect.Sets;


public class HBaseClusterConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "HBase";
    public static final String PRODUCT_NAME = "HBase";
    private String templateName = PRODUCT_NAME;
    private int numberOfNodes = 4;
    private UUID uuid;


    private String clusterName = "";
    private String hadoopNameNode;

    private String hadoopClusterName;
    private UUID hbaseMaster;
    private Set<UUID> hadoopNodes = Sets.newHashSet();
    private Set<UUID> regionServers = Sets.newHashSet();
    private Set<UUID> quorumPeers = Sets.newHashSet();
    private Set<UUID> backupMasters = Sets.newHashSet();
    private Set<UUID> allNodes = Sets.newHashSet();
    private String domainName = Common.DEFAULT_DOMAIN_NAME;
    private SetupType setupType;
    private UUID environmentId;


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    public HBaseClusterConfig()
    {
        this.uuid = UUID.fromString( UUIDUtil.generateTimeBasedUUID().toString() );
    }


    public SetupType getSetupType()
    {
        return setupType;
    }


    public void setSetupType( final SetupType setupType )
    {
        this.setupType = setupType;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public String getDomainName()
    {
        return domainName;
    }


    public void setDomainName( final String domainName )
    {
        this.domainName = domainName;
    }


    public String getHadoopNameNode()
    {
        return hadoopNameNode;
    }


    public void setHadoopNameNode( String hadoopNameNode )
    {
        this.hadoopNameNode = hadoopNameNode;
    }


    public String getHadoopClusterName()
    {
        return hadoopClusterName;
    }


    public void setHadoopClusterName( String hadoopClusterName )
    {
        this.hadoopClusterName = hadoopClusterName;
    }


    public UUID getUuid()
    {
        return uuid;
    }


    public void setUuid( UUID uuid )
    {
        this.uuid = uuid;
    }


    public void reset()
    {
        this.hbaseMaster = null;
        this.regionServers = null;
        this.quorumPeers = null;
        this.backupMasters = null;
        this.domainName = "";
        this.clusterName = "";
    }


    public Set<UUID> getHadoopNodes()
    {
        return hadoopNodes;
    }


    public void setHadoopNodes( Set<UUID> hadoopNodes )
    {
        this.hadoopNodes = hadoopNodes;
    }


    public int getNumberOfNodes()
    {
        return numberOfNodes;
    }


    public void setNumberOfNodes( int numberOfNodes )
    {
        this.numberOfNodes = numberOfNodes;
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


    @Override
    public String toString()
    {
        return "HBaseConfig{" +
                "numberOfNodes=" + numberOfNodes +
                ", uuid=" + uuid +
                ", hbaseMaster='" + hbaseMaster + '\'' +
                ", regionServers=" + regionServers +
                ", quorumPeers=" + quorumPeers +
                ", backupMasters='" + backupMasters + '\'' +
                ", domainName='" + domainName + '\'' +
                ", hadoopNodes=" + hadoopNodes +
                ", clusterName='" + clusterName + '\'' +
                ", hadoopNameNode='" + hadoopNameNode + '\'' +
                '}';
    }


    public Set<UUID> getAllNodes()
    {
        final Set<UUID> allNodes = new HashSet<>();

        allNodes.add( getHbaseMaster() );

        for ( UUID agent : getRegionServers() )
        {
            allNodes.add( agent );
        }

        for ( UUID agent : getQuorumPeers() )
        {
            allNodes.add( agent );
        }

        for ( UUID agent : getBackupMasters() )
        {
            allNodes.add( agent );
        }
        return allNodes;
    }


    public UUID getHbaseMaster()
    {
        return hbaseMaster;
    }


    public void setHbaseMaster( UUID hbaseMaster )
    {
        this.hbaseMaster = hbaseMaster;
    }


    public Set<UUID> getRegionServers()
    {
        return regionServers;
    }


    public void setRegionServers( Set<UUID> regionServers )
    {
        this.regionServers = regionServers;
    }


    public Set<UUID> getQuorumPeers()
    {
        return quorumPeers;
    }


    public void setQuorumPeers( Set<UUID> quorumPeers )
    {
        this.quorumPeers = quorumPeers;
    }


    public Set<UUID> getBackupMasters()
    {
        return backupMasters;
    }


    public void setBackupMasters( Set<UUID> backupMasters )
    {
        this.backupMasters = backupMasters;
    }
}
