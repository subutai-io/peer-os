package org.safehaus.subutai.plugin.hadoop.api;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeType;

import com.google.common.base.Preconditions;


public class HadoopClusterConfig implements ConfigBase
{
    public static final String PRODUCT_KEY = "Hadoop";
    public static final int DEFAULT_HADOOP_MASTER_NODES_QUANTITY = 3;
    public static final String PRODUCT_NAME = PRODUCT_KEY.toLowerCase();
    private String templateName = PRODUCT_NAME;
    public static final int NAME_NODE_PORT = 8020, JOB_TRACKER_PORT = 9000;

    private String clusterName, domainName;
    private UUID nameNode, jobTracker, secondaryNameNode;
    private List<UUID> dataNodes, taskTrackers;
    private Integer replicationFactor = 1, countOfSlaveNodes = 1;
    private Set<UUID> blockedAgents;
    private UUID environmentId;


    public HadoopClusterConfig()
    {
        domainName = Common.DEFAULT_DOMAIN_NAME;
        dataNodes = new ArrayList<>();
        taskTrackers = new ArrayList<>();
        blockedAgents = new HashSet<>();
    }


    public static List<NodeType> getNodeRoles( HadoopClusterConfig clusterConfig, final ContainerHost containerHost )
    {
        List<NodeType> nodeRoles = new ArrayList<>();

        if ( clusterConfig.isNameNode( containerHost.getAgent().getUuid() ) )
        {
            nodeRoles.add( NodeType.NAMENODE );
        }
        if ( clusterConfig.isSecondaryNameNode( containerHost.getAgent().getUuid() ) )
        {
            nodeRoles.add( NodeType.SECONDARY_NAMENODE );
        }
        if ( clusterConfig.isJobTracker( containerHost.getAgent().getUuid() ) )
        {
            nodeRoles.add( NodeType.JOBTRACKER );
        }
        if ( clusterConfig.isDataNode( containerHost.getAgent().getUuid() ) )
        {
            nodeRoles.add( NodeType.DATANODE );
        }
        if ( clusterConfig.isTaskTracker( containerHost.getAgent().getUuid() ) )
        {
            nodeRoles.add( NodeType.TASKTRACKER );
        }

        return nodeRoles;
    }


    public boolean isDataNode( UUID uuid )
    {
        return getAllDataNodeAgent().contains( uuid );
    }


    public Set<UUID> getAllDataNodeAgent()
    {
        Set<UUID> allAgents = new HashSet<>();
        for ( UUID uuid : getDataNodes() )
        {
            allAgents.add( uuid );
        }
        return allAgents;
    }


    public List<UUID> getDataNodes()
    {
        return dataNodes;
    }


    public void setDataNodes( List<UUID> dataNodes )
    {
        this.dataNodes = dataNodes;
    }


    public boolean isTaskTracker( UUID uuid )
    {
        return getAllTaskTrackerNodeAgents().contains( uuid );
    }


    public Set<UUID> getAllTaskTrackerNodeAgents()
    {
        Set<UUID> allAgents = new HashSet<>();
        for ( UUID uuid : getTaskTrackers() )
        {
            allAgents.add( uuid );
        }
        return allAgents;
    }


    public List<UUID> getTaskTrackers()
    {
        return taskTrackers;
    }


    public void setTaskTrackers( List<UUID> taskTrackers )
    {
        this.taskTrackers = taskTrackers;
    }


    public boolean isNameNode( UUID uuid )
    {
        return getNameNode().equals( uuid );
    }


    public UUID getNameNode()
    {
        return nameNode;
    }


    public void setNameNode( UUID nameNode )
    {
        this.nameNode = nameNode;
    }


    public boolean isJobTracker( UUID uuid )
    {
        return getJobTracker().equals( uuid );
    }


    public UUID getJobTracker()
    {
        return jobTracker;
    }


    public void setJobTracker( UUID jobTracker )
    {
        this.jobTracker = jobTracker;
    }


    public boolean isSecondaryNameNode( UUID uuid )
    {
        return getSecondaryNameNode().equals( uuid );
    }


    public UUID getSecondaryNameNode()
    {
        return secondaryNameNode;
    }


    public void setSecondaryNameNode( UUID secondaryNameNode )
    {
        this.secondaryNameNode = secondaryNameNode;
    }


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


    public List<UUID> getAllNodes()
    {
        Set<UUID> allAgents = new HashSet<>();
        if ( dataNodes != null )
        {
            allAgents.addAll( dataNodes );
        }
        if ( taskTrackers != null )
        {
            allAgents.addAll( taskTrackers );
        }

        if ( nameNode != null )
        {
            allAgents.add( nameNode );
        }
        if ( jobTracker != null )
        {
            allAgents.add( jobTracker );
        }
        if ( secondaryNameNode != null )
        {
            allAgents.add( secondaryNameNode );
        }

        return new ArrayList<>( allAgents );
    }


    public Set<UUID> getAllMasterNodesAgents()
    {
        Set<UUID> allAgents = new HashSet<>();
        for ( UUID uuid : getAllMasterNodes() )
        {
            allAgents.add( uuid );
        }
        return allAgents;
    }


    public Set<UUID> getAllMasterNodes()
    {
        Preconditions.checkNotNull( nameNode, "NameNode is null" );
        Preconditions.checkNotNull( jobTracker, "JobTracker is null" );
        Preconditions.checkNotNull( secondaryNameNode, "SecondaryNameNode is null" );
        Set<UUID> allMastersNodes = new HashSet<>();
        allMastersNodes.add( nameNode );
        allMastersNodes.add( jobTracker );
        allMastersNodes.add( secondaryNameNode );
        return allMastersNodes;
    }


    public Set<UUID> getAllSlaveNodesAgents()
    {
        Set<UUID> allAgents = new HashSet<>();
        for ( UUID uuid : getAllSlaveNodes() )
        {
            allAgents.add( uuid );
        }
        return allAgents;
    }


    public List<UUID> getAllSlaveNodes()
    {
        Set<UUID> allAgents = new HashSet<>();
        if ( dataNodes != null )
        {
            allAgents.addAll( dataNodes );
        }
        if ( taskTrackers != null )
        {
            allAgents.addAll( taskTrackers );
        }

        return new ArrayList<>( allAgents );
    }


    public void removeNode( ContainerHost agent )
    {
        if ( dataNodes.contains( agent ) )
        {
            dataNodes.remove( agent );
        }
        if ( taskTrackers.contains( agent ) )
        {
            taskTrackers.remove( agent );
        }
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
        return PRODUCT_NAME;
    }


    @Override
    public String getProductKey()
    {
        return PRODUCT_KEY;
    }


    public String getDomainName()
    {
        return domainName;
    }


    public void setDomainName( String domainName )
    {
        this.domainName = domainName;
    }


    public Integer getReplicationFactor()
    {
        return replicationFactor;
    }


    public void setReplicationFactor( Integer replicationFactor )
    {
        this.replicationFactor = replicationFactor;
    }


    public Integer getCountOfSlaveNodes()
    {
        return countOfSlaveNodes;
    }


    public void setCountOfSlaveNodes( Integer countOfSlaveNodes )
    {
        this.countOfSlaveNodes = countOfSlaveNodes;
    }


    public Set<UUID> getBlockedAgentUUIDs()
    {
        Set<UUID> blockedAgents = new HashSet<>();
        for ( UUID uuid : getBlockedAgents() )
        {
            blockedAgents.add( uuid );
        }
        return blockedAgents;
    }


    public Set<UUID> getBlockedAgents()
    {
        return blockedAgents;
    }


    public void setBlockedAgents( HashSet<UUID> blockedAgents )
    {
        this.blockedAgents = blockedAgents;
    }


    public boolean isMasterNode( ContainerHost containerHost )
    {
        return containerHost.getAgent().getUuid().equals( getNameNode() ) ||
                containerHost.getAgent().getUuid().equals( getJobTracker() ) ||
                containerHost.getAgent().getUuid().equals( getSecondaryNameNode() );
    }


    @Override
    public int hashCode()
    {
        return clusterName != null ? clusterName.hashCode() : 0;
    }


    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        HadoopClusterConfig hadoopClusterConfig = ( HadoopClusterConfig ) o;

        if ( clusterName != null ? !clusterName.equals( hadoopClusterConfig.clusterName ) :
             hadoopClusterConfig.clusterName != null )
        {
            return false;
        }

        return true;
    }


    @Override
    public String toString()
    {
        return "Config{" +
                "clusterName='" + clusterName + '\'' +
                ", domainName='" + domainName + '\'' +
                ", nameNode=" + nameNode +
                ", jobTracker=" + jobTracker +
                ", secondaryNameNode=" + secondaryNameNode +
                ", dataNodes=" + dataNodes +
                ", taskTrackers=" + taskTrackers +
                ", replicationFactor=" + replicationFactor +
                ", countOfSlaveNodes=" + countOfSlaveNodes +
                '}';
    }
}
