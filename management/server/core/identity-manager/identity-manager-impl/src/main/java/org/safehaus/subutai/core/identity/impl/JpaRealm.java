package org.safehaus.subutai.core.identity.impl;


import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.core.identity.api.User;
import org.safehaus.subutai.core.identity.impl.dao.UserDataService;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;


/**
 * Created by timur on 1/21/15.
 */
public class JpaRealm extends AuthorizingRealm
{
    private UserDataService userDataService;

    private DaoManager daoManager;


    public void setDaoManager( final DaoManager daoManager )
    {
        userDataService = new UserDataService( daoManager.getEntityManagerFactory() );
        //        userDataService.setEntityManagerFactory( );
    }


    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( final PrincipalCollection principals )
    {
        Long userId = ( Long ) principals.fromRealm( getName() ).iterator().next();
        User user = userDataService.find( userId );
        if ( user != null )
        {
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            //            for ( Role role : user.getRoles() )
            //            {
            //                info.addRole( role.getName() );
            //                info.addStringPermissions( role.getPermissions() );
            //            }

            info.addStringPermissions( user.getPermissions() );

            return info;
        }
        else
        {
            return null;
        }
    }


    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( final AuthenticationToken authcToken )
            throws AuthenticationException
    {
        UsernamePasswordToken token = ( UsernamePasswordToken ) authcToken;
        User user = userDataService.findByUsername( token.getUsername() );
        if ( user != null )
        {
            return new SimpleAuthenticationInfo( user.getId(), user.getPassword(), getName() );
        }
        else
        {
            return null;
        }
    }
}
