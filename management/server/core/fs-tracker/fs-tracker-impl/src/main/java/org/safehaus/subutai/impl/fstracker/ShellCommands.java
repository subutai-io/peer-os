package org.safehaus.subutai.impl.fstracker;


import org.safehaus.subutai.api.fstracker.FSTrackerTest;
import org.safehaus.subutai.api.fstracker.Listener;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "fs-tracker", name = "cli" )
public class ShellCommands extends OsgiCommandSupport {

    Listener listener;

    @Override
    protected Object doExecute() {

//        FSTrackerTest.sayHello();
        showMessage();

        return null;
    }

    private void showMessage() {
        System.out.println( "message" );
    }
}
