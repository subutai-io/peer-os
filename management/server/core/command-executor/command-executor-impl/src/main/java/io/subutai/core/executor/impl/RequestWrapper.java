package io.subutai.core.executor.impl;


import com.google.common.base.Preconditions;

import io.subutai.common.command.Request;


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
