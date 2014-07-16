package org.safehaus.subutai.configuration.manager.api;


import java.util.ArrayList;
import java.util.List;


/**
 * Created by bahadyr on 7/17/14.
 */
public class Config {

    private String path;
    private ConfigTypeEnum configTypeEnum;
    private List<ConfigField> configField;


    public Config( ) {
        this.configField = new ArrayList<>();
    }


    public void addConfigField(ConfigField configField) {
        this.configField.add( configField );
    }

    public String getPath() {
        return path;
    }


    public void setPath( final String path ) {
        this.path = path;
    }


    public ConfigTypeEnum getConfigTypeEnum() {
        return configTypeEnum;
    }


    public void setConfigTypeEnum( final ConfigTypeEnum configTypeEnum ) {
        this.configTypeEnum = configTypeEnum;
    }


    public List<ConfigField> getConfigField() {
        return configField;
    }


    public void setConfigField( final List<ConfigField> configField ) {
        this.configField = configField;
    }
}
