package org.safehaus.subutai.core.env.impl.exception;


/**
 * NodeGroup specific error exceptions.
 *
 * @see org.safehaus.subutai.core.env.impl.builder.NodeGroupBuilder#fetchRequiredTemplates(java.util.UUID, String)
 * @see org.safehaus.subutai.core.env.impl.builder.NodeGroupBuilder#call()
 */
public class NodeGroupBuildException extends Exception
{
    public NodeGroupBuildException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
