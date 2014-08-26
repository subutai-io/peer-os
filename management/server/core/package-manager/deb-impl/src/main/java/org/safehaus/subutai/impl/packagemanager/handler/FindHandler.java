package org.safehaus.subutai.impl.packagemanager.handler;

import org.safehaus.subutai.api.packagemanager.PackageInfo;
import org.safehaus.subutai.impl.packagemanager.DebPackageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class FindHandler extends AbstractHandler<Collection<PackageInfo>> {

	public FindHandler(DebPackageManager pm, String hostname) {
		super(pm, hostname);
	}

	@Override
	Logger getLogger() {
		return LoggerFactory.getLogger(FindHandler.class);
	}

	@Override
	public Collection<PackageInfo> performAction() {
		ListHandler ls = new ListHandler(packageManager, hostname);
		ls.setFromFile(true);
		return ls.performAction();
	}

}
