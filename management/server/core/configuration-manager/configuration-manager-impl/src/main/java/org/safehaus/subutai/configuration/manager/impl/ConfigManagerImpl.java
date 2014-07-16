/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.configuration.manager.impl;


import org.safehaus.subutai.configuration.manager.api.ConfigManager;
import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;
import org.safehaus.subutai.configuration.manager.impl.utils.ConfigurationLoader;
import org.safehaus.subutai.configuration.manager.impl.utils.PropertiesConfigurationLoader;
import org.safehaus.subutai.configuration.manager.impl.utils.XMLConfigurationLoader;
import org.safehaus.subutai.configuration.manager.impl.utils.YamConfigurationlLoader;
import org.safehaus.subutai.shared.protocol.Agent;


/**
 * This is an implementation of LxcManager
 */
public class ConfigManagerImpl implements ConfigManager {


//    public BundleContext bcontext;
//    private Bundle bundle;


//    public void start() {
//        bundle = bcontext.getBundle();
//    }


    /*private String readFile( InputStream is ) throws IOException {
        java.util.Scanner s = new java.util.Scanner( is ).useDelimiter( "\\A" );
        return s.hasNext() ? s.next() : "";
    }*/


//    public void setBcontext( BundleContext bcontext ) {
//        this.bcontext = bcontext;
//    }


    @Override
    public void injectConfiguration( final Object conf, final String path, final Agent agent ) {


    }


    @Override
    public Object getConfiguration(String filepath, ConfigTypeEnum configTypeEnum) {

        ConfigurationLoader configurationLoader = null;
        switch ( configTypeEnum ) {
            case YAML:{
                configurationLoader = new YamConfigurationlLoader();
                break;
            }
            case NAME_VALUE:{
                configurationLoader = new PropertiesConfigurationLoader();
                break;
            }
            case XML: {
                configurationLoader = new XMLConfigurationLoader();
                break;
            }
        }

        return configurationLoader.getConfiguration();
    }
}
