package org.safehaus.subutai.configuration.manager.api;


/**
 * Created by bahadyr on 7/17/14.
 */
public class ConfigField {

    private String fieldName;
    private String label;
    private Boolean required;
    private String uiType;
    private String value;


    public String getFieldName() {
        return fieldName;
    }


    public void setFieldName( final String fieldName ) {
        this.fieldName = fieldName;
    }


    public String getLabel() {
        return label;
    }


    public void setLabel( final String label ) {
        this.label = label;
    }


    public Boolean getRequired() {
        return required;
    }


    public void setRequired( final Boolean required ) {
        this.required = required;
    }


    public String getUiType() {
        return uiType;
    }


    public void setUiType( final String uiType ) {
        this.uiType = uiType;
    }


    public String getValue() {
        return value;
    }


    public void setValue( final String value ) {
        this.value = value;
    }
}
