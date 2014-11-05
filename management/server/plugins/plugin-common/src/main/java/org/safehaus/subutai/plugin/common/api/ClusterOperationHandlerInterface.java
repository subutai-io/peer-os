package org.safehaus.subutai.plugin.common.api;


public interface ClusterOperationHandlerInterface
{

    /**
     * Runs operations on containers.
     * This method will be used to start, stop, status
     * whole cluster.
     * @param operationType type of operation (START, STOP, STATUS)
     */
    public void runOperationOnContainers( OperationType operationType );


    /**
     * Setup cluster
     */
    public void setupCluster();


    /**
     * Destroy cluster
     */
    public void destroyCluster();
}
