package org.safehaus.subutai.core.identity.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.identity.api.IdentityManager;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;

import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class TokenRealmTest
{
    private TokenRealm tokenRealm;

    @Mock
    IdentityManager identityManager;
    @Mock
    AuthenticationToken authenticationToken;
    @Mock
    UserToken userToken;
    @Mock
    PrincipalCollection principalCollection;

    @Before
    public void setUp() throws Exception
    {
        tokenRealm = new TokenRealm( identityManager  );
    }


    @Test
    public void testSupports() throws Exception
    {
        tokenRealm.supports( authenticationToken );
    }


    @Test
    public void testDoGetAuthenticationInfo() throws Exception
    {
        when(userToken.getPrincipal()).thenReturn( "userName" );

        tokenRealm.doGetAuthenticationInfo( userToken );
    }
}