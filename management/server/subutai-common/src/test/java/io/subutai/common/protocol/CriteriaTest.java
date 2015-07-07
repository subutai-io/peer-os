package io.subutai.common.protocol;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.protocol.Criteria;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class CriteriaTest
{
    private Criteria criteria;

    @Mock
    Object object;


    @Before
    public void setUp() throws Exception
    {
        criteria = new Criteria( "test", object );
        criteria.setId( "testId" );
        criteria.setValue( object );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull(criteria.getId());
        assertNotNull(criteria.getValue());
    }
}