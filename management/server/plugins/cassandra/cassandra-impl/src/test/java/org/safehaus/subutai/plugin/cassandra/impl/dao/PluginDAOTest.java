package org.safehaus.subutai.plugin.cassandra.impl.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sql.DataSource;

import java.sql.*;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginDAOTest
{
    private PluginDAO pluginDAO;
    private Object object;
    @Mock
    DataSource dataSource;
    @Mock
    Connection connection;
    @Mock
    PreparedStatement preparedStatement;
    @Mock
    ResultSet resultSet;
    @Mock
    ResultSetMetaData resultSetMetaData;

    @Before
    public void setUp() throws SQLException
    {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);

        object = new Object();
        pluginDAO = new PluginDAO(dataSource);
    }

    @Test
    public void testSaveInfo() 
    {
        Boolean bol = pluginDAO.saveInfo("test", "test", object);

        // asserts
        assertTrue(bol);

    }

    @Test
    public void testGetInfo() 
    {
        pluginDAO.getInfo("test", object.getClass());
    }

    @Test
    public void testGetInfo1() 
    {
        pluginDAO.getInfo("test","test",object.getClass());
    }

    @Test
    public void testDeleteInfo() 
    {
        Boolean bol = pluginDAO.deleteInfo("test", "test");

        // asserts
        assertTrue(bol);

    }
}