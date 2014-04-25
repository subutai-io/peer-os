package org.safehaus.kiskis.mgmt.shared.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dilshat on 4/26/14.
 */
public class RefHolder {
    private static RefHolder INSTANCE = new RefHolder();
    private Map<Class, Object> references = new HashMap<>();

    private RefHolder() {
    }

    public static <T> T getRef(Class<T> clazz) {
        return clazz.cast(INSTANCE.references.get(clazz));
    }

    public static void addRef(Object ref) {
        INSTANCE.references.put(ref.getClass(), ref);
    }

}
