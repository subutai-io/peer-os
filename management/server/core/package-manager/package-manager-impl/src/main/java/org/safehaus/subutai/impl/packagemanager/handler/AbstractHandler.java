package org.safehaus.subutai.impl.packagemanager.handler;

import java.util.concurrent.Callable;
import org.safehaus.subutai.impl.packagemanager.PackageManagerImpl;

public abstract class AbstractHandler<T> implements Callable<T> {

    protected final PackageManagerImpl packageManager;

    public AbstractHandler(PackageManagerImpl pm) {
        this.packageManager = pm;
    }

    public abstract T performAction();

    @Override
    public T call() throws Exception {
        return performAction();
    }

}
