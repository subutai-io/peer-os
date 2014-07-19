package org.safehaus.subutai.cli.elasticsearch;


import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "elasticsearch", name = "list-clusters" )
public class InstallCommand extends OsgiCommandSupport {

    protected Object doExecute() {

        System.out.println( "hello from cli" );

        return null;
    }
}
