package org.safehaus.subutai.plugin.hadoop.api;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;


/**
 * Created by daralbaev on 02.04.14.
 */
public class HadoopClusterConfig implements ConfigBase
{
    public static final String PRODUCT_KEY = "Hadoop";

    public static final int DEFAULT_HADOOP_MASTER_NODES_QUANTITY = 3;
    public static final String PRODUCT_NAME = PRODUCT_KEY.toLowerCase();
    private String templateName = PRODUCT_NAME;
    public static final int NAME_NODE_PORT = 8020, JOB_TRACKER_PORT = 9000;

    private String clusterName, domainName;
    private Agent nameNode, jobTracker, secondaryNameNode;
    private List<Agent> dataNodes, taskTrackers;
    private Integer replicationFactor = 1, countOfSlaveNodes = 1;
    private Set<Agent> blockedAgents;


    public HadoopClusterConfig()
    {
        domainName = Common.DEFAULT_DOMAIN_NAME;
        dataNodes = new ArrayList<>();
        taskTrackers = new ArrayList<>();
        blockedAgents = new HashSet<>();
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public List<Agent> getAllNodes()
    {
        Set<Agent> allAgents = new HashSet<>();
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


    public List<Agent> getAllSlaveNodes()
    {
        Set<Agent> allAgents = new HashSet<>();
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


    public void removeNode( Agent agent )
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


    public Agent getNameNode()
    {
        return nameNode;
    }


    public void setNameNode( Agent nameNode )
    {
        this.nameNode = nameNode;
    }


    public Agent getJobTracker()
    {
        return jobTracker;
    }


    public void setJobTracker( Agent jobTracker )
    {
        this.jobTracker = jobTracker;
    }


    public Agent getSecondaryNameNode()
    {
        return secondaryNameNode;
    }


    public void setSecondaryNameNode( Agent secondaryNameNode )
    {
        this.secondaryNameNode = secondaryNameNode;
    }


    public List<Agent> getDataNodes()
    {
        return dataNodes;
    }


    public void setDataNodes( List<Agent> dataNodes )
    {
        this.dataNodes = dataNodes;
    }


    public List<Agent> getTaskTrackers()
    {
        return taskTrackers;
    }


    public void setTaskTrackers( List<Agent> taskTrackers )
    {
        this.taskTrackers = taskTrackers;
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


    public Set<Agent> getBlockedAgents()
    {
        return blockedAgents;
    }


    public void setBlockedAgents( HashSet<Agent> blockedAgents )
    {
        this.blockedAgents = blockedAgents;
    }


    public boolean isMasterNode( Agent agent )
    {
        if ( agent.equals( getNameNode() ) || agent.equals( getJobTracker() ) || agent
                .equals( getSecondaryNameNode() ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    public boolean isDataNode( Agent agent )
    {
        if ( getDataNodes().contains( agent ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    public boolean isTaskTracker( Agent agent )
    {
        if ( getTaskTrackers().contains( agent ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    public boolean isNameNode( Agent agent )
    {
        if ( getNameNode().equals( agent ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    public boolean isJobTracker( Agent agent )
    {
        if ( getJobTracker().equals( agent ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    public boolean isSecondaryNameNode( Agent agent )
    {
        if ( getSecondaryNameNode().equals( agent ) )
        {
            return true;
        }
        else
        {
            return false;
        }
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
