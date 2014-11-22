package org.safehaus.subutai.core.peer.api;


import java.util.List;

import org.safehaus.subutai.common.protocol.Template;

import com.google.common.base.Preconditions;


/**
 * Order for container cloning process.
 */
public class ContainerCreateOrder
{
    private String customerId;
    private String hostname;
    private String nodeGroupName;
    private String environmentId;
    private State state = State.ORDERED;
    private String id;
    private String ip;
    private List<Template> templates;
    private String description;


    public ContainerCreateOrder( final String customerId, final String hostname, final String nodeGroupName,
                                 final String environmentId, String ip, List<Template> templates )
    {
        Preconditions.checkNotNull( customerId, "Customer ID is null." );
        Preconditions.checkNotNull( hostname, "Host name is null." );
        Preconditions.checkNotNull( nodeGroupName, "Node group name is null." );
        Preconditions.checkNotNull( environmentId, "Environment ID is null." );
        Preconditions.checkNotNull( templates, "Template list is null." );
        Preconditions.checkState( templates.size() == 0, "Template list is empty" );

        this.hostname = hostname;
        this.nodeGroupName = nodeGroupName;
        this.environmentId = environmentId;
        this.customerId = customerId;
        this.ip = ip;
        this.templates = templates;
    }


    public String getHostname()
    {
        return hostname;
    }


    public String getNodeGroupName()
    {
        return nodeGroupName;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public String getCustomerId()
    {
        return customerId;
    }


    public String getIp()
    {
        return ip;
    }


    public List<Template> getTemplates()
    {
        return templates;
    }


    public State getState()
    {
        return state;
    }


    public void setState( final State state )
    {
        this.state = state;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription( final String description )
    {
        this.description = description;
    }


    /**
     * Returns created container ID;
     */
    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    /**
     * Returns the template name from them resource host will clone a container. Assumed that is last in the list.
     */
    public String getTemplateName()
    {
        return templates.get( templates.size() - 1 ).getTemplateName();
    }


    public enum State
    {
        ORDERED, SUCCESS, FAIL
    }
}
