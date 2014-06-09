package org.safehaus.subutai.impl.packagemanager.handler;

import java.util.concurrent.Callable;
import org.safehaus.subutai.impl.packagemanager.DebPackageManager;

public abstract class AbstractHandler<T> implements Callable<T> {

    protected final DebPackageManager packageManager;

    public AbstractHandler(DebPackageManager pm) {
        this.packageManager = pm;
    }

    public abstract T performAction();

    @Override
    public T call() throws Exception {
        return performAction();
    }

}
