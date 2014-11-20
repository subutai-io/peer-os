package org.safehaus.subutai.plugin.common.api;


import java.util.UUID;


public interface NodeOperationTaskInterface
{
    /**
     * Runs operation tasks on cluster.
     * Example operations: start, stop, status.
     *
     * @return
     */
    public UUID runTask();


    /**
     * This string will be used while comparing command result
     * to verify if product is running on container.
     *
     * For example; this method should return "elasticsearch is running",
     * since the output of "service elasticsearch status" command
     * "elasticsearch is running" or "elasticsearch is not running".
     * @return should be equal to the result of "service {product} status" command.
     */
    public String getProductStoppedIdentifier();


    /**
     * This string will be used while comparing command result
     * to verify if product is running on container.
     *
     * For example; this method should return "elasticsearch is running",
     * since the output of "service elasticsearch status" command
     * "elasticsearch is running" or "elasticsearch is not running".
     * @return should be equal to the result of "service {product} status" command.
     */
    public String getProductRunningIdentifier();


    /**
     * Wait until operation finishes.
     * @param trackID
     */
    public void waitUntilOperationFinish ( UUID trackID );


}
