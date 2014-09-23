package org.safehaus.subutai.common.protocol;


import java.util.List;
import java.util.UUID;


/**
 * Created by bahadyr on 9/19/14.
 */
public class CloneContainersMessage extends PeerCommandMessage
{
    private UUID envId;
    private String template;
    private int numberOfNodes;
    private String Strategy;
    private List<String> criteria;


    public UUID getEnvId()
    {
        return envId;
    }


    public void setEnvId( final UUID envId )
    {
        this.envId = envId;
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

}
