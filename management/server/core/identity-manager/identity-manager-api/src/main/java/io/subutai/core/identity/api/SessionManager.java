package io.subutai.core.identity.api;


import java.util.Date;
import java.util.Map;

import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.User;


/**
 * Manages User sessions
 */
public interface SessionManager
{

    //*****************************************
    void startSessionController();


    //*****************************************
    void stopSessionController();


    /* *************************************************
     */
    Session startSession( String sessionId , Session userSession, User user );


    /* ****************************************
     *
     */
    Session getValidSession( String sessionId );


    /* ****************************************
     *
     */
    void extendSessionTime( String sessionId );


    /* ****************************************
     *
     */
    void extendSessionTime( Session userSession );


    /* ****************************************
     *
     */
    void endSession( String sessionId );


    /* ****************************************
     */
    void invalidateSessions(Date currentDate );


    //*****************************************
    int getSessionTimeout();


    //*****************************************
    Map getSessionContext();
}
