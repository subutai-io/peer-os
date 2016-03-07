package io.subutai.core.bazaar.impl;


import java.util.List;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.bazaar.api.Bazaar;
import io.subutai.core.bazaar.api.dao.ConfigDataService;
import io.subutai.core.bazaar.api.model.Plugin;
import io.subutai.core.bazaar.impl.dao.ConfigDataServiceImpl;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.Integration;


public class BazaarImpl implements Bazaar
{

    private Integration integration;
    private DaoManager daoManager;
    private ConfigDataService configDataService;


    public BazaarImpl( final Integration integration, final DaoManager daoManager )
    {
        this.daoManager = daoManager;
        this.configDataService = new ConfigDataServiceImpl( this.daoManager );
        this.integration = integration;
    }


    @Override
    public String getProducts()
    {
        try
        {
            String result = this.integration.getProducts();
            return result;
        }
        catch ( HubPluginException e )
        {
            e.printStackTrace();
        }
        return "";
    }


    @Override
    public List<Plugin> getPlugins()
    {
        return this.configDataService.getPlugins();
    }


    @Override
    public void installPlugin( String name, String version, String kar, String url, String uid ) throws HubPluginException
    {
        this.integration.installPlugin( kar );
        this.configDataService.savePlugin( name, version, kar, url, uid );
    }


    @Override
    public void uninstallPlugin( Long id, String kar )
    {
        this.integration.uninstallPlugin( kar );
        this.configDataService.deletePlugin( id );
    }
}
