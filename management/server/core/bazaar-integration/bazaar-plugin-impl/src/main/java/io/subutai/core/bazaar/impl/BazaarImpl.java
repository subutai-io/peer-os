package io.subutai.core.bazaar.impl;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.bazaar.api.Bazaar;
import io.subutai.core.bazaar.api.dao.ConfigDataService;
import io.subutai.core.bazaar.api.model.Plugin;
import io.subutai.core.bazaar.impl.dao.ConfigDataServiceImpl;
import io.subutai.core.hubmanager.api.HubManager;


public class BazaarImpl implements Bazaar
{
	private static final Logger LOG = LoggerFactory.getLogger( BazaarImpl.class );
    private HubManager hubManager;
    private DaoManager daoManager;
    private ConfigDataService configDataService;


    public BazaarImpl( final HubManager hubManager, final DaoManager daoManager )
    {
        this.daoManager = daoManager;
        this.configDataService = new ConfigDataServiceImpl( this.daoManager );
        this.hubManager = hubManager;
	}


	@Override
	public String getChecksum ()
	{
		return this.hubManager.getChecksum();
	}

	@Override
    public String getProducts()
    {
        try
        {

            String result = this.hubManager.getProducts();
            return result;
        }
        catch ( Exception e )
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
    public void installPlugin( String name, String version, String kar, String url, String uid ) throws Exception
    {
		this.hubManager.installPlugin(kar, name );
        this.configDataService.savePlugin( name, version, kar, url, uid );
    }


    @Override
    public void uninstallPlugin( Long id, String name )
    {
        this.hubManager.uninstallPlugin( name );
        this.configDataService.deletePlugin( id );
    }

	@Override
	public void restorePlugin (Long id, String name, String version, String kar, String url, String uid) throws Exception
	{
		this.hubManager.uninstallPlugin(name );
		this.hubManager.installPlugin(kar, name );
		this.configDataService.deletePlugin (id);
		this.configDataService.savePlugin( name, version, kar, url, uid );
	}
}
