package io.subutai.core.test.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.template.api.TemplateManager;


@Command( scope = "test", name = "do", description = "test command" )
public class TestCommand extends SubutaiShellCommandSupport
{

    @Argument( name = "templateName", description = "Template name", required = true )

    String templateName;


    @Override
    protected Object doExecute()
    {
        TemplateManager templateManager = ServiceLocator.lookup( TemplateManager.class );

        System.out.println(templateManager.getTemplateByName( templateName ));

        System.out.println(templateManager.getVerifiedTemplateByName( templateName ));

        return null;
    }
}
