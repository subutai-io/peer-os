package io.subutai.common.quota;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.bazaar.share.quota.QuotaException;


@RunWith( MockitoJUnitRunner.class )
public class QuotaExceptionTest
{
    private QuotaException quotaException;

    @Before
    public void setUp() throws Exception
    {
        quotaException = new QuotaException( "test" );
        quotaException = new QuotaException(  );
        quotaException = new QuotaException( new Throwable(  ) );
        quotaException = new QuotaException( "test" , new Throwable(  ) );

    }


    @Test
    public void test()
    {

    }
}