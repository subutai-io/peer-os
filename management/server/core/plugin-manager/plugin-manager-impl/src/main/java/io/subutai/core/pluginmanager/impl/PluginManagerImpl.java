package io.subutai.core.pluginmanager.impl;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.pluginmanager.api.PluginManager;
import io.subutai.core.pluginmanager.api.dao.ConfigDataService;
import io.subutai.core.pluginmanager.api.model.PermissionJson;
import io.subutai.core.pluginmanager.api.model.PluginDetails;
import io.subutai.core.pluginmanager.impl.dao.ConfigDataServiceImpl;


public class PluginManagerImpl implements PluginManager
{
    private IdentityManager identityManager;
    private DaoManager daoManager;


    private ConfigDataService configDataService;
    private User user;


    public PluginManagerImpl( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    public void init()
    {
        configDataService = new ConfigDataServiceImpl( daoManager, identityManager );
    }


    @Override
    public void register( final String name, final String version, final String pathToKar,
                          final ArrayList<PermissionJson> permissions )
    {
        Date newDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime( newDate );
        cal.add( Calendar.YEAR, 1 );

//        this.user = identityManager
//                .createUser( name, name, "Plugin User", String.format( "%s@subutai.io", name ), UserType.Plugin.getId(),
//                        "test", true );
//        UserToken token = identityManager.createUserToken( user, null, null, "subutai.io", 2, cal.getTime() );
//        User currentUser = identityManager.getUser( name );

//        Role role = identityManager.createRole( name, UserType.Plugin.getId() );
//        identityManager.assignUserRole( currentUser.getId(), role );
//
//        for ( final PermissionJson permission : permissions )
//        {
//            identityManager.removeAllRolePermissions( role.getId() );
//            Permission per = identityManager
//                    .createPermission( permission.getObject(), permission.getScope(), permission.getRead(),
//                            permission.getWrite(), permission.getUpdate(), permission.getDelete() );
//            identityManager.assignRolePermission( role.getId(), per );
//        }

        configDataService.saveDetails( name, version, pathToKar/*, currentUser.getId(), role.getId(), token.getToken() */);
    }


    @Override
    public ConfigDataService getConfigDataService()
    {
        return configDataService;
    }


    @Override
    public List<PluginDetails> getInstalledPlugins()
    {
        return configDataService.getInstalledPlugins();
    }


    @Override
    public void unregister( final Long pluginId )
    {

        configDataService.deleteDetails( pluginId );
    }


    @Override
    public void setPermissions( final Long pluginId, final String permissionJson )
    {
        PluginDetails pluginInfo = configDataService.getPluginDetails( pluginId );
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    public IdentityManager getIdentityManager()
    {
        return identityManager;
    }


    public void setConfigDataService( final ConfigDataService configDataService )
    {
        this.configDataService = configDataService;
    }
}
