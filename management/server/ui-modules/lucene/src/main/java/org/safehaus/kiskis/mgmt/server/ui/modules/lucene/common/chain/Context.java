package org.safehaus.kiskis.mgmt.server.ui.modules.lucene.common.chain;

import java.util.LinkedHashMap;

public class Context extends LinkedHashMap<String, Object> {

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) super.get(key);
    }

}
