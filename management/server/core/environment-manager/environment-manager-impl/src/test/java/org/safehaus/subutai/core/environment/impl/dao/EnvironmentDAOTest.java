package org.safehaus.subutai.core.environment.impl.dao;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.db.api.DbManager;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;


/**
 * Created by bahadyr on 9/25/14.
 */
@RunWith( MockitoJUnitRunner.class )
public class EnvironmentDAOTest
{
    EnvironmentDAO environmentDAO;
    @Mock
    DbManager dbManager;


    @Before
    public void init()
    {
        environmentDAO = new EnvironmentDAO( dbManager );
    }


    @Test
    public void shoudSaveInfo()
    {
        assertTrue( environmentDAO.saveInfo( "SOURCE", "KEY", new Object() ) );
    }


    @Test
    public void shoudDeleteInfo()
    {
        assertTrue( environmentDAO.deleteInfo( "SOURCE", "KEY" ) );
    }


    @Test
    public void shoudGetInfo()
    {
        environmentDAO.getInfo( "KEY", Object.class );
    }
}
