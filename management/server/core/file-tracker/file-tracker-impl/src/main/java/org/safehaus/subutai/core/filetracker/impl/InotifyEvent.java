package org.safehaus.subutai.core.filetracker.impl;


import java.util.Set;
import java.util.UUID;


/**
 * I_Notify Event
 */
public class InotifyEvent
{
    private EventDetails response;


    class EventDetails
    {
        private UUID id;
        private Set<String> configPoints;


        public UUID getId()
        {
            return id;
        }


        public Set<String> getConfigPoints()
        {
            return configPoints;
        }
    }


    public EventDetails getResponse()
    {
        return response;
    }
}
