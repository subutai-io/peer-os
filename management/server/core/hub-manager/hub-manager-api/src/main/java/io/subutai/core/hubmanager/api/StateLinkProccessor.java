package io.subutai.core.hubmanager.api;


import java.util.Set;


public interface StateLinkProccessor
{
    public void proccessStateLinks( Set<String> stateLinks ) throws HubPluginException;
}
