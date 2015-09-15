package io.subutai.core.filetracker.impl;


import io.subutai.core.filetracker.api.InotifyEventType;


/**
 * I_Notify Event
 */
public class InotifyEvent
{
    private EventDetails response;


    public EventDetails getResponse()
    {
        return response;
    }


    static class EventDetails
    {
        private String id;
        private String configPoint;
        private InotifyEventType eventType;
        private String dateTime;


        public String getId()
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
}