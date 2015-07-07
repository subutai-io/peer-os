package io.subutai.core.channel.impl.entity;


import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.channel.impl.entity.UserChannelToken;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class)
public class UserChannelTokenTest
{
    private UserChannelToken userChannelToken;
    @Mock
    Timestamp timestamp;

    @Before
    public void setUp() throws Exception
    {
        userChannelToken = new UserChannelToken();
        userChannelToken.setIpRangeEnd( "ipRangeEnd" );
        userChannelToken.setIpRangeStart( "ipRangeStart" );
        userChannelToken.setStatus( ( short ) 2 );
        userChannelToken.setToken( "token" );
        userChannelToken.setTokenName( "tokenName" );
        userChannelToken.setUserId( ( long ) 5 );
        userChannelToken.setValidPeriod( ( short ) 5 );
        userChannelToken.setDate( timestamp );

    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull(userChannelToken.getDate());
        assertNotNull(userChannelToken.getIpRangeEnd());
        assertNotNull(userChannelToken.getIpRangeStart());
        assertNotNull(userChannelToken.getStatus());
        assertNotNull(userChannelToken.getToken());
        assertNotNull(userChannelToken.getTokenName());
        assertNotNull(userChannelToken.getUserId());
        assertNotNull(userChannelToken.getValidPeriod());
    }
}