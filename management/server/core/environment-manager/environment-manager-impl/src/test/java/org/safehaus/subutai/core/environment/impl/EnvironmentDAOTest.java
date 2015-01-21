package org.safehaus.subutai.core.environment.impl;


import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentPersistenceException;
import org.safehaus.subutai.core.environment.impl.dao.EnvironmentDAO;

import static org.junit.Assert.assertTrue;


/**
 * Created by bahadyr on 9/25/14.
 */
@Ignore
@RunWith( MockitoJUnitRunner.class )
public class EnvironmentDAOTest
{
    private static final String SOURCE = "source";
    private static final String KEY = "key";
    EnvironmentDAO environmentDAO;
    DaoManager daoManager;



    @Before
    public void setUp() throws Exception
    {
        environmentDAO = new EnvironmentDAO( daoManager );
    }


    @Test(expected = EnvironmentPersistenceException.class)
    public void shoudSaveInfo() throws EnvironmentPersistenceException
    {
       environmentDAO.saveInfo( SOURCE, KEY, new DummyClass() );
    }


    @Test
    public void shoudDeleteInfo() throws EnvironmentPersistenceException
    {
         environmentDAO.deleteInfo( SOURCE, KEY );
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
