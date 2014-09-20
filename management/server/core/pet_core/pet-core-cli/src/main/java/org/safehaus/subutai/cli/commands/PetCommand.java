package org.safehaus.subutai.cli.commands;


import org.safehaus.subutai.pet.api.PetManager;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by bahadyr on 8/28/14.
 */
@Command( scope = "pet", name = "get-command" )
public class PetCommand extends OsgiCommandSupport
{

    private PetManager petManager;


    public PetManager getPetManager()
    {
        return petManager;
    }


    public void setPetManager( final PetManager petManager )
    {
        this.petManager = petManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        System.out.println( petManager.getPetName() );
        return null;
    }
}
