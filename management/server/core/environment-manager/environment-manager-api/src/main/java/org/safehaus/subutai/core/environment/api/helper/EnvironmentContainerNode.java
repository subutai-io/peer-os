package org.safehaus.subutai.core.environment.api.helper;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ContainerException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.protocol.DefaultCommandMessage;
import org.safehaus.subutai.common.protocol.Template;


/**
 * Created by bahadyr on 7/24/14.
 */
public class EnvironmentContainerNode extends Container
{

    private Agent agent;
    private Template template;
    private String nodeGroupName;


    public EnvironmentContainerNode( final Agent agent, final Template template, final String nodeGroupName )
    {
        this.agent = agent;
        this.template = template;
        this.nodeGroupName = nodeGroupName;
    }


    public String getNodeGroupName()
    {
        return nodeGroupName;
    }


    public Agent getAgent()
    {
        return agent;
    }


    public Template getTemplate()
    {
        return template;
    }


    @Override
    public String toString()
    {
        return "Node{" +
                "agent=" + agent +
                ", template=" + template +
                ", nodeGroupName='" + nodeGroupName + '\'' +
                '}';
    }


    @Override
    public UUID getEnvironmentId()
    {
        return null;
    }


    @Override
    public DefaultCommandMessage start() throws ContainerException
    {
        return null;
    }


    @Override
    public DefaultCommandMessage stop() throws ContainerException
    {
        return null;
    }


    @Override
    public DefaultCommandMessage isConnected() throws ContainerException
    {
        return null;
    }
}
