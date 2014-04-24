package org.safehaus.kiskis.mgmt.api.sqoop.setting;

import java.util.HashMap;
import java.util.Map;
import org.safehaus.kiskis.mgmt.api.sqoop.DataSourceType;

public class ImportSetting extends CommonSetting {

    DataSourceType type;
    Map<String, Object> parameters;

    public DataSourceType getType() {
        return type;
    }

    public void setType(DataSourceType type) {
        this.type = type;
    }

    public void addParameter(String param, Object value) {
        if(parameters == null) parameters = new HashMap<String, Object>();
        parameters.put(param, value);
    }

    public Object getParameter(String param) {
        return parameters != null ? parameters.get(param) : null;
    }

    public String getStringParameter(String param) {
        Object p = getParameter(param);
        return p != null ? p.toString() : null;
    }

    public boolean getBooleanParameter(String param) {
        Object p = getParameter(param);
        return p != null ? Boolean.parseBoolean(p.toString()) : false;
    }

    @Override
    public String toString() {
        return super.toString() + ", type=" + type + ", parameters="
                + (parameters != null ? parameters.size() : 0);
    }

}
