package org.safehaus.subutai.core.filetracker.impl;


import java.util.UUID;

import org.safehaus.subutai.core.filetracker.api.InotifyEventType;


/**
 * I_Notify Event
 */
public class InotifyEvent
{
    private EventDetails response;


    class EventDetails
    {
        private UUID id;
        private String configPoint;
        private InotifyEventType eventType;
        private String dateTime;


        public UUID getId()
        {
            return id;
        }


        public String getConfigPoint()
        {
            return configPoint;
        }


        public InotifyEventType getEventType()
        {
            return eventType;
        }
    }


    public EventDetails getResponse()
    {
        return response;
    }
}