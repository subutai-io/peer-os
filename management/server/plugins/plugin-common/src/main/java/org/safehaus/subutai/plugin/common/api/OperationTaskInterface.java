package org.safehaus.subutai.plugin.common.api;


import java.util.UUID;


public interface OperationTaskInterface
{
    public UUID startOperation();

    public String getProductStoppedIdentifier();

    public String getProductRunningIdentifier();

    public void waitUntilOperationFinish ( UUID trackID );


}
