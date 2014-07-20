package org.safehaus.subutai.cli.monitor;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "monitor", name = "test" )
public class TestCommand extends OsgiCommandSupport {

    @Argument( index = 0, name = "metric", required = true, multiValued = false )
    private String metric = null;


    protected Object doExecute() {

        System.out.println( "monitoring test" );

        return null;
    }
}
