package org.safehaus.kiskis.mgmt.impl.hive.handler;

import org.safehaus.kiskis.mgmt.impl.hive.HiveImpl;

public class AbstractHandlerMock extends AbstractHandler {

    public AbstractHandlerMock(HiveImpl manager, String clusterName) {
        super(manager, clusterName);
    }

    @Override
    public void run() {
    }

}
