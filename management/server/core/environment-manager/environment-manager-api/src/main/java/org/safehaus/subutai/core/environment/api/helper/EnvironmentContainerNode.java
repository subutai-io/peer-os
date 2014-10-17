package org.safehaus.subutai.core.environment.api.helper;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ContainerException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.protocol.DefaultCommandMessage;
import org.safehaus.subutai.common.protocol.PeerCommandType;
import org.safehaus.subutai.common.protocol.Template;


/**
 * Created by bahadyr on 7/24/14.
 */
public class EnvironmentContainerNode extends Container
{

    private UUID environmentId;
    private String templateName;
    private Agent agent;
    private Template template;
    private String nodeGroupName;


    public EnvironmentContainerNode( final Agent agent, final Template template, final String nodeGroupName )
    {
        this.agent = agent;
        this.template = template;
        this.nodeGroupName = nodeGroupName;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    @Override
    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    @Override
    public DefaultCommandMessage start() throws ContainerException
    {
        DefaultCommandMessage cmd =
                new DefaultCommandMessage( PeerCommandType.START, environmentId, getPeerId(), getAgentId() );
        return cmd;
    }


    @Override
    public DefaultCommandMessage stop() throws ContainerException
    {
        DefaultCommandMessage cmd =
                new DefaultCommandMessage( PeerCommandType.STOP, environmentId, getPeerId(), getAgentId() );
        return cmd;
    }


    @Override
    public DefaultCommandMessage isConnected() throws ContainerException
    {
        DefaultCommandMessage cmd =
                new DefaultCommandMessage( PeerCommandType.IS_CONNECTED, environmentId, getPeerId(), getAgentId() );
        return cmd;
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
}
