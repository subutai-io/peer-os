/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.configuration.impl;


import com.google.gson.JsonObject;
import org.apache.commons.configuration.ConfigurationException;
import org.safehaus.subutai.core.configuration.api.ConfigManager;
import org.safehaus.subutai.core.configuration.api.ConfigTypeEnum;
import org.safehaus.subutai.core.configuration.api.TextInjector;
import org.safehaus.subutai.core.configuration.impl.loaders.*;
import org.safehaus.subutai.core.configuration.impl.utils.*;
import org.safehaus.subutai.common.util.FileUtil;


/**
 * This is an implementation of LxcManager
 */
public class ConfigManagerImpl implements ConfigManager {

	private TextInjector textInjector;


	public TextInjector getTextInjector() {
		return textInjector;
	}


	public void setTextInjector(final TextInjector textInjector) {
		this.textInjector = textInjector;
	}


	@Override
	public boolean injectConfiguration(String hostname, String configFilePath, String jsonObjectConfig,
	                                   ConfigTypeEnum configTypeEnum) {

		//TODO echo to given agent
		ConfigurationLoader configurationLoader = null;
		//        Agent agent = agentManager.getAgentByHostname( agentHostname );

		String type = "";
		switch (configTypeEnum) {
			case YAML: {
				configurationLoader = new YamConfigurationlLoader(textInjector);
				break;
			}
			case PROPERTIES: {
				configurationLoader = new PropertiesConfigurationLoader(textInjector);
				break;
			}
			case XML: {
				configurationLoader = new XMLConfigurationLoader(textInjector);
				break;
			}
			case PLAIN: {
				//TODO
				configurationLoader = new PlainConfigurationLoader(textInjector);
				break;
			}
			case SH: {
				//TODO
				configurationLoader = new ShellConfigurationLoader(textInjector);
				break;
			}
		}
		boolean result = configurationLoader.setConfiguration(hostname, configFilePath, jsonObjectConfig);

		return result;
	}


	@Override
	public JsonObject getConfiguration(String agentHostname, String configPathFilename,
	                                   ConfigTypeEnum configTypeEnum) {
		ConfigurationLoader loader = null;
		switch (configTypeEnum) {
			case YAML: {
				loader = new YamConfigurationlLoader(textInjector);
				break;
			}
			case PROPERTIES: {
				loader = new PropertiesConfigurationLoader(textInjector);
				break;
			}
			case XML: {
				loader = new XMLConfigurationLoader(textInjector);
				break;
			}
			case PLAIN: {
				//TODO
				loader = new PlainConfigurationLoader(textInjector);
				break;
			}
			case SH: {
				//TODO
				loader = new ShellConfigurationLoader(textInjector);
				break;
			}
		}

		return loader.getConfiguration(agentHostname, configPathFilename);
	}

	@Override
	public String getProperty(JsonObject config, String path, ConfigTypeEnum configTypeEnum) {
	    /*ConfigParser configParser = null;
        String content = FileUtil.getContent( configPathFilename, this );
        try {
            switch ( configTypeEnum ) {
                case YAML: {
                    configParser = new XmlParser( content );
                    break;
                }
                case PROPERTIES: {
                    configParser = new IniParser( content );
                    break;
                }
                case XML: {
                    configParser = new XmlParser( content );
                    break;
                }
            }
            configParser.getProperty( path );
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }*/
		return null;
	}

	@Override
	public void setProperty(JsonObject config, String path, String value, ConfigTypeEnum configTypeEnum) {
        /*ConfigParser configParser = null;
//        String content = FileUtil.getContent(configPathFilename , this ); try {
            switch ( configTypeEnum ) {
                case YAML: {
                    configParser = new XmlParser( config );
                    break;
                }
                case PROPERTIES: {
                    configParser = new IniParser( content );
                    break;
                }
                case XML: {
                    configParser = new XmlParser( content );
                    break;
                }
            }
            configParser.setProperty( path, value );
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }*/
	}

	@Override
	public JsonObject getJsonObjectFromResources(String configPathFilename, ConfigTypeEnum configTypeEnum) {
		ConfigParser parser = null;
		String content = FileUtil.getContent(configPathFilename, this);
		try {
			switch (configTypeEnum) {
				case YAML: {
					parser = new XmlParser(content);
					break;
				}
				case PROPERTIES: {
					parser = new IniParser(content);
					break;
				}
				case XML: {
					parser = new XmlParser(content);
					break;
				}
				case PLAIN: {
					parser = new PlainParser(content);
					break;
				}
				case SH: {
					parser = new ShellParser(content);
				}
			}
			return parser.parserConfig(configPathFilename, configTypeEnum);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
}
