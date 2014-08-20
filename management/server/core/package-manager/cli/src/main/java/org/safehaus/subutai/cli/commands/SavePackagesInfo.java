package org.safehaus.subutai.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.api.packagemanager.PackageManager;

import java.util.Collection;

@Command (scope = "deb-package", name = "save", description = "save packages info")
public class SavePackagesInfo extends OsgiCommandSupport {

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
		Collection<PackageInfo> ls = packageManager.savePackagesInfo(hostname);
		if (ls == null)
			System.out.println("Failed to save packages info for " + hostname);
		else
			System.out.println("Packages info saved for " + hostname);

		return null;
	}

}
