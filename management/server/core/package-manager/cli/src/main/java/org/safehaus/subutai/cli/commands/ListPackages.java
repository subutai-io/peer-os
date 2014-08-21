package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.api.packagemanager.PackageManager;

import java.util.Collection;

@Command (scope = "deb-package", name = "list", description = "list packages")
public class ListPackages extends OsgiCommandSupport {

	private PackageManager packageManager;
	@Argument (index = 0, required = true)
	private String hostname;
	@Argument (index = 1, description = "package name pattern")
	private String namePattern;

	public PackageManager getPackageManager() {
		return packageManager;
	}

	public void setPackageManager(PackageManager packageManager) {
		this.packageManager = packageManager;
	}

	@Override
	protected Object doExecute() throws Exception {
		Collection<PackageInfo> ls = packageManager.listPackages(hostname, namePattern);
		if (ls == null)
			System.out.println("Invalid hostname or agent is not connected");
		else
			for (PackageInfo pi : ls) System.out.println(pi);

		return null;
	}

}
