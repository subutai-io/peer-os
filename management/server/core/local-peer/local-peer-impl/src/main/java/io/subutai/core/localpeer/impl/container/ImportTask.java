package io.subutai.core.localpeer.impl.container;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandResultParser;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.task.Command;
import io.subutai.common.task.CommandBatch;


public class ImportTask extends AbstractTask<Boolean> implements CommandResultParser<Boolean>
{
    protected static final Logger LOG = LoggerFactory.getLogger( ImportTask.class );

    private static final int DOWNLOAD_TIMEOUT = 60 * 60 * 5; // 5 hour
    private final ResourceHost resourceHost;
    private String templateName;


    public ImportTask( final ResourceHost resourceHost, final String templateName )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkNotNull( templateName );

        this.resourceHost = resourceHost;
        this.templateName = templateName;
    }


    public CommandBatch getCommandBatch() throws Exception
    {
        CommandBatch result = new CommandBatch();

        Command importAction = new Command( "import" );

        importAction.addArgument( templateName );

        result.addCommand( importAction );

        return result;
    }


    @Override
    public Host getHost()
    {
        return resourceHost;
    }


    @Override
    public CommandResultParser<Boolean> getCommandResultParser()
    {
        return this;
    }


    @Override
    public Boolean parse( final CommandResult commandResult )
    {
        return commandResult != null && commandResult.hasSucceeded();
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
