package org.safehaus.subutai.core.packge.impl.handler;

import org.safehaus.subutai.core.packge.impl.DebPackageManager;
import org.safehaus.subutai.common.protocol.Agent;
import org.slf4j.Logger;

import java.util.concurrent.Callable;

public abstract class AbstractHandler<T> implements Callable<T> {

	protected final DebPackageManager packageManager;
	protected final String hostname;

	public AbstractHandler(DebPackageManager pm, String hostname) {
		this.packageManager = pm;
		this.hostname = hostname;
	}

	abstract Logger getLogger();

	@Override
	public T call() throws Exception {
		return performAction();
	}

	public abstract T performAction();

	public Agent getAgent() {
		return packageManager.getAgentManager().getAgentByHostname(hostname);
	}

}
