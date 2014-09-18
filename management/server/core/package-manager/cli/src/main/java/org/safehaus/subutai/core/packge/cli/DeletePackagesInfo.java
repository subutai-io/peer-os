package org.safehaus.subutai.core.packge.cli;


import org.safehaus.subutai.core.packge.api.PackageManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command(scope = "deb-package", name = "delete", description = "delete previously saved packages info")
public class DeletePackagesInfo extends OsgiCommandSupport {

    private PackageManager packageManager;
    @Argument(index = 0, required = true)
    private String hostname;


    public PackageManager getPackageManager()
    {
        return packageManager;
    }


    public void setPackageManager( PackageManager packageManager )
    {
        this.packageManager = packageManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        boolean deleted = packageManager.deletePackagesInfo( hostname );
        if ( deleted )
        {
            System.out.println( "Packages info deleted for " + hostname );
        }
        else
        {
            System.out.println( "Packages info not deleted" );
        }

        return null;
    }
}
