package io.subutai.core.identity.impl;


import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.commons.lang3.time.DateUtils;

import io.subutai.core.identity.api.SessionManager;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.impl.model.UserEntity;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;


/**
 *
 */
@RunWith( MockitoJUnitRunner.class )
public class SessionManagerImplTest
{
    private String sessionId = null;
    private Session session = null;
    //private User user = null;

    //@Mock
    private SessionManager sessionManager = null;


    /* *************************************************
     */
    @Before
    public void setUp() throws Exception
    {
        sessionManager = new SessionManagerImpl(null);

        sessionId = "TestSessionId";
        //user = new UserEntity();
        session = sessionManager.startSession(sessionId , session ,new UserEntity() );
    }


    /* *************************************************
     */
    @Test
    public void testStartSession()
    {
        assertNotNull(session);
    }


    /* *************************************************
     */
    @Test
    public void testGetValidSession()
    {
        Session validSession = sessionManager.getValidSession(sessionId);

        assertEquals(session , validSession);
    }


    /* *************************************************
     */
    @Test
    public void testExtendSessionTime()
    {
        Date sessionDateBefore = session.getStartDate();
        sessionManager.extendSessionTime (session);

        long mSeconds = session.getEndDate().getTime() - sessionDateBefore.getTime();

        mSeconds = (mSeconds/1000)/60;
        assertTrue(mSeconds >= sessionManager.getSessionTimeout());
    }



    /* *************************************************
     */
    @Test
    public void invalidateSessions()
    {
        Date currentDate = DateUtils.addMinutes(session.getStartDate(), sessionManager.getSessionTimeout());

        System.out.println( "Session Time:" + session.getEndDate() + "\nCurrentTime:"+ currentDate);
        assertTrue( session.getEndDate().getTime() <= currentDate.getTime() );

        sessionManager.invalidateSessions( currentDate );

        assertNull( sessionManager.getValidSession(sessionId));
    }


}
