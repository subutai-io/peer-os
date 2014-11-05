package org.safehaus.subutai.plugin.common.api;


public interface ClusterOperationHandlerInterface
{

    public void runOperationOnContainers( OperationType operationType );

    public void setupCluster();

    public void destroyCluster();
}
