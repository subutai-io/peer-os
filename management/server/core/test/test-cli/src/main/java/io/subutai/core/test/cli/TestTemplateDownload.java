package io.subutai.core.test.cli;


import java.io.IOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import ai.subut.kurjun.model.repository.Repository;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.kurjun.api.TemplateManager;


@Command( scope = "download", name = "template", description = "test download command" )
public class TestTemplateDownload extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( TestCommand.class.getName() );

    private TemplateManager templateManager;

    @Argument( index = 0, name = "log error", required = false, multiValued = false, description = "log error" )
    String templateName;


    public void setTemplateManager( final TemplateManager templateManager )
    {
        this.templateManager = templateManager;
    }


    @Override
    public Object execute( final CommandSession session ) throws Exception
    {
        try
        {
            final String repository = "public";
            final TemplateKurjun template = templateManager.getTemplateByName( repository, templateName, "", true );

            new Thread( new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        templateManager.getTemplateData( repository, template.getMd5Sum(), template.getOwnerFprint(),
                                true, new Repository.PackageProgressListener()

                        {
                            @Override
                            public String downloadFileId()
                            {
                                return template.getId();
                            }


                            @Override
                            public void writeBytes( final ByteBuffer byteBuffer )
                            {
                            }


                            @Override
                            public long getSize()
                            {
                                return template.getSize();
                            }
                        } );
                    }
                    catch ( IOException e )
                    {
                        LOG.error( "Error downloading template", e );
                    }
                }
            } ).start();
            System.out.println( template.getId() );
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
