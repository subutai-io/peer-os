package org.safehaus.subutai.common.quota;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith( MockitoJUnitRunner.class )
public class QuotaExceptionTest
{
    private QuotaException quotaException;

    @Before
    public void setUp() throws Exception
    {
        quotaException = new QuotaException( "test" );
        quotaException = new QuotaException( "test", QuotaType.QUOTA_TYPE_CPU );
        quotaException = new QuotaException(  );
        quotaException = new QuotaException( new Throwable(  ) );
        quotaException = new QuotaException( "test" , new Throwable(  ) );
        quotaException = new QuotaException( "test" , new Throwable(  ), QuotaType.QUOTA_TYPE_ALL_JSON );

    }


    @Test
    public void test()
    {

    }
}