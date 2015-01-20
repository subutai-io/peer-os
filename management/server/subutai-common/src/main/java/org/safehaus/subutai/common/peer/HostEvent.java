package org.safehaus.subutai.common.peer;


public class HostEvent<T>
{
    private EventType type;
    private T object;
    private Host source;


    public HostEvent( Host source, EventType type, T object )
    {
        this.source = source;
        this.type = type;
        this.object = object;
    }


    public EventType getType()
    {
        return type;
    }


    public T getObject()
    {
        return object;
    }


    public enum EventType
    {
        HOST_REGISTER_SUCCESS, HOST_REGISTER_FAIL, HOST_CLONE_SUCCESS, HOST_CLONE_FAIL, HOST_START_SUCCESS,
        HOST_START_FAIL, HOST_STOP_SUCCESS, HOST_STOP_FAIL, HOST_DESTROY_SUCCESS, HOST_DESTROY_FAIL, HOST_TASK_STARTED,
        HOST_TASK_DONE
    }
}
