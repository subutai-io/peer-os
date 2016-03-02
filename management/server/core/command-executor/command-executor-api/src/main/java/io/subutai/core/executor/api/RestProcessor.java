package io.subutai.core.executor.api;


import java.util.Set;

import io.subutai.common.command.Request;
import io.subutai.common.command.Response;


public interface RestProcessor
{

    void handleResponse( Response response );

    Set<Request> getRequests( String hostId );
}
