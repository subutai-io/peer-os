package org.safehaus.subutai.cli.commands;


import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.wol.api.WolManager;


/**
 * Created by emin on 14/11/14.
 */
@Command( scope = "pet", name = "get-command" )
public class WolCommand extends OsgiCommandSupport {

    private WolManager wolManager;


    public WolManager getPetManager() {
        return wolManager;
    }


    public void setWolManager( final WolManager wolManager ) {
        this.wolManager = wolManager;
    }


    @Override
    protected Object doExecute() throws Exception {
        System.out.println(wolManager.getWolName());
        return null;
    }
}