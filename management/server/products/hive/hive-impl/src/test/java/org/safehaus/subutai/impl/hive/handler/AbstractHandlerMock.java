package org.safehaus.subutai.impl.hive.handler;

import org.safehaus.subutai.impl.hive.HiveImpl;

public class AbstractHandlerMock extends AbstractHandler {

	public AbstractHandlerMock(HiveImpl manager, String clusterName) {
		super(manager, clusterName);
	}

	@Override
	public void run() {
	}

}
