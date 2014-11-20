package org.safehaus.subutai.common.protocol;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by bahadyr on 9/19/14.
 */
public class CloneContainersMessage
{
    private int numberOfNodes;
    private PlacementStrategy strategy;
    private List<Template> templates = new ArrayList();
    private String nodeGroupName;
    private UUID targetPeerId;


    public UUID getTargetPeerId()
    {
        return targetPeerId;
    }


    public void setTargetPeerId( final UUID targetPeerId )
    {
        this.targetPeerId = targetPeerId;
    }


    public String getNodeGroupName()
    {
        return nodeGroupName;
    }
    //    private Set<Agent> agents;


    public void setNodeGroupName( final String nodeGroupName )
    {
        this.nodeGroupName = nodeGroupName;
    }


    public int getNumberOfNodes()
    {
        return numberOfNodes;
    }


    public void setNumberOfNodes( final int numberOfNodes )
    {
        this.numberOfNodes = numberOfNodes;
    }


    public PlacementStrategy getStrategy()
    {
        return strategy;
    }


    public void setStrategy( final PlacementStrategy strategy )
    {
        this.strategy = strategy;
    }


    @Override
    public String toString()
    {
        return "CloneContainersMessage{" +
                ", numberOfNodes=" + numberOfNodes +
                ", Strategy='" + strategy + '\'' +
                ", templates=" + templates +
                '}';
    }


    public void incrementNumberOfNodes()
    {
        numberOfNodes++;
    }


    public List<Template> getTemplates()
    {
        return templates;
    }


    public void setTemplates( final List<Template> templates )
    {
        this.templates = templates;
    }


    public void addTemplate( Template t )
    {
        if ( t == null )
        {
            throw new IllegalArgumentException( "Template could not be null." );
        }
        this.templates.add( t );
    }
}
