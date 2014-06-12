package org.safehaus.subutai.impl.packagemanager.handler;

import java.util.concurrent.Callable;
import org.safehaus.subutai.impl.packagemanager.DebPackageManager;
import org.safehaus.subutai.shared.protocol.Agent;
import org.slf4j.Logger;

public abstract class AbstractHandler<T> implements Callable<T> {

    protected final DebPackageManager packageManager;
    protected final String hostname;

    public AbstractHandler(DebPackageManager pm, String hostname) {
        this.packageManager = pm;
        this.hostname = hostname;
    }

    public abstract T performAction();

    abstract Logger getLogger();

    @Override
    public T call() throws Exception {
        return performAction();
    }

    public Agent getAgent() {
        return packageManager.getAgentManager().getAgentByHostname(hostname);
    }

}
