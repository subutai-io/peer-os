package io.subutai.core.localpeer.impl.container;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandResult;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.quota.Quota;
import io.subutai.common.task.Command;
import io.subutai.common.task.CommandBatch;
import io.subutai.common.task.QuotaRequest;
import io.subutai.common.task.QuotaResponse;
import io.subutai.common.task.TaskResponseBuilder;
import io.subutai.core.lxc.quota.api.QuotaManager;


//todo call RH.setContainerQuota
public class QuotaTask extends AbstractTask<QuotaRequest, QuotaResponse>
        implements TaskResponseBuilder<QuotaRequest, QuotaResponse>
{
    protected static final Logger LOG = LoggerFactory.getLogger( QuotaTask.class );
    private final QuotaManager quotaManager;


    public QuotaTask( QuotaManager quotaManager, final QuotaRequest request )
    {
        super( request );
        this.quotaManager = quotaManager;
    }


    public CommandBatch getCommandBatch() throws Exception
    {
        CommandBatch result = new CommandBatch( CommandBatch.Type.JSON );

        ContainerQuota quota = quotaManager.getDefaultContainerQuota( request.getSize() );
        for ( Quota r : quota.getAll() )
        {

            Command quotaCommand = new Command( "quota" );
            quotaCommand.addArgument( request.getHostname() );
            quotaCommand.addArgument( r.getResource().getContainerResourceType().getKey() );
            quotaCommand.addArgument( "-s" );
            quotaCommand.addArgument( r.getResource().getWriteValue() );
            result.addCommand( quotaCommand );
        }

        return result;
    }


    @Override
    public TaskResponseBuilder<QuotaRequest, QuotaResponse> getResponseBuilder()
    {
        return this;
    }


    @Override
    public QuotaResponse build( final QuotaRequest request, final CommandResult commandResult, final long elapsedTime )
    {
        final boolean succeeded = commandResult != null && commandResult.hasSucceeded();
        return new QuotaResponse( request.getResourceHostId(), request.getHostname(), succeeded, getElapsedTime() );
    }
}
