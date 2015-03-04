package org.safehaus.subutai.core.identity.impl;


import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import com.google.common.collect.Sets;


public class TokenRealm extends AuthorizingRealm
{


    public TokenRealm()
    {
        setCredentialsMatcher( new CredentialsMatcher()
        {
            @Override
            public boolean doCredentialsMatch( final AuthenticationToken token, final AuthenticationInfo info )
            {
                return token.getCredentials().equals( info.getCredentials() );
            }
        } );
    }


    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( final PrincipalCollection principals )
    {
        String principal = ( String ) principals.getPrimaryPrincipal();
        //TODO lookup roles by principal
        return new SimpleAuthorizationInfo( Sets.newHashSet( "admin" ) );
    }


    @Override
    public boolean supports( final AuthenticationToken token )
    {
        return token instanceof UserToken;
    }


    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token ) throws AuthenticationException
    {
        UserToken userToken = ( UserToken ) token;

        String tokenId = ( String ) userToken.getCredentials();
        //TODO lookup username by tokenId
        //throw  AuthenticationException if not found or other checks not met

        return new SimpleAccount( "some-user", tokenId, getName() );
        //        return new AuthenticationInfo() {
        //            @Override
        //            public PrincipalCollection getPrincipals()
        //            {
        //
        //                return null;
        //            }
        //
        //
        //            @Override
        //            public Object getCredentials()
        //            {
        //                return null;
        //            }
        //        };
    }
}
