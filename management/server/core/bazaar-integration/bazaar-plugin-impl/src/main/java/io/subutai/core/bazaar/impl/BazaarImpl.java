package io.subutai.core.bazaar.impl;


import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.bazaar.api.Bazaar;
import io.subutai.core.bazaar.api.dao.ConfigDataService;
import io.subutai.core.bazaar.api.model.Plugin;
import io.subutai.core.bazaar.impl.dao.ConfigDataServiceImpl;
import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.hub.share.common.HubEventListener;
import io.subutai.hub.share.dto.PeerProductDataDto;


public class BazaarImpl implements Bazaar, HubEventListener
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
        this.hubManager.installPlugin(kar, name, uid );
        this.configDataService.savePlugin( name, version, kar, url, uid );
    }


    @Override
    public void uninstallPlugin( Long id, String name )
    {
        this.hubManager.uninstallPlugin( name, this.configDataService.getPluginById( id ).get( 0 ).getUid() );
        this.configDataService.deletePlugin( id );
    }

	@Override
	public void restorePlugin (Long id, String name, String version, String kar, String url, String uid) throws Exception
	{
		this.hubManager.uninstallPlugin(name, uid );
		this.hubManager.installPlugin(kar, name, uid );
		this.configDataService.deletePlugin (id);
		this.configDataService.savePlugin( name, version, kar, url, uid );
	}


    @Override
    public void onRegistrationSucceeded()
    {
        // TODO: send installed plugin list to Hub
    }


    @Override
    public void onPluginEvent( final String pluginUid, final PeerProductDataDto.State state )
    {
        try
        {
            switch ( state )
            {
                case INSTALLED:
                    String jsonString = getProducts();
                    JSONObject productDtosJSON = new JSONObject( jsonString );
                    JSONArray products = productDtosJSON.getJSONArray( "productDtos" );

                    String name = "", version = "", kar = "", url = "", uid = pluginUid;

                    for ( int i = 0; i < products.length(); ++i )
                    {
                        JSONObject product = products.getJSONObject( i );
                        if ( product.get( "id" ).equals( pluginUid ) )
                        {
                            name = product.getString( "name" );
                            version = product.getString( "version" );
                            JSONArray metadata = product.getJSONArray( "metadata" );
                            kar = metadata.length() > 0? metadata.getString( 0 ) : "";
                            url = name.toLowerCase();
                        }
                    }
                    this.configDataService.savePlugin( name, version, kar, url, uid );
                    break;
                case REMOVE:
                    List<Plugin> plugins = this.configDataService.getPluginByUid( pluginUid );
                    if ( !plugins.isEmpty() )
                    {
                        this.configDataService.deletePlugin( plugins.get( 0 ).getId() );
                    }
                    break;
            }
        }
        catch ( Throwable t )
        {
            LOG.error( "Failed to handle plugin event [{}]: {}", pluginUid, t.getMessage() );
        }
    }
}
