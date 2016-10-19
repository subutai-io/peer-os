package io.subutai.core.logcollector.api;

public interface LogCollector
{
    void addLogMessage( String message );

    String getLogMessage();
}
