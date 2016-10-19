package io.subutai.core.logcollector.rest;

import io.subutai.core.logcollector.api.LogCollector;

import javax.ws.rs.core.Response;

public class LogCollectorRestServiceImpl implements LogCollectorRestService
{
    private LogCollector logCollector;

    public LogCollectorRestServiceImpl( LogCollector logCollector )
    {
        this.logCollector = logCollector;
    }

    @Override
    public Response respondPost()
    {
        return Response.ok().build();
    }

    @Override
    public Response respondGet()
    {
        return Response.ok().build();
    }

    @Override
    public Response respondGet(String data)
    {
        logCollector.addLogMessage(data);
        return Response.ok().build();
    }
}
