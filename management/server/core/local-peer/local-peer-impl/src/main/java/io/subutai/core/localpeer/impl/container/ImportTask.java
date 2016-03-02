package io.subutai.core.localpeer.impl.container;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandResult;
import io.subutai.common.task.Command;
import io.subutai.common.task.CommandBatch;
import io.subutai.common.task.ImportTemplateRequest;
import io.subutai.common.task.ImportTemplateResponse;
import io.subutai.common.task.TaskResponseBuilder;


public class ImportTask extends AbstractTask<ImportTemplateRequest, ImportTemplateResponse>
        implements TaskResponseBuilder<ImportTemplateRequest, ImportTemplateResponse>

{
    protected static final Logger LOG = LoggerFactory.getLogger( ImportTask.class );

    private static final int DOWNLOAD_TIMEOUT = 60 * 60 * 5; // 5 hour


    public ImportTask( final ImportTemplateRequest request )
    {
        super( request );
    }


    public CommandBatch getCommandBatch() throws Exception
    {
        CommandBatch result = new CommandBatch( CommandBatch.Type.STANDARD );

        Command importAction = new Command( "import" );

        importAction.addArgument( request.getTemplateName() );

        result.addCommand( importAction );

        return result;
    }


    @Override
    public int getTimeout()
    {
        return DOWNLOAD_TIMEOUT;
    }


    @Override
    public boolean isSequential()
    {
        return false;
    }


    @Override
    public TaskResponseBuilder<ImportTemplateRequest, ImportTemplateResponse> getResponseBuilder()
    {
        return this;
    }


    @Override
    public ImportTemplateResponse build( final ImportTemplateRequest request, final CommandResult commandResult,
                                         final long elapsedTime )
    {
        boolean succeeded = commandResult != null && commandResult.hasSucceeded();
        return new ImportTemplateResponse( request.getResourceHostId(), request.getTemplateName(), succeeded,
                elapsedTime, succeeded ? getStdOut() : getStdErr() );
    }
}
