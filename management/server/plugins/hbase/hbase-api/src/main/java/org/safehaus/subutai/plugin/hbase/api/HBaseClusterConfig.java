package org.safehaus.subutai.plugin.hbase.api;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
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
    private Agent hbaseMaster;
    private Set<Agent> hadoopNodes = Sets.newHashSet();
    private Set<Agent> regionServers = Sets.newHashSet();
    private Set<Agent> quorumPeers = Sets.newHashSet();
    private Set<Agent> backupMasters = Sets.newHashSet();
    private Set<Agent> allNodes = Sets.newHashSet();
    private String domainName = Common.DEFAULT_DOMAIN_NAME;
    private SetupType setupType;


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


    public Set<Agent> getHadoopNodes()
    {
        return hadoopNodes;
    }


    public void setHadoopNodes( Set<Agent> hadoopNodes )
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


    public Set<Agent> getAllNodes()
    {
        final Set<Agent> allNodes = new HashSet<>();

        allNodes.add( getHbaseMaster() );

        for ( Agent agent : getRegionServers() )
        {
            allNodes.add( agent );
        }

        for ( Agent agent : getQuorumPeers() )
        {
            allNodes.add( agent );
        }

        for ( Agent agent : getBackupMasters() )
        {
            allNodes.add( agent );
        }
        return allNodes;
    }


    public Agent getHbaseMaster()
    {
        return hbaseMaster;
    }


    public void setHbaseMaster( Agent hbaseMaster )
    {
        this.hbaseMaster = hbaseMaster;
    }


    public Set<Agent> getRegionServers()
    {
        return regionServers;
    }


    public void setRegionServers( Set<Agent> regionServers )
    {
        this.regionServers = regionServers;
    }


    public Set<Agent> getQuorumPeers()
    {
        return quorumPeers;
    }


    public void setQuorumPeers( Set<Agent> quorumPeers )
    {
        this.quorumPeers = quorumPeers;
    }


    public Set<Agent> getBackupMasters()
    {
        return backupMasters;
    }


    public void setBackupMasters( Set<Agent> backupMasters )
    {
        this.backupMasters = backupMasters;
    }
}
