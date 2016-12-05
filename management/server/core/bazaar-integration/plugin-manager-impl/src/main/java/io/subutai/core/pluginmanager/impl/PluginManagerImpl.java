package io.subutai.core.pluginmanager.impl;


import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.pluginmanager.api.PluginManager;
import io.subutai.core.pluginmanager.api.dao.ConfigDataService;
import io.subutai.core.pluginmanager.api.model.PermissionJson;
import io.subutai.core.pluginmanager.api.model.PluginDetails;
import io.subutai.core.pluginmanager.impl.dao.ConfigDataServiceImpl;


public class PluginManagerImpl implements PluginManager
{
    private static final Logger LOG = LoggerFactory.getLogger( PluginManagerImpl.class.getName() );

    private IdentityManager identityManager;
    private DaoManager daoManager;


    private ConfigDataService configDataService;


    public PluginManagerImpl( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    public void init()
    {
        configDataService = new ConfigDataServiceImpl( daoManager );
    }


    @Override
    @RolesAllowed( "Plugin-Management|Write" )
    public void register( final String name, final String version, final Attachment kar,
                          final List<PermissionJson> permissions ) throws IOException
    {
        File karFile = new File( System.getProperty( "karaf.home" ) + "/deploy/" + name + ".kar" );

        if ( !karFile.createNewFile() )
        {
            LOG.info( "Plugin {} already exists. Overwriting", name );
        }

        kar.transferTo( karFile );

        Date newDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime( newDate );
        cal.add( Calendar.YEAR, 1 );

        configDataService.saveDetails( name, version, karFile.getAbsolutePath() );
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
    @RolesAllowed( "Plugin-Management|Delete" )
    public void unregister( final Long pluginId )
    {

        configDataService.deleteDetails( pluginId );
    }


    @Override
    public void setPermissions( final Long pluginId, final String permissionJson )
    {
        //todo implement
    }


    @Override
    @RolesAllowed( "Plugin-Management|Update" )
    public void update( final String pluginId, final String name, final String version )
    {
        configDataService.update( pluginId, name, version );
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    public void setConfigDataService( final ConfigDataService configDataService )
    {
        this.configDataService = configDataService;
    }
}
