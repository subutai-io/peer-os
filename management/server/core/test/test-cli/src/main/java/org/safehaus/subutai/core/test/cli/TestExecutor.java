package org.safehaus.subutai.core.test.cli;


import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.test.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Command;


@Command( scope = "test", name = "exec" )
public class TestExecutor extends SubutaiShellCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( TestExecutor.class.getName() );


    private final Test test;


    public TestExecutor( final Test test )
    {
        this.test = test;
    }


    @Override
    protected Object doExecute()
    {

        test.testExecutor();
        return null;
    }
}
