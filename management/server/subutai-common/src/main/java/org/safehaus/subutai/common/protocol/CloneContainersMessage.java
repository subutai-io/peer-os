package org.safehaus.subutai.common.protocol;


import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.gson.reflect.TypeToken;


/**
 * Created by bahadyr on 9/19/14.
 */
public class CloneContainersMessage extends PeerCommandMessage
{
    private String template;
    private int numberOfNodes;
    private String Strategy;
    private List<String> criteria;

    public CloneContainersMessage( UUID envId, UUID peerId )
    {
        super( PeerCommandType.CLONE, envId, peerId, null );
    }


    public String getTemplate()
    {
        return template;
    }


    public void setTemplate( final String template )
    {
        this.template = template;
    }


    public int getNumberOfNodes()
    {
        return numberOfNodes;
    }


    public void setNumberOfNodes( final int numberOfNodes )
    {
        this.numberOfNodes = numberOfNodes;
    }


    public String getStrategy()
    {
        return Strategy;
    }


    public void setStrategy( final String strategy )
    {
        Strategy = strategy;
    }


    public List<String> getCriteria()
    {
        return criteria;
    }


    public void setCriteria( final List<String> criteria )
    {
        this.criteria = criteria;
    }


    @Override
    public Type getResultObjectType()
    {
        return new TypeToken<Set<Agent>>()
        {
        }.getType();
    }


    @Override
    public String toString()
    {
        return "CloneContainersMessage{" +
                "envId=" + envId +
                ", template='" + template + '\'' +
                ", numberOfNodes=" + numberOfNodes +
                ", Strategy='" + Strategy + '\'' +
                ", criteria=" + criteria +
                ", success=" + success +
                '}';
    }


    public void incrementNumberOfNodes()
    {
        numberOfNodes++;
    }
}
