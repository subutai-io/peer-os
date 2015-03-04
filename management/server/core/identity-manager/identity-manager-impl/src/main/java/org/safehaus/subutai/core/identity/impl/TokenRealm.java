package org.safehaus.subutai.core.identity.impl;


import java.util.Set;

import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.api.Role;
import org.safehaus.subutai.core.identity.api.User;

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

    private final IdentityManager identityManager;


    public TokenRealm( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
        setCredentialsMatcher( new CredentialsMatcher()
        {
            @Override
            public boolean doCredentialsMatch( final AuthenticationToken token, final AuthenticationInfo info )
            {
                return token.getPrincipal().equals( info.getPrincipals().getPrimaryPrincipal() );
            }
        } );
    }


    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( final PrincipalCollection principals )
    {
        String username = ( String ) principals.getPrimaryPrincipal();
        User user = identityManager.getUser( username );
        Set<String> roles = Sets.newHashSet();
        for ( Role role : user.getRoles() )
        {
            roles.add( role.getName() );
        }
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

        String username = ( String ) userToken.getPrincipal();

        return new SimpleAccount( username, null, getName() );
    }
}
