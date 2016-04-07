package io.subutai.core.executor.api;


import java.util.Set;

import io.subutai.common.command.Response;
import io.subutai.common.host.HeartBeat;


public interface RestProcessor
{

    void handleResponse( Response response );

    Set<String> getRequests( String hostId );

    void handleHeartbeat( HeartBeat heartBeat );
}
