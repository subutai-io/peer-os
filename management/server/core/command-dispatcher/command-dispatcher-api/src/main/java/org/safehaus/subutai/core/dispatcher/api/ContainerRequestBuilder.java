/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.dispatcher.api;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;

import com.google.common.base.Preconditions;


/**
 * Represents command to agent. This class is used when for each agent there is a specific custom command, not common to
 * all agents
 */
public class ContainerRequestBuilder extends RequestBuilder
{

    //target agent
    private final Container container;


    /**
     * Constructor
     *
     * @param container - target container
     * @param command - command to run
     */
    public ContainerRequestBuilder( Container container, String command )
    {
        super( command );
        Preconditions.checkNotNull( container, "Container is null" );

        this.container = container;
    }


    /**
     * Returns target container
     *
     * @return - container
     */
    public Container getContainer()
    {
        return container;
    }


    public Request build( final UUID taskUUID )
    {
        return super.build( container.getAgentId(), taskUUID );
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof ContainerRequestBuilder ) )
        {
            return false;
        }
        if ( !super.equals( o ) )
        {
            return false;
        }

        final ContainerRequestBuilder that = ( ContainerRequestBuilder ) o;

        return container.equals( that.container );
    }


    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + container.hashCode();
        return result;
    }
}
