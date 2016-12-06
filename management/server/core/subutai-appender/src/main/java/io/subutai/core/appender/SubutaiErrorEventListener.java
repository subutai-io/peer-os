package io.subutai.core.appender;


import io.subutai.hub.share.dto.SubutaiErrorEvent;


public interface SubutaiErrorEventListener
{
    public void onEvent( SubutaiErrorEvent event );
}
