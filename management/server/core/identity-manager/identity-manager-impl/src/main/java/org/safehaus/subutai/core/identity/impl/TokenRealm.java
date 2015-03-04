package org.safehaus.subutai.core.identity.impl;


import java.util.Set;

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
        String username = ( String ) principals.getPrimaryPrincipal();
        //TODO lookup roles by username
        //SecurityManager.getUser(username) and obtain its roles
        Set<String> roles = Sets.newHashSet( "admin" );
        return new SimpleAuthorizationInfo( roles );
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
        //TODO (1) lookup username by tokenId

        String username = "username-from-db";
        //throw  AuthenticationException if not found or other checks not met

        return new SimpleAccount( username, tokenId, getName() );
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
