package org.safehaus.subutai.common.cache;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandStatus;
import org.safehaus.subutai.common.command.ResponseType;


@RunWith( MockitoJUnitRunner.class )
public class ExpiringCacheTest
{
    private ExpiringCache expiringCache;

    @Mock
    Object object;
    @Mock
    EntryExpiryCallback entryExpiryCallback;

    @Before
    public void setUp() throws Exception
    {
        expiringCache = new ExpiringCache();
    }


    @Test
    public void testGet() throws Exception
    {
        expiringCache.get( object );
    }


    @Test
    public void testPut() throws Exception
    {
        expiringCache.put( object, object, 500 );
    }


    @Test
    public void testPut1() throws Exception
    {
        expiringCache.put( object, object, 500, entryExpiryCallback );
    }


    @Test
    public void testRemove() throws Exception
    {
        expiringCache.remove( object );
    }


    @Test
    public void testGetEntries() throws Exception
    {
        expiringCache.getEntries();
    }


    @Test
    public void testClear() throws Exception
    {
        expiringCache.clear();
    }


    @Test
    public void testSize() throws Exception
    {
        expiringCache.size();
    }


    @Test
    public void testDispose() throws Exception
    {
        expiringCache.dispose();
    }

    @Test
    public void test()
    {
        CommandStatus failed = CommandStatus.FAILED;
        ResponseType inQueue = ResponseType.IN_QUEUE;
    }
}