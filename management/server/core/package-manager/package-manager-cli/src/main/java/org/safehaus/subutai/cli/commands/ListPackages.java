package org.safehaus.subutai.cli.commands;

import java.util.Collection;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.api.packagemanager.PackageManager;

@Command(scope = "deb-package", name = "list", description = "list packages")
public class ListPackages extends OsgiCommandSupport {

    private PackageManager packageManager;
    @Argument(index = 0, required = true)
    private String hostname;

    public PackageManager getPackageManager() {
        return packageManager;
    }

    public void setPackageManager(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @Override
    protected Object doExecute() throws Exception {
        if(hostname != null) {
            Collection<PackageInfo> ls = packageManager.listPackages(hostname);
            for(PackageInfo pi : ls) {
                System.out.println(pi);
            }
        } else System.out.println("Target host is not defined");

        return null;
    }

}
