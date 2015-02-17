package org.safehaus.subutai.core.executor.impl;


import org.safehaus.subutai.common.command.Request;

import com.google.common.base.Preconditions;


/**
 * Serializes command request
 */
public class RequestWrapper
{
    private final Request request;


    public RequestWrapper( final Request request )
    {
        Preconditions.checkNotNull( request );

        this.request = request;
    }
}
