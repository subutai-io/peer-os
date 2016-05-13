package io.subutai.core.hubmanager.api;


import java.util.Set;


public interface StateLinkProcessor
{
    void processStateLinks( Set<String> stateLinks ) throws HubPluginException;
}
