package org.safehaus.subutai.core.packge.cli;


import java.util.Collection;

import org.safehaus.subutai.core.packge.api.PackageInfo;
import org.safehaus.subutai.core.packge.api.PackageManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "deb-package", name = "find", description = "find previously saved packages info" )
public class FindPackagesInfo extends OsgiCommandSupport
{

    private PackageManager packageManager;
    @Argument( index = 0, required = true )
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
        Collection<PackageInfo> ls = packageManager.findPackagesInfo( hostname );
        if ( ls == null )
        {
            System.out.println( "Package info not found for " + hostname );
        }
        else
        {
            for ( PackageInfo pi : ls )
            {
                System.out.println( pi );
            }
        }

        return null;
    }
}
