package org.safehaus.subutai.core.environment.impl.dao;


import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;


/**
 * Created by bahadyr on 9/25/14.
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class EnvironmentDAOTest
{
    private static final String SOURCE = "source";
    private static final String KEY = "key";
    EnvironmentDAO environmentDAO;
    @Mock
    DataSource dataSource;


    @Before
    public void setUp() throws Exception
    {
        environmentDAO = new EnvironmentDAO( dataSource);
    }


    @Test
    public void shoudSaveInfo()
    {
        assertTrue( environmentDAO.saveInfo( SOURCE, KEY, new DummyClass() ) );
    }


    @Test
    public void shoudDeleteInfo()
    {
        assertTrue( environmentDAO.deleteInfo( SOURCE, KEY ) );
    }


    @Test
    public void shoudGetInfo()
    {
        environmentDAO.getInfo( KEY, DummyClass.class );
    }
}


class DummyClass
{
    String dummyText = "text";
}
