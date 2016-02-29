package io.subutai.core.localpeer.impl.container;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandResultParser;
import io.subutai.common.task.ImportTemplateRequest;
import io.subutai.common.task.ImportTemplateResponse;
import io.subutai.common.task.Command;
import io.subutai.common.task.CommandBatch;


public class ImportTask extends AbstractTask<ImportTemplateRequest, ImportTemplateResponse>
        implements CommandResultParser<ImportTemplateResponse>
{
    protected static final Logger LOG = LoggerFactory.getLogger( ImportTask.class );

    private static final int DOWNLOAD_TIMEOUT = 60 * 60 * 5; // 5 hour


    public ImportTask( final ImportTemplateRequest request )
    {
        super( request );
    }


    public CommandBatch getCommandBatch() throws Exception
    {
        CommandBatch result = new CommandBatch();

        Command importAction = new Command( "import" );

        importAction.addArgument( request.getTemplateName() );

        result.addCommand( importAction );

        return result;
    }


    @Override
    public CommandResultParser<ImportTemplateResponse> getCommandResultParser()
    {
        return this;
    }


    @Override
    public ImportTemplateResponse parse( final CommandResult commandResult )
    {
        final boolean succeeded = commandResult != null && commandResult.hasSucceeded();
        final ImportTemplateResponse importTemplateResponse =
                new ImportTemplateResponse( request.getResourceHostId(), request.getTemplateName(), succeeded );
        if ( succeeded )
        {
            importTemplateResponse.addSucceededMessage(
                    String.format( "Importing template %s on %s succeeded.", request.getTemplateName(),
                            request.getResourceHostId() ), commandResult.getStdOut() );
        }
        else
        {
            importTemplateResponse.addFailMessage(
                    String.format( "Importing template %s on %s failed.", request.getTemplateName(),
                            request.getResourceHostId() ), commandResult != null ? commandResult.getStdErr() : "" );
        }
        return importTemplateResponse;
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
}
