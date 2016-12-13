package io.subutai.core.test.cli;


import org.apache.commons.lang.StringUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "test", name = "exception", description = "test exception command" )
public class TestExceptionCommand extends SubutaiShellCommandSupport
{

    @Argument( index = 0, name = "message", multiValued = false, required = false, description = "Exception message" )
    protected String message;


    @Override
    protected Object doExecute()
    {
        String msg = StringUtils.isEmpty( message ) ? "Test exception" : message;

        throw new RuntimeException( msg );
    }
}
