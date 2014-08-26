package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.api.packagemanager.PackageManager;

import java.util.Collection;

@Command (scope = "deb-package", name = "find", description = "find previously saved packages info")
public class FindPackagesInfo extends OsgiCommandSupport {

	private PackageManager packageManager;
	@Argument (index = 0, required = true)
	private String hostname;

	public PackageManager getPackageManager() {
		return packageManager;
	}

	public void setPackageManager(PackageManager packageManager) {
		this.packageManager = packageManager;
	}

	@Override
	protected Object doExecute() throws Exception {
		Collection<PackageInfo> ls = packageManager.findPackagesInfo(hostname);
		if (ls == null)
			System.out.println("Package info not found for " + hostname);
		else
			for (PackageInfo pi : ls) System.out.println(pi);

		return null;
	}

}
