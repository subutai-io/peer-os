package io.subutai.core.identity.impl;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;

import io.subutai.core.identity.impl.SubutaiJdbcRealm;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class SubutaiJdbcRealmTest
{
    private SubutaiJdbcRealm subutaiJdbcRealm;

    @Mock
    javax.sql.DataSource dataSource;
    @Mock
    AuthenticationToken authenticationToken;
    @Mock
    UsernamePasswordToken usernamePasswordToken;
    @Mock
    Connection connection;
    @Mock
    ResultSet resultSet;
    @Mock
    PreparedStatement preparedStatement;
    @Mock
    ResultSetMetaData resultSetMetaData;

    @Before
    public void setUp() throws Exception
    {
        subutaiJdbcRealm = new SubutaiJdbcRealm( dataSource );
    }


    @Test
    public void testSupports() throws Exception
    {
        subutaiJdbcRealm.supports( authenticationToken );
    }


    @Test
    public void testDoGetAuthenticationInfoUserNameNull() throws Exception
    {
        subutaiJdbcRealm.doGetAuthenticationInfo( usernamePasswordToken );
    }


    @Test
    public void testDoGetAuthenticationInfo() throws Exception
    {
        when( usernamePasswordToken.getUsername() ).thenReturn( "userName" );
        when( dataSource.getConnection() ).thenReturn( connection );
        when( connection.prepareStatement( anyString() ) ).thenReturn( preparedStatement );
        when( preparedStatement.executeQuery() ).thenReturn( resultSet );

        subutaiJdbcRealm.doGetAuthenticationInfo( usernamePasswordToken );
    }

}