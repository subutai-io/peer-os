package org.safehaus.subutai.plugin.hadoop.api;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.peer.api.ContainerHost;

import com.google.common.base.Preconditions;


public class HadoopClusterConfig implements ConfigBase
{
    public static final String PRODUCT_KEY = "Hadoop";
    public static final int DEFAULT_HADOOP_MASTER_NODES_QUANTITY = 3;
    public static final String PRODUCT_NAME = PRODUCT_KEY.toLowerCase();
    private String templateName = PRODUCT_NAME;
    public static final int NAME_NODE_PORT = 8020, JOB_TRACKER_PORT = 9000;

    private String clusterName, domainName;
    private ContainerHost nameNode, jobTracker, secondaryNameNode;
    private List<ContainerHost> masterNodes;
    private List<ContainerHost> dataNodes, taskTrackers;
    private Integer replicationFactor = 1, countOfSlaveNodes = 1;
    private Set<ContainerHost> blockedAgents;
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

        if ( clusterConfig.isNameNode( containerHost ) )
        {
            nodeRoles.add( NodeType.NAMENODE );
        }
        if ( clusterConfig.isSecondaryNameNode( containerHost ) )
        {
            nodeRoles.add( NodeType.SECONDARY_NAMENODE );
        }
        if ( clusterConfig.isJobTracker( containerHost ) )
        {
            nodeRoles.add( NodeType.JOBTRACKER );
        }
        if ( clusterConfig.isDataNode( containerHost ) )
        {
            nodeRoles.add( NodeType.DATANODE );
        }
        if ( clusterConfig.isTaskTracker( containerHost ) )
        {
            nodeRoles.add( NodeType.TASKTRACKER );
        }

        return nodeRoles;
    }


    public boolean isDataNode( ContainerHost containerHost )
    {
        return getAllDataNodeAgent().contains( containerHost.getAgent().getUuid() );
    }


    public Set<UUID> getAllDataNodeAgent()
    {
        Set<UUID> allAgents = new HashSet<>();
        for ( ContainerHost containerHost : getDataNodes() )
        {
            allAgents.add( containerHost.getAgent().getUuid() );
        }
        return allAgents;
    }


    public List<ContainerHost> getDataNodes()
    {
        return dataNodes;
    }


    public void setDataNodes( List<ContainerHost> dataNodes )
    {
        this.dataNodes = dataNodes;
    }


    public boolean isTaskTracker( ContainerHost containerHost )
    {
        return getAllTaskTrackerNodeAgents().contains( containerHost.getAgent().getUuid() );
    }


    public Set<UUID> getAllTaskTrackerNodeAgents()
    {
        Set<UUID> allAgents = new HashSet<>();
        for ( ContainerHost containerHost : getTaskTrackers() )
        {
            allAgents.add( containerHost.getAgent().getUuid() );
        }
        return allAgents;
    }


    public List<ContainerHost> getTaskTrackers()
    {
        return taskTrackers;
    }


    public void setTaskTrackers( List<ContainerHost> taskTrackers )
    {
        this.taskTrackers = taskTrackers;
    }


    public boolean isNameNode( ContainerHost containerHost )
    {
        return getNameNode().getAgent().getUuid().equals( containerHost.getAgent().getUuid() );
    }


    public boolean isJobTracker( ContainerHost containerHost )
    {
        return getJobTracker().getAgent().getUuid().equals( containerHost.getAgent().getUuid() );
    }


    public boolean isSecondaryNameNode( ContainerHost containerHost )
    {
        return getSecondaryNameNode().getAgent().getUuid().equals( containerHost.getAgent().getUuid() );
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


    public ContainerHost getNode( Agent agent )
    {
        Preconditions.checkNotNull( agent, "Agent is null" );
        for ( ContainerHost containerHost : getAllNodes() )
        {
            if ( containerHost.getAgent().getUuid().equals( agent.getUuid() ) )
            {
                return containerHost;
            }
        }
        return null;
    }


    public List<ContainerHost> getAllNodes()
    {
        Set<ContainerHost> allAgents = new HashSet<>();
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
        for ( ContainerHost containerHost : getAllMasterNodes() )
        {
            allAgents.add( containerHost.getAgent().getUuid() );
        }
        return allAgents;
    }


    public Set<ContainerHost> getAllMasterNodes()
    {
        Preconditions.checkNotNull( nameNode, "NameNode is null" );
        Preconditions.checkNotNull( jobTracker, "JobTracker is null" );
        Preconditions.checkNotNull( secondaryNameNode, "SecondaryNameNode is null" );
        Set<ContainerHost> allMastersNodes = new HashSet<>();
        allMastersNodes.add( nameNode );
        allMastersNodes.add( jobTracker );
        allMastersNodes.add( secondaryNameNode );
        return allMastersNodes;
    }


    public Set<UUID> getAllSlaveNodesAgents()
    {
        Set<UUID> allAgents = new HashSet<>();
        for ( ContainerHost containerHost : getAllSlaveNodes() )
        {
            allAgents.add( containerHost.getAgent().getUuid() );
        }
        return allAgents;
    }


    public List<ContainerHost> getAllSlaveNodes()
    {
        Set<ContainerHost> allAgents = new HashSet<>();
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


    public Set<ContainerHost> getBlockedAgents()
    {
        return blockedAgents;
    }

    public Set<UUID> getBlockedAgentUUIDs(){
        Set<UUID> blockedAgents = new HashSet<>();
        for ( ContainerHost containerHost : getBlockedAgents() ){
            blockedAgents.add( containerHost.getAgent().getUuid() );
        }
        return blockedAgents;
    }


    public void setBlockedAgents( HashSet<ContainerHost> blockedAgents )
    {
        this.blockedAgents = blockedAgents;
    }


    public boolean isMasterNode( ContainerHost containerHost )
    {
        return containerHost.getAgent().getUuid().equals( getNameNode().getAgent().getUuid() ) ||
                containerHost.getAgent().getUuid().equals( getJobTracker().getAgent().getUuid() ) ||
                containerHost.getAgent().getUuid().equals( getSecondaryNameNode().getAgent().getUuid() );
    }


    public ContainerHost getNameNode()
    {
        return nameNode;
    }


    public void setNameNode( ContainerHost nameNode )
    {
        this.nameNode = nameNode;
    }


    public ContainerHost getJobTracker()
    {
        return jobTracker;
    }


    public void setJobTracker( ContainerHost jobTracker )
    {
        this.jobTracker = jobTracker;
    }


    public ContainerHost getSecondaryNameNode()
    {
        return secondaryNameNode;
    }


    public void setSecondaryNameNode( ContainerHost secondaryNameNode )
    {
        this.secondaryNameNode = secondaryNameNode;
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
