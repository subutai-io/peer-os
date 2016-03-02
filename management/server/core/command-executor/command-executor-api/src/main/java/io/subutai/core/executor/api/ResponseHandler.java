package io.subutai.core.executor.api;


import io.subutai.common.command.Response;


public interface ResponseHandler
{

    void handleResponse( Response response );
}
