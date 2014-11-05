package org.safehaus.subutai.plugin.common.api;


import java.util.UUID;


public interface NodeOperationTaskInterface
{
    /**
     * Runs operation tasks on cluster.
     * Example operations could start, stop, status.
     *
     * @return
     */
    public UUID runTask();


    /**
     * This string is going to be used to check if
     * product is running on containers. Particularly for
     * elasticsearch, you need to return "elasticsearch is
     * not running". Then this returned value is going to be
     * used while comparing command results coming from containers.
     * e.g. ( elasticsearch is not running )
     *
     * @return
     */
    public String getProductStoppedIdentifier();


    /**
     * This string is going to be used to check if
     * product is running on containers. Particularly for
     * elasticsearch, you need to return "elasticsearch is
     * running". Then this returned value is going to be
     * used while comparing command results coming from containers.
     * e.g. ( elasticsearch is running )
     * @return
     */
    public String getProductRunningIdentifier();


    /**
     * Wait until operation finishes.
     * @param trackID
     */
    public void waitUntilOperationFinish ( UUID trackID );


}
