package org.safehaus.subutai.cli.configpointtracker;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "conf-point-tracker", name = "add" )
public class AddCommand extends OsgiCommandSupport {



    protected Object doExecute() {

        System.out.println( "hello" );

        return null;
    }
}
