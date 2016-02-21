package io.subutai.core.localpeer.api;


import java.util.List;

import io.subutai.common.peer.Host;


/**
 * Task interface
 */
public interface Task<T>
{
    int getTimeout();

    enum State
    {
        PENDING, RUNNING, SUCCESS, FAILURE;
    }

    void start();

    State getState();

    Exception getException();

    T getResult();

    Host getHost();

    boolean isSequential();
}
