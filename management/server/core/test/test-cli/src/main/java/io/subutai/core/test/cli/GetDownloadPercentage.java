package io.subutai.core.test.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.kurjun.api.TemplateManager;


@Command( scope = "download", name = "get-percentage", description = "test download progress" )
public class GetDownloadPercentage extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( TestCommand.class.getName() );

    private TemplateManager templateManager;

    @Argument( index = 0, name = "log error", required = false, multiValued = false, description = "log error" )
    String templateId;


    public void setTemplateManager( final TemplateManager templateManager )
    {
        this.templateManager = templateManager;
    }


    @Override
    public Object execute( final CommandSession session ) throws Exception
    {
        try
        {
            System.out.println( templateManager.getDownloadPercent( templateId ) );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in test", e );
        }

        return null;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        return null;
    }
}
