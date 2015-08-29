package io.subutai.core.environment.impl.exception;


/**
 * NodeGroup specific error exceptions.
 *
 * @see io.subutai.core.env.impl.builder.NodeGroupBuilder#fetchRequiredTemplates(java.util.UUID, String)
 * @see io.subutai.core.env.impl.builder.NodeGroupBuilder#call()
 */
public class NodeGroupBuildException extends Exception
{
    public NodeGroupBuildException( final String message, final Throwable cause )
    {
        super( message, cause );
    }
}
