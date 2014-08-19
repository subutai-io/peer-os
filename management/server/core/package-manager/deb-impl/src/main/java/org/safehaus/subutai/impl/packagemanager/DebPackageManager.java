package org.safehaus.subutai.impl.packagemanager;

import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.impl.packagemanager.handler.DeleteHandler;
import org.safehaus.subutai.impl.packagemanager.handler.FindHandler;
import org.safehaus.subutai.impl.packagemanager.handler.ListHandler;
import org.safehaus.subutai.impl.packagemanager.handler.SaveHandler;

import java.util.Collection;

public class DebPackageManager extends DebPackageManagerBase {

	@Override
	public Collection<PackageInfo> listPackages(String hostname) {
		return listPackages(hostname, null);
	}

	@Override
	public Collection<PackageInfo> listPackages(String hostname, String namePattern) {
		ListHandler h = new ListHandler(this, hostname);
		h.setNamePattern(namePattern);
		return h.performAction();
	}

	@Override
	public Collection<PackageInfo> findPackagesInfo(String hostname) {
		FindHandler h = new FindHandler(this, hostname);
		return h.performAction();
	}

	@Override
	public Collection<PackageInfo> savePackagesInfo(String hostname) {
		SaveHandler h = new SaveHandler(this, hostname);
		return h.performAction();
	}

	@Override
	public boolean deletePackagesInfo(String hostname) {
		DeleteHandler h = new DeleteHandler(this, hostname);
		Boolean b = h.performAction();
		return b != null ? b.booleanValue() : false;
	}

}
